(ns numbo.codelet
	(:require [clojure.tools.logging :as log]
											[clojure.string :as str]
											[numbo.coderack :as cr]
											[numbo.misc :as misc]
											[numbo.pnet :as pn]
											[numbo.working :as wm]))

(def codelet-types :load-target)

(def URGENCY_HIGH 3)
(def URGENCY_MEDIUM 2)
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

(defn -noop
 [] nil)

(defn new-codelet
 "Create a skeleton of a new codelet, with optional modified fields"
 [& s]
 (into (hash-map :urgency URGENCY_LOW :fn -noop :iteration @cr/ITERATIONS) (map vec (partition 2 s))))



;----- HELPER FUNCTIONS USED BY CODELETS -----
; move these into their files

(defn wm-get-random-free-bricks
 "Returns a sequence of randomly chosen free bricks (probabilistically chosen)"
	[]
	)

;----- CODELETS HEREON -----

; activates a specific node in the Pnet

(defn activate-pnet
	[n]
	(do
	(log/debug "activate-pnet n=" n)
	(cr/add-codelet (new-codelet :type :activate-pnet
																														:desc (str "Activate PNet: " n)
																														:urgency URGENCY_MEDIUM
																														:fn (fn [] (pn/activate-node n)))))
)
(defn pump-node
 "Pumps the attractiveness of a brick or block with UUID u"
	[u]
	(cr/add-codelet (new-codelet :type :inc-attraction
																														:desc (str "Pump brick/block: " u)
																														:urgency URGENCY_MEDIUM :fn
																														(fn [] (wm/pump-node u)))))

; load-target - (high urgency) when a target is loaded, the pnet landmark closest is activated.
; operands (* - +) are activated. If it’s larger than the largest brick, * is activated more.
; (p143)

(defn load-target
	"Add target node, activate closest number in Pnet, and operands (* if target is larger than largest brick)"
 [v]
 (cr/add-codelet
 	(new-codelet :type :load-target :desc (str "Load target: " v) :urgency URGENCY_HIGH
	 	:fn (fn []
		 	(do
		 		(wm/set-target v)
		 	 (activate-pnet (keyword (str (misc/closest (pn/get-numbers) v))))
		 	 (map activate-pnet (pn/get-operators))
		 	 (if (and 
		 	 	(not (nil? (wm/get-largest-brick)))
		 	 	(> v (:value (wm/get-largest-brick))))
		 	 		(activate-pnet :times))
		 	)))))

; syntactic-comparison (mid urgency) There is a type of codeliet which inspects various nodes and
; notices syntactic similarities, increases attractiveness of them - e.g. if brick 11 shares digits 
; with target 114, increase attractiveness of 11 (p141)

(defn rand-syntactic-comparison
 "Examine a random brick or block, compare to the target, pump it if promising"
 []
 (let [blocks (list (wm/get-random-brick false) (wm/get-random-block))
 						block (rand-nth blocks)
 						val (str (:value block))
 						tval (str (:value @wm/TARGET))]
 						(cr/add-codelet (new-codelet :type :syntactic-comparison :desc (str "Compare " val " to target " tval) :urgency URGENCY_LOW
 						:fn (fn []
							 (cond
							 	(nil? block) nil ; if we couldn't find a block to compare to the target, just do nothing
							  ; either node contains the other, as a string - e.g. 114 contains 11, 15 contains 5, 51 contains 5
							  (or
							  	(str/includes? val tval)
							  	(str/includes? tval val)) (pump-node (:uuid block))
							 ))))))

(defn load-brick
 "Loads a new brick into memory"
 [v]
	(cr/add-codelet (new-codelet :type :load-brick :desc (str "Load brick: " v) :urgency URGENCY_HIGH
	:fn (fn [] 
		(do
			(wm/add-brick v)
			(activate-pnet (keyword (str (misc/closest (pn/get-numbers) v)))))))))

; Tries to build a block which makes something close to a biped in the pnet
; Find a biped: i.e. a randomly highly activated node of type :calculation
; Make a block for this calculation, by looking for  brick/block-results for each param, or :similar :params if not
; Load a test-if-possible-and-desirable codelet on this block




; Best matches might be an exact match of the brick/block, or the exact match for a brick/block which is
; :similar in the Pnet to the node n. If there's no best match, return nil

(defn -best-match-for
 "Given a Pnet node n, find the best match for it in bricks or blocks, returning the brick/block"
 [n]
 (let [val (misc/int-k n)
 						perfect-block (wm/get-block-by-result val)
 						perfect-brick (wm/get-brick-by-val val)
 						similar (map misc/int-k (pn/get-similar n))
 						similar-blocks (map wm/get-block-by-result similar)
 						similar-bricks (map wm/get-brick-by-val similar)
 						similar-list (filter (complement nil?) (concat similar-blocks similar-bricks))
 						]
	 (cond 
	 	perfect-block perfect-block
	 	perfect-brick perfect-brick
	 	(not-empty similar-list) (rand-nth similar-list)
	 	:else nil
)))

