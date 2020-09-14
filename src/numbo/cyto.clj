(ns numbo.cyto
 (:require [clojure.set :refer [intersection]]
											[clojure.tools.logging :as log]
 										[numbo.config :as cfg :refer [CONFIG]]
 										[numbo.misc :as misc]
 										[numbo.pnet :as pn]
 										[random-seed.core :refer :all])
 (:refer-clojure :exclude [rand rand-int rand-nth]))

; has a solution been found?
(def COMPLETE (misc/thread-local (atom false)))

; ----- Private functions -----

(defn -new-node
 "Create a new node with value v, attractiveness a"
 ([v a] {:val v :attr a})
 ([v] (-new-node v 0)))

(defn -initial-attr
	"Calculates an initial attractiveness for a given value"
 [n]
 (cond-> (:ATTR_DEFAULT @@CONFIG)
 	(= 0 (mod n 5)) (+ 0.1)
 	(= 0 (mod n 10)) (+ 0.1)
 	(= 0 (mod n 100)) (+ 0.1)
 ))

; Handles the case where a :val can be an integer (bricks) or a sequence (blocks)
(defn -contains-n-evals?
 "Does the supplied sequence s contain >=n nodes with value v?"
 [s v n]
 (>=
 	(count (filter #(= v (eval (:val %1))) s)) n))

(defn -contains-n-vals?
 "Does the supplied sequence s contain >=n nodes with value v?"
 [s v n]
 (>=
 	(count (filter #(= v (:val %1)) s)) n))

(defn -remove-first
 "Return sequence s without the first instance of a node with value v"
 [s v]
 ((if (vector? s) vec identity) ; preserve vectorhood in inputs, as we rely on it for ordering purposes
 	(let [[n m] (split-with #(not= v (:val %1)) s)] (concat n (rest m)))))

(defn -remove-each
 "Returns sequence s1 removing nodes with values of each instance of s2"
 [s1 s2]
 (reduce -remove-first s1 s2))

(defn -bricks-for-block
 "Takes a potentially nested block and returns all the bricks"
 [b]
 (filter (and (complement sequential?) int?) (tree-seq sequential? seq b)))

; Not exactly efficient but we'll be going over ~5 nodes at most.
;TODO: if there are 2 with the same value, return a random one
(defn -closest-node
	"Given a sequence s of nodes, apply function f to the value of each and return one with the nearest result to v"
	[s f v]
	(ffirst (sort-by val (into '{} (map #(hash-map (:val %1) (Math/abs (- v (f (:val %1))))) s)))))

(defn -inc-attr
 "Up the :attr value of nodes with :val n in the map m by i"
	[i m n] (let [matches (filter #(= n (:val %1)) m)
															updates (map #(assoc %1 :attr (misc/normalized i (:attr %1))) matches)]
															(replace  (apply hash-map (interleave matches updates)) m)))

(defn -dec-attr
 "Decay the :attr of each entry in the sequence s by i"
 [i s]
 ((if (vector? s) vec identity) ; preserve vectorhood in inputs, as we rely on it for ordering purposes
		(map #(assoc %1 :attr (misc/normalized (:attr %1) (* -1 i))) s)))

; ----- Public functions -----

(defn new-cyto
	"Create a blank cytoplasm"
	[]
	'{:bricks ()
	  :blocks ()
	  :targets [] ; Vector because the first entry is the primary target
	  :original-bricks () ; used to keep a second record of all available bricks, to test solutions
  }
)

(def CYTO (misc/thread-local (atom (new-cyto))))
(def BRICK-ATTR-SAMPLER (misc/thread-local (atom (misc/mk-sampler @CYTO :attr :bricks))))
(def TARGET-ATTR-SAMPLER (misc/thread-local (atom (misc/mk-sampler @CYTO :attr :targets))))
(def BLOCK-INVATTR-SAMPLER (misc/thread-local (atom (misc/mk-sampler @CYTO :attr :blocks (partial misc/invert-val :attr)))))
(def NODE-ATTR-SAMPLER (misc/thread-local (atom (misc/mk-sampler @CYTO :attr (fn [x] (concat (:blocks x) (:bricks x)))))))

(defn reset
	"Resets the cytoplasm"
	[]
	(reset! @CYTO (new-cyto))
	(reset! @COMPLETE false)
	(reset! @BRICK-ATTR-SAMPLER (misc/mk-sampler @CYTO :attr :bricks))
 (reset! @TARGET-ATTR-SAMPLER (misc/mk-sampler @CYTO :attr :targets))
 (reset! @BLOCK-INVATTR-SAMPLER (misc/mk-sampler @CYTO :attr :blocks (partial misc/invert-val :attr)))
 (reset! @NODE-ATTR-SAMPLER (misc/mk-sampler @CYTO :attr (fn [x] (concat (:blocks x) (:bricks x)))))
	)

; ----- Functions for targets -----

(defn set-target
 "Sets the primary target to t"
 ([c t] (if (empty? (:targets c))
 	(update-in c [:targets] conj (-new-node t))
 	(do
 		(log/warn "set-target" t "but there's already a target in " (:targets c))
 		c)))
 ([t] (reset! @CYTO (set-target @@CYTO t))))

(defn get-target
	"Returns the current primary target"
	([c] (:val (first (:targets c))))
	([] (get-target @@CYTO)))

(defn get-target2
	"Returns all secondary targets, or a random one"
	([c] (map :val (rest (:targets c))))
	([] (get-target2 @@CYTO)))

(defn random-target
	"Returns a random target, probabilistically weighted by attraction"
	[]
	(:val (@@TARGET-ATTR-SAMPLER)))

(defn add-target2
 "Adds a secondary target t"
 ([c t] (if (> (count (:targets c)) 0)
 	(update-in c [:targets] conj (-new-node t (-initial-attr t)))
 	(do
 		(log/warn "add-target2" t "but no primary target set")
 		c)))
 ([t] (reset! @CYTO (add-target2 @@CYTO t))))

(defn del-target2
 "Removes the first instance of a secondary target t"
 ([c t]
 (cond
  (= t (get-target)) (do (log/warn "del-target2 called on primary target" t) c)
  (empty? (rest (:targets c))) (do (log/warn "del-target2 called but no secondary targets set") c)
  :else (update-in c [:targets] -remove-first t)))
 ([t] (reset! @CYTO (del-target2 @@CYTO t))))

(defn -plug-block
 "In block a, find a leaf node where block b can provide the value and substitute b for that node"
 [a b]
 (do
 	(log/debug "-plug-block" a b)
 	(assoc a :val (misc/replace-first (eval b) b (:val a)))
 ))

(defn -can-plug?
 "Can the expression b be plugged into the block a?"
 [a b]
 (some #(= (eval b) %1) (:val a)))

; Forgive me father for I have sinned, there must be an idiomatic method of doing this.

(defn -plug-blocks
 "In list-of-blocks s substitute b in for a value in the first place it can apply"
 [s b]
 (loop [blocks s ret '()]
 	(let [cur (first blocks)]
 		(cond
 			(empty? blocks) s ; if we never found a match, return the original
 			(-can-plug? cur b) (concat ret (cons (-plug-block cur b) (rest blocks))) ; plug in & return where we can
 			(= (:val cur) b) (recur (rest blocks) ret) ; don't add the block we're plugging in
 			:else (recur (rest blocks) (conj ret cur))))))

(defn plug-target2
	"Given the brick or block b, which evals to a secondary target, find the first block where it might plug in, and plug it in"
	([c b] (do
		(log/debug "plug-target2 c=" c "b="b)
		(if (seq? b) (update-in c [:blocks] -plug-blocks b) c))) ; only plug in blocks, plugging in bricks is a noop
 ([b] (reset! @CYTO (plug-target2 @@CYTO b))))

; ----- Functions for bricks -----

(defn add-brick
 "Add a brick with value v"
 ([c v] (-> c
 	(update-in [:bricks] conj (-new-node v (-initial-attr v)))
 	(update-in [:original-bricks] conj v)))
 ([v] (reset! @CYTO (add-brick @@CYTO v))))

(defn brick-free?
	"Is a brick with value v free? Are n copies of it free?"
 ([c n v] (-contains-n-vals? (:bricks c) v n))
 ([n v] (brick-free? @@CYTO n v))
 ([v] (brick-free? @@CYTO 1 v)))

(defn largest-brick
 "Return the value of the largest free brick"
 ([c] (first (reverse (sort (map :val (:bricks c))))))
 ([] (largest-brick @@CYTO)))

(defn random-brick
	"Return a random brick, or n random bricks, probabilistically weighted by attraction"
	[] (:val (@@BRICK-ATTR-SAMPLER)))

(defn closest-brick
	"Returns the closest brick to the value v"
	([c v] (do
		(log/debug "closest-brick c=" c "v=" v)
		(-closest-node (:bricks c) identity v)))
	([v] (closest-brick @@CYTO v)))

; Used when going from (target=100) (* 24 4) --> (+ (* 24 4) 4)
; (combine-target '(* 24 4) 4)

(defn combine-target2
 "Combine the block b with an existing secondary target t, using operator o"
 ([c b t o]
  (log/debug "combine-target2 c=" c "b=" b "t=" t "o=" o)
 	(let [bl (first (filter #(= b (:val %1)) (:blocks c))) ; look up the block entry
 							new-bl (assoc bl :val (list o (:val bl) t))] ; update it to the new form
  (log/debug "combine-target2 c=" c "b=" b "t=" t "o=" o "bl=" bl "new-bl=" new-bl)
	 	(if (nil? bl) (do
	 		(log/warn "combine-target2 couldn't find block" b)
	 		c)
	 	(update-in c [:blocks] (partial replace (hash-map bl new-bl))))))
 ([b t o] (reset! @CYTO (combine-target2 @@CYTO b t o))))

; ----- Functions for blocks -----

; Herein, block b should be a list of [op b1 b2] where b1 and b2 can be blocks themselves, or integers

(defn block-exists?
 "Does the cytoplasm contain the supplied block b?"
 ([c b] (-contains-n-vals? (:blocks c) b 1))
 ([b] (block-exists? @@CYTO b)))

(defn add-block
	"Add a new block b to the cytoplasm c"
	([c b]
		(if 
			(or
				(and (= (second b) (misc/third b)) (brick-free? c 2 (second b))) ; check we have 2 copies of the parameter free, if they're the same
				(every? true? (map #(or (seq? %1) (brick-free? c 1 %1)) (rest b)))) ; if all the parameters of the block are free bricks or blocks
		 (-> c
				(update-in [:blocks] conj (-new-node b (-initial-attr (eval b)))) ; add the new block to the cyto
				(update-in [:blocks] (partial reduce -remove-first) (filter seq? (rest b)))
				(update-in [:bricks] (partial reduce -remove-first) (-bricks-for-block b))) ; remove all the bricks from the free list
		 (do
		 	(log/warn "add-block failed, bricks not free for " b)
		 	c))) ; otherwise don't
	([b] (reset! @CYTO (add-block @@CYTO b))))

(defn del-block
 "Removes the block b from the cytoplasm c, returning its bricks"
 ([c b]
 	(if (block-exists? c b) ; if the cytoplasm still contains this block
 	 (let [bricks (-bricks-for-block b)
 	 						shared (misc/common-elements (rest (map :val (:targets c))) bricks)
 	 						shared2 (mapcat -bricks-for-block )
 	 						remaining (misc/remove-each bricks shared)]
 	 	(log/debug "del-block bricks=" b "shared=" shared "remaining=" remaining)
	 		(-> c
	 			(update-in [:blocks] -remove-first b) ; remove the block from blocks
	 			(update-in [:targets] -remove-each shared) ; remove any secondary targets it used
	 			(update-in [:bricks] (partial apply conj) (map #(-new-node %1 (-initial-attr %1)) remaining)))) ; return any remaining bricks
	 		(do
	 		 (log/warn "del-block failed, block not in cytoplasm " b)
	 		 	c))); otherwise don't
 ([b] (reset! @CYTO (del-block @@CYTO b))))

(defn closest-block
 "Return the block which most closely produces v"
	([c v] (-closest-node (:blocks c) eval v))
	([v] (closest-block @@CYTO v)))

(defn unworthy-block
 "Return a random block, weighted by the inverse of its attractiveness"
 []
	(:val (@@BLOCK-INVATTR-SAMPLER))) ; invert it back again once we have it

(defn get-block
	"Read the block b from cyto c"
	([c b] (first (filter #(= b (:val %1)) (:blocks c))))
	([b] (get-block @@CYTO b)))

(defn format-block
 "Turn the block or brick b into a nice human-readable calculation"
 [b]
  (if (seq? b)
		 (let [op (first b) p1 (second b) p2 (misc/third b)]
		 	(str "("
		 		(if (seq? p1) (format-block p1) p1)
		 		" "
			 	(get pn/op-lookups op)
			 	" "
		 		(if (seq? p1) (format-block p2) p2)
		 		")"
		 	))
		  b))

; ----- Functions for nodes (i.e. blocks AND bricks) -----

(defn random-node
	"Return a random node, or n random nodes, probabilistically weighted by attraction"
 ([n] (:val (@@NODE-ATTR-SAMPLER n)))
	([] (:val (@@NODE-ATTR-SAMPLER))))

(defn node-free?
	"Is a node with value v free? Are n copies of it free?"
 ([c n v] (-contains-n-evals? (concat (:bricks c) (:blocks c)) v n))
 ([n v] (node-free? @@CYTO n v))
 ([v] (node-free? @@CYTO 1 v)))

(defn pump-node
 "Pump the attractiveness of the node n"
 ([c n]
 	(-> c
 		(update-in [:blocks] (partial -inc-attr (:ATTR_INC @@CONFIG)) n)
 		(update-in [:bricks] (partial -inc-attr (:ATTR_INC @@CONFIG)) n)
 		(update-in [:targets] (partial -inc-attr (:ATTR_INC @@CONFIG)) n)
 	))
 ([v] (reset! @CYTO (pump-node @@CYTO v))))

(defn closest-node
 "Return the node which most closely produces v"
	([c v] (-closest-node (concat (:bricks c) (:blocks c)) eval v))
	([v] (closest-node @@CYTO v)))


(defn -lookup-node
 "Look up the node with value v in cytoplasm c, preferring bricks to blocks"
 [c v]
 (if
 	(some #{v} (map :val (:bricks c))) v ; it's a brick, just return it
 	(first (filter #(= (eval %1) v) (map :val (:blocks c))))))

(defn fuzzy-closest
	"Return an element of sequence s close to the input value n: closest 70% of the time, second-closest 20%, third 10%"
	[s n]
	(do
		(log/debug "fuzzy-closest s=" s " n=" n)
		(let [which (condp >= (rand)
																(:FUZZY_CLOSEST_TOP @@cfg/CONFIG) first
																(:FUZZY_CLOSEST_MID @@cfg/CONFIG) (if (> (count s) 1) second first)
																(condp >= (count s)
																	2 first
																	1 second
																	misc/third
																))]
		 (log/debug "which=" which "s=" (count s))
			(nth s (first (which (sort-by second (map-indexed #(list %1 (Math/abs (- n %2))) s))))))))

(defn fuzzy-closest-seq
 "Return a list of integer elements from sequence c which are closest to each item in s, w/o reusing elements"
 [c s]
 (loop [nodes c
 							ins s
 							ret '[]]
 							(if (or (empty? ins) (empty? nodes)) ret
	 							(let [best-match (fuzzy-closest nodes (first ins))]
	 								(recur
	 									(misc/seq-remove nodes best-match)
	 									(rest ins)
	 									(conj ret best-match)
	 								)))))

(defn closest-nodes
 "Return the nodes in cytoplasm c which most closely match those in input sequence s"
 ([c s] 
  (do (log/debug "closest-nodes c=" c "s=" s)
  (map (partial -lookup-node c)
  	(fuzzy-closest-seq (map :val (concat (:bricks c) (map eval (:blocks c)))) s))))
 ([s] (closest-nodes @@CYTO s)))

; ----- Other functions -----

(defn decay
 "Causes all attractiveness of all bricks, blocks, targets to drop"
 ([c]
	 (-> c
	 	(update-in [:blocks] (partial -dec-attr (:ATTR_DEC @@CONFIG)))
	 	(update-in [:bricks] (partial -dec-attr (:ATTR_DEC @@CONFIG)))
	 	(update-in [:targets] (partial -dec-attr (:ATTR_DEC @@CONFIG)))))
 ([] (reset! @CYTO (decay @@CYTO))))

(defn get-solutions
 "Return all blocks which are complete and valid solutions"
 ([c]
		(filter
			#(and
				(= (get-target c) (eval (:val %1))) ; block evaluates to the target
				(=
				  (count (-bricks-for-block (:val %1)))
						(count (misc/common-elements (-bricks-for-block (:val %1)) (:original-bricks c)))))
			(:blocks c)))
 ([] (get-solutions @@CYTO)))

; Contributors to temperature:
; # secondary targets - 0.2 * (# secondary targets)
; # blocks which are highly attractive: (- ((sum of attr of blocks) / 2))
; # free bricks:  + 0.2 for each
;
; no secondary targets, no blocks, 5 bricks = 1.0
; 1 secondary target, 1 block 0.7 attr, 3 bricks = -0.2 -0.35 + 0.6 = 0.05
; 1 block 0.4 attr, 3 bricks  = -0.2 + 0.6 = 0.4

;
;
; High temperature --> less promising; dismantler codelets loaded into coderack, to dismantle probabilistically chosen targets
; Temperature is on a scale of 0..1

(defn get-temperature
 "What's the temperature of the cytoplasm c?"
 ([c] 
 (misc/normalized
	 (+
	  (* -0.2 (dec (count (:targets c)))) ; lose 0.2 for each secondary target we have
	  (* 0.2 (count (:bricks c))) ; add 0.2 for every free brick
	  (/ (reduce + (map :attr (:blocks c))) 2) ; + half the sum of the attr of all blocks 	
	 ) 0.1 0.9))
 ([] (get-temperature @@CYTO)))