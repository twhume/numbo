(ns numbo.codelet
	(:require [clojure.tools.logging :as log]
											[clojure.string :as str]
											[numbo.coderack :as cr]
											[numbo.cyto :as cy]
											[numbo.misc :as misc]
											[numbo.pnet :as pn]
											[random-seed.core :refer :all])
	(:refer-clojure :exclude [rand rand-int rand-nth]))

(def codelet-types :load-target)

(def URGENCY_HIGH 5)
(def URGENCY_MEDIUM 3)
(def URGENCY_LOW 1)

(def urgency-labels '{
	URGENCY_HIGH "High"
	URGENCY_MEDIUM "Medium"
	URGENCY_LOW "Low"
})

; Lets us create codelets with a consistent set of fields.
; Example usage:
;
; (new-codelet)
; (new-codelet :urgency 10)
;
;
; Codelet fields are:
; :urgency
; :fn
; :type (one of codelet-types)
; :desc description

(defn -format-block
 "Make a nice printable version of the calculation in b"
 [b]
 (if (seq? b)
 	(str (second b) (get pn/op-lookups (first b)) (misc/third b) "=" (eval b))
 	(str b)))

(defn new-codelet
 "Create a skeleton of a new codelet, with optional modified fields"
 [& s]
 (into (hash-map :urgency URGENCY_LOW :fn nil :iteration @cr/ITERATIONS) (map vec (partition 2 s))))

;----- CODELETS HEREON -----

; activates a specific node in the Pnet

(defn activate-pnet
	[n]
	(do
	(cr/add-codelet (new-codelet :type :activate-pnet
																														:desc (str "Activate PNet: " n)
																														:urgency URGENCY_MEDIUM
																														:fn (fn [] (do
																												 		(log/info "activate-pnet" n)
																												 		(pn/activate-node n)))))))

(defn pump-node
 "Pumps the attractiveness of a node"
	[n]
	(cr/add-codelet (new-codelet :type :inc-attraction
																														:desc (str "Pump node: " (-format-block n))
																														:urgency URGENCY_MEDIUM :fn
																														(fn [] (do
																												 		(log/info "pump-node" n)
																															(cy/pump-node n))))))

; syntactic-comparison (low urgency) There is a type of codeliet which inspects various nodes and
; notices syntactic similarities, increases attractiveness of them - e.g. if brick 11 shares digits 
; with target 114, increase attractiveness of 11 (p141)

(defn rand-syntactic-comparison
 "Examine a random brick or block, compare to the target, pump it if promising"
 []
 (let [node (cy/random-node)
 						nval (str (eval node))
 						tval (str (cy/random-target))]
 						(if node
	 						(cr/add-codelet (new-codelet :type :rand-syntactic-comparison
	 																																			:desc (str "Compare " nval " to target " tval)
	 																																			:urgency URGENCY_LOW
	 																																			:fn (fn []
	 						 (do
	 	 						(log/info "rand-syntactic-comparison val=" nval "tval=" tval)
									 (if; either node contains the other, as a string - e.g. 114 contains 11, 15 contains 5, 51 contains 5
									  (or
									  	(str/includes? nval tval)
									  	(str/includes? tval nval)) (do
									  		(log/debug "rand-syntactic-comparison pumping " node)
									  		(pump-node node))))))))))

(defn check-done
	"Is there a block in the cytoplasm which is complete and == target?"
	[]
 (cr/add-codelet (new-codelet :type :check-done
 																													:desc (str "Check done")
 																													:urgency URGENCY_HIGH
 																													:fn (fn [] 
		(do
			(log/info "check-done")
			(if (not (empty? (cy/get-solutions)))
				(do
					(log/info "check-done SOLVED! " (first (cy/get-solutions)))
					(reset! cy/COMPLETE true))))))))