; Search bricks and blocks to find an entry which is free and has the given value
(defn -find-free-values
 "Find a free brick or block with the given value"
 [br bl v]
 (filter #(and (= v (:value %1)) (:free %1)) (concat br bl)))

(defn test-block
	"Schedule a test of a given block UUID u, if it can be found"
	[u]
	(let [[block src] (wm/find-anywhere u)]
		(cond
			(nil? block) (log/info "test-block couldn't find UUID " u) ; Blocks may have been dismantled
			(= :target src) (log/warn "test-block resolved UUID to target " u) ; Should be impossible
			(= :brick src) (log/warn "test-block resolved UUID to brick " u) ; Should be impossible
			:else	(cr/add-codelet (new-codelet :type :test-block :desc (str "Test block: " block) :urgency URGENCY_HIGH
				:fn (fn []

				(let [params (:params block)
										p1-entry (first (-find-free-values @wm/BRICKS @wm/BLOCKS (first params)))
										p2-entry (if (= (first params) (second params)) ; cope with the case where both params are the same
																				(second (-find-free-values @wm/BRICKS @wm/BLOCKS (first params)))
																				(first (-find-free-values @wm/BRICKS @wm/BLOCKS (second params))))]
										(log/debug "test-block p1-entry=" p1-entry " p2-entry=" p2-entry)
					(if
						(and
						 p1-entry ; if there is a free entry for each parameter
						 p2-entry
						 (not= p1-entry p2-entry)
						 (> 0.2 (:activation (get @pn/PNET (keyword (str (misc/closest (pn/get-numbers) (:value block)))))))) ; and  nearest value in the Pnet is active (i.e. this is worthy)
								(do
								 (log/info "test-block " u " has params available and is worthy")
									(wm/mark-taken (:uuid p1-entry))
									(wm/mark-taken (:uuid p2-entry))
								)
								(do
								 (log/info "test-block " u " judged unworthy")
								 (log/debug "test-block before delete " @wm/BLOCKS)
									(wm/delete-block u)
								 (log/debug "test-block after delete " @wm/BLOCKS)

									) ; otherwise it hasn't proved useful... remove it
				))))))))

(defn seek-facsimile
 "Find a highly activated calculation, make a block for it, and schedule a test of it"
 []
 (let [calc (pn/get-random-calc)]
	 (cr/add-codelet (new-codelet :type :seek-facsimile :desc (str "Seek facsimile: " (pn/format-calc calc)) :urgency URGENCY_MEDIUM
	 :fn (fn []
	  (let [links (:links calc)
	        params (filter (complement nil?) (mapv #(:value (-best-match-for %1)) (pn/filter-links-for links :param))) ; TODO need to get the -best-match-for here
	        op (first (pn/filter-links-for links :operator))
	        result (apply (op pn/operator-map) params) ; Remember, result may not be the original one from the calculation...
	        new-block (wm/new-entry result op params)]
	        (if (= (count params) 2) (do ; It's possible we don't find enough best matches - in which case the seek has failed
		        	(wm/add-block new-block) ; Add a new block for the calculation to WM
		        	(test-block (:uuid new-block)))) ; Schedule a new test of it in future
	        	)
	  )))))

; rand-op: (low urgency) - select 2 random bricks (biased by attractiveness), and an op
; (biased towards active pnet nodes),  place resulting block in the WM (p145, #4)

(defn rand-block
 "Make a new block out of sampled random bricks and ops"
 []
 (let [b1 (wm/get-random-brick false)
 						v1 (:value b1)
 						b2 (wm/get-random-brick false)
 						v2 (:value b2)
 						op (pn/get-random-op)]
 						(cr/add-codelet (new-codelet :type :new-block
 							:desc (str "Random op: " (:name op) " " v1 "," v2)
 							:urgency URGENCY_MEDIUM
 							:fn (fn [] (wm/add-block (wm/new-entry 
 																			(((:name op) pn/operator-map) v1 v2)
 																			(:name op)
 																			(vector v1 v2))))))))

(defn dismantler
 "Picks a random low-attractiveness block and removes it, returning taken bricks"
 []
 (let [block (wm/get-unattractive-block)
 						uuid (:uuid block)]
 (cr/add-codelet (new-codelet :type :dismantler
 	:desc (str "Dismantle " uuid)
 	:urgency URGENCY_LOW
 	:fn (fn [] (wm/delete-block uuid)) ; TODO: also undo any mark-takens
 )
)))

;----- END OF CODELETS -----