(defn fulfil-target2
 "Given a target block or brick b which we believe to resolve to value of a secondary target, plug it in"
 [b]
 (cr/add-codelet (new-codelet :type :fulfil-target2
 																													:desc (str "Fulfil target2:" (-format-block b))
 																													:urgency URGENCY_HIGH
 																													:fn (fn [] 
		(do
			(log/info "fulfil-target2 b=" b)
			(if (and
				(or (cy/brick-free? b) (cy/block-exists? b)) ; either a brick or block can fulfil a target
				(some #{(eval b)} (cy/get-target2)))
				(do
					(log/debug "fulfil-target2 b=" b ", fulfilling target2")
					(cy/plug-target2 b)
					(cy/del-target2 (eval b))
					(check-done))
				(log/debug "fulfil-target2 b=" b "or target2 no longer exists")))))))

; rand-target-match
; See if we have a block which is an exact match for a secondary target
; Triggered randomly or when we add a secondary target

(defn rand-target-match
	"Look to see if a free brick matches a target"
	[]
	(cr/add-codelet (new-codelet :type :rand-target-match
																														:desc (str "Target2 brick match?")
																														:urgency URGENCY_MEDIUM
																														:fn (fn [] (do
		(let [target2 (cy/random-target)
								brick (if target2 (cy/closest-brick target2) nil)]
								(log/info "rand-target-match" target2 brick)
								(if (and target2 (= target2 brick))
									(do
									 (log/debug "rand-target-match matched, fulfilling")
										(fulfil-target2 brick))
								 (log/debug "rand-target-match not matched")
									)))))))

; load-target - (high urgency) when a target is loaded, the pnet landmark closest is activated.
; operands (* - +) are activated. If it’s larger than the largest brick, * is activated more.
; (p143)

(defn load-target
	"Add target node, activate closest number in Pnet, and operands (* if target is larger than largest brick)"
 [v]
 (cr/add-codelet
 	(new-codelet :type :load-target
 														:desc (str "Load target: " v)
 														:urgency URGENCY_HIGH
	 													:fn (fn []
		 	(do
		 		(log/info "load-target" v)
		 		(cy/set-target v)
		 	 (activate-pnet (pn/closest-keyword v))
		 	 (doall (map activate-pnet (pn/get-operators))) ; activate all operators
		 	 (rand-target-match) ; check we don't have a brick for it
		 	 (cond

		 	 	; if we have an enormous target, get ready to multiply
		 	 	(and 
			 	 	(not (nil? (cy/largest-brick))) (> v (cy/largest-brick))) 
			 	 		(activate-pnet :times)

			 	 	; if we have a small target, prefer to subtract
		 	 	(< v 10)
		 	 		(activate-pnet :minus)

			 	 		))))))

(defn create-target2
 "Create a target block with b as one arm, a secondary target of t2 and an operator op combining them, pump the target2"
	[b t2 op]
 (cr/add-codelet (new-codelet :type :create-target2
 																													:desc (str "Create target2:" (-format-block b) " off by " t2)
 																													:urgency URGENCY_HIGH
 																													:fn (fn [] 
		(do
			(log/info "create-target2 b=" b ",t2=" t2 ",op=" op)
			(if (cy/block-exists? b)
				(do
					(log/debug "create-target2 b=" b ", adding target2")
					(cy/add-target2 t2)
					(cy/combine-target2 b t2 op)																										
					(activate-pnet (pn/closest-keyword t2)) ; light up the pnet for the target
					(doall (map activate-pnet (pn/get-operators))) ; activate all operators
					(pump-node (list op b t2)) ; pump the newly created block, so it's less likely to die
				 (pump-node t2) ; pump that secondary target, so it's more likely to be targeted
				 (rand-target-match)) ; see if we have a brick that matches it
				(log/debug "create-target2 b=" b "no longer exists")))))))

; Run on newly created blocks; compares them to the target and if they're close, kick off
; a create-secondary-target codelet. Also compare to secondary targets and if there's a
; match, kick off a fulfil-target2 codelet to plug it in

(defn probe-target2
 "Probes a newly created block b to see if it justifies or fulfils a secondary target"
 [b]
 (cr/add-codelet (new-codelet :type :probe-target2
																													 :desc (str "Probe target2:" (-format-block b))
																													 :urgency URGENCY_HIGH
																													 :fn (fn []

		(do
			(log/info "probe-target2 b=" (-format-block b))
			(if (cy/block-exists? b)
				(cond

				 ; If b is equal to a secondary target, connect it up

				 (some #{(eval b)} (cy/get-target2)) (fulfil-target2 b)

					; If this is near to the main target, set up a secondary target

					(misc/within (cy/get-target) (eval b) 0.4)
						(do
							(log/debug "probe-target2 b=" b "deserves target2")
							(create-target2 b (Math/abs (- (eval b) (cy/get-target))) (if (< (eval b) (cy/get-target)) + -)))

					:else (log/debug "probe-target2 b=" b "doesn't deserve target2"))))))))

(defn load-brick
 "Loads a new brick with value v into the cytoplasm"
 [v]
	(cr/add-codelet (new-codelet :type :load-brick
																														:desc (str "Load brick: " v)
																														:urgency URGENCY_HIGH
																														:fn (fn [] 
		(do
	  (log/info "load-brick " v)
			(cy/add-brick v)
			(activate-pnet (pn/closest-keyword v)))))))

(defn test-block
	"Schedule a test of a given block b, if it can be found"
	[b]
  (cr/add-codelet (new-codelet :type :test-block
																									  				:desc (str "Test block: " (-format-block b))
																									  				:urgency URGENCY_HIGH
																															:fn (fn []
			(let [b1 (second b)
									b2 (misc/third b)
									op (first b)
									bstr (-format-block b)]
				(log/info "test-block b1=" b1 "b2=" b2 "op=" op)
				(cond

					 ; If it evaluates to the target, check we're done!

					 (= (cy/get-target) (eval b)) (check-done)

						; If this is near a highly activated node, it's worth pursuing
						(not (empty? (pn/val-near-and-activated 25 (eval b) 0.2)))
;						(>= (:activation ((pn/closest-keyword (eval b)) @pn/PNET)) 0.2)
							(do
							 (log/debug "test-block b=" bstr " is worthy")
							 (probe-target2 b))

							:else
							 (do
							 	(log/debug "test-block b=" bstr " is not worthy")
							 	(log/debug "test-block b=" str ", (= b1 b2)=" (= b1 b2))
							 	(log/debug "test-block b=" str ", (cy/brick-free? b2 2)=" (cy/brick-free? b2 2))
							 	(log/debug "test-block b=" str ", (cy/brick-free? b1)=" (cy/brick-free? b1))
							 	(log/debug "test-block b=" str ", closest=" ((pn/closest-keyword (eval b)) @pn/PNET))
							 	(log/debug "test-block b=" str ", activation" (:activation ((pn/closest-keyword (eval b)) @pn/PNET)))
							 	(log/debug "test-block b=" str ", act-test" (>= (:activation ((pn/closest-keyword (eval b)) @pn/PNET)) pn/DEFAULT_INC))

							 	(cy/del-block b))))))))

; Tries to build a block which makes something close to a biped in the pnet
; Find a biped: i.e. a randomly highly activated node of type :calculation
; Make a block for this calculation, by looking for the nearest brick or block for each param
; TODO: if we can't find an exact :param find a :similar one
; Load a test-block codelet on this block

(defn seek-facsimile
 "Find a highly activated calculation, make a block for it, and schedule a test of the block"
 []
 (let [calc (pn/get-random-calc)]
	 (cr/add-codelet (new-codelet :type :seek-facsimile
																					 									:desc (str "Seek facsimile: " (pn/format-calc calc))
																					 									:urgency URGENCY_MEDIUM
																					 									:fn (fn []
	  (let [ope ((first (pn/filter-links-for (:links calc) :operator)) pn/operator-map)
	        params (map (comp cy/closest-node misc/int-k) (pn/filter-links-for (:links calc) :param))]
	       	(log/info "seek-facsimile for " (pn/format-calc calc))

	       	; TODO: check that all the params are within 50% of the desired
	       	; TODO: check the result is desirable (somewhere)?

	        (if
	        	(and
	        		(= (count params) 2) ; It's possible we don't find enough best matches - in which case the seek has failed
	        		(or
	        			(and
	        				(= (first params) (second params))
	        				(cy/brick-free? 2 (first params) )) ; either the params are same and free twice
	        			(and
	        				(not= (first params) (second params))
	        				(cy/brick-free? (first params))
	        				(cy/brick-free? (second params))) ; or they differ and are free
	        			))
	        			(do 
			        		(log/debug "seek-facsimile START ")
			        		(log/debug "seek-facsimile DOING " ope params)
			        		(cy/add-block (cons ope params)) ; add it to the cytoplasm
				        	(test-block (cons ope params)) ; Schedule a new test of it in future
			        		(log/debug "seek-facsimile DONE " ope params)

		        	 
	        	))))))))

; rand-op: (low urgency) - select 2 random bricks (biased by attractiveness), and an op
; (biased towards active pnet nodes),  place resulting block in the WM (p145, #4)

(defn rand-block
 "Make a new block out of sampled random bricks and ops"
 []
 (let [rand-op (pn/get-random-op)
 						op ((:name rand-op) pn/operator-map)
 						[b1 b2] (cy/random-brick 2)
 						]
 						(log/debug "rand-block op=" op "b1=" b1 "b2=" b2)
 						(if (and b1 b2 op)
	 						(cr/add-codelet (new-codelet :type :rand-block
																													 							:desc (str "Random block: " (-format-block (list op b1 b2)))
																													 							:urgency URGENCY_MEDIUM
																													 							:fn (fn []
		(do
			(log/info "rand-block adding " (-format-block (list op b1 b2)))
			(cy/add-block (list op b1 b2))
			(test-block (list op b1 b2))
		)))))))

(defn dismantler
 "Picks a random low-attractiveness block and removes it, returning taken bricks"
 []
 (let [block (cy/unworthy-block)]
 						(if block
							 (cr/add-codelet (new-codelet :type :dismantler
																																			 	:desc (str "dismantler " (-format-block (:val block)))
																																			 	:urgency URGENCY_LOW
																																			 	:fn (fn []
		(let [bl (cy/get-block (:val block))]
			(log/info "dismantler " (:val bl))
			(if (and bl (< (:attr bl) 0.3)) ; Never abandon a promising theory
				(cy/del-block (:val bl))))))))))

;----- END OF CODELETS -----
