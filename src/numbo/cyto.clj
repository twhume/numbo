(ns numbo.cyto
 (:require [clojure.tools.logging :as log]
 										[numbo.misc :as misc]
 										[numbo.pnet :as pn]
 										[random-seed.core :refer :all])
 (:refer-clojure :exclude [rand rand-int rand-nth]))

; default amount by which to increase attractiveness of a node, when it's pumped
(def DEFAULT_ATTRACTION_INC 0.7)
; default amount by which to decay attractiveness of a node, each timestep
(def DEFAULT_ATTRACTION_DEC 0.01)
; default starting attraction
(def DEFAULT_ATTRACTION 0.4)

; has a solution been found?
(def COMPLETE (atom false))


; ----- Private functions -----

(defn -new-node
 "Create a new node with value v, attractiveness a"
 ([v a] {:val v :attr a})
 ([v] (-new-node v 0)))

(defn -initial-attr
	"Calculates an initial attractiveness for a given value"
 [n]
 (cond-> DEFAULT_ATTRACTION
 	(= 0 (mod n 5)) (+ 0.1)
 	(= 0 (mod n 10)) (+ 0.1)
 	(= 0 (mod n 100)) (+ 0.1)
 ))

(defn -contains-n-vals?
 "Does the supplied sequence s contain >=n nodes with value v?"
 [s v n]
 (>= (count (filter #(= v (:val %1)) s)) n))

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

(defn -n-random-nodes
 "Return n nodes, weighted by attraction, from the supplied list s"
 [s n]
 (loop [ranges (misc/make-percent-ranges s :attr) ret '()]
			(let [entry (first (filter #(< (rand-int (first (last ranges))) (first %)) ranges))]
				(if (or (empty? ranges) (= n (count ret))) ret
					(recur (misc/seq-remove ranges entry) (conj ret (:val (second entry))))))))

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
	  :targets []} ; Vector because the first entry is the primary target
)

(def CYTO (atom (new-cyto)))

(defn reset
	"Resets the cytoplasm"
	[]
	(reset! CYTO (new-cyto))
	(reset! COMPLETE false))

; ----- Functions for targets -----

(defn set-target
 "Sets the primary target to t"
 ([c t] (if (empty? (:targets c))
 	(update-in c [:targets] (partial conj) (-new-node t))
 	(do
 		(log/warn "set-target" t "but there's already a target in " (:targets c))
 		c)))
 ([t] (reset! CYTO (set-target @CYTO t))))

(defn get-target
	"Returns the current primary target"
	([c] (:val (first (:targets c))))
	([] (get-target @CYTO)))

(defn get-target2
	"Returns all secondary targets"
	([c] (map :val (rest (:targets c))))
	([] (get-target2 @CYTO)))

(defn random-target
	"Returns a random target, probabilistically weighted by attraction"
	([c] (-n-random-nodes (:targets c) 1))
 ([] (random-target @CYTO)))

(defn add-target2
 "Adds a secondary target t"
 ([c t] (if (> (count (:targets c)) 0)
 	(update-in c [:targets] (partial conj) (-new-node t (-initial-attr t)))
 	(do
 		(log/warn "add-target2" t "but no primary target set")
 		c)))
 ([t] (reset! CYTO (add-target2 @CYTO t))))

(defn del-target2
 "Removes the first instance of a secondary target t"
 ([c t]
 (cond
  (= t (get-target)) (do (log/warn "del-target2 called on primary target" t) c)
  (empty? (rest (:targets c))) (do (log/warn "del-target2 called but no secondary targets set") c)
  :else (update-in c [:targets] (partial -remove-first) t)))
 ([t] (reset! CYTO (del-target2 @CYTO t))))

(defn random-target
 "Return a random target entry"
	([c] (if (empty? (:targets c)) nil (:val (rand-nth (:targets c)))))
	([] (random-target @CYTO)))

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
 ([b] (reset! CYTO (plug-target2 @CYTO b))))

; ----- Functions for bricks -----

(defn add-brick
 "Add a brick with value v"
 ([c v] (update-in c [:bricks] (partial conj) (-new-node v (-initial-attr v))))
 ([v] (reset! CYTO (add-brick @CYTO v))))

(defn brick-free?
	"Is a brick with value v free? Are n copies of it free?"
 ([c n v] (-contains-n-vals? (:bricks c) v n))
 ([n v] (brick-free? @CYTO n v))
 ([v] (brick-free? @CYTO 1 v)))

(defn largest-brick
 "Return the value of the largest free brick"
 ([c] (first (reverse (sort (map :val (:bricks c))))))
 ([] (largest-brick @CYTO)))

; The complicated stuff in here I cribbed from misc/select-val-in-range and random-val-in-range

(defn random-brick
	"Return a random brick, or n random bricks, probabilistically weighted by attraction"
	([c n] (-n-random-nodes (:bricks c) n))
 ([n] (random-brick @CYTO n))
	([] (first (random-brick @CYTO 1))))

(defn closest-brick
	"Returns the closest brick to the value v"
	([c v] (-closest-node (:bricks c) identity v))
	([v] (closest-brick @CYTO v)))

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
 ([b t o] (reset! CYTO (combine-target2 @CYTO b t o))))

; ----- Functions for blocks -----

; Herein, block b should be a list of [op b1 b2] where b1 and b2 can be blocks themselves, or integers

(defn block-exists?
 "Does the cytoplasm contain the supplied block b?"
 ([c b] (-contains-n-vals? (:blocks c) b 1))
 ([b] (block-exists? @CYTO b)))

(defn add-block
	"Add a new block b to the cytoplasm c"
	([c b]
		(if 
			(or
				(and (= (second b) (misc/third b)) (brick-free? c 2 (second b))) ; check we have 2 copies of the parameter free, if they're the same
				(every? true? (map (partial brick-free? c 1) (-bricks-for-block b)))) ; if all the parameters of the block are free bricks
		 (-> c
				(update-in [:blocks] (partial conj) (-new-node b (-initial-attr (eval b)))) ; add the new block to the cyto
				(update-in [:bricks] (partial reduce -remove-first) (-bricks-for-block b))) ; remove all the bricks from the free list
		 (do
		 	(log/warn "add-block failed, bricks not free for " b)
		 	c))) ; otherwise don't
	([b] (reset! CYTO (add-block @CYTO b))))

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
	 			(update-in [:blocks] (partial -remove-first) b) ; remove the block from blocks
	 			(update-in [:targets] (partial -remove-each) shared) ; remove any secondary targets it used
	 			(update-in [:bricks] (partial apply conj) (map #(-new-node %1 (-initial-attr %1)) remaining)))) ; return any remaining bricks
	 		(do
	 		 (log/warn "del-block failed, block not in cytoplasm " b)
	 		 	c))); otherwise don't
 ([b] (reset! CYTO (del-block @CYTO b))))

(defn closest-block
 "Return the block which most closely produces v"
	([c v] (-closest-node (:blocks c) eval v))
	([v] (closest-block @CYTO v)))

(defn block-result
	"Return all blocks which have result v"
 ([c v] (filter #(= v (eval (:val %1))) (:blocks c)))
 ([v] (block-result @CYTO v)))

(defn unworthy-block
 "Return a random block, weighted by the inverse of its attractiveness"
 ([c]
		(misc/invert-val :attr ; invert it back again once we have it
			(misc/random-val-in-range
				(misc/make-percent-ranges (map (partial misc/invert-val :attr) (:blocks c)) :attr))))
	([] (unworthy-block @CYTO)))

(defn get-block
	"Read the block b from cyto c"
	([c b] (first (filter #(= b (:val %1)) (:blocks c))))
	([b] (get-block @CYTO b)))

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
	([c n] (-n-random-nodes (concat (:bricks c) (:blocks c)) n))
 ([n] (random-node @CYTO n))
	([] (first (random-node @CYTO 1))))

(defn free-nodes
 "Return all the bricks or blocks with the value v"
 ([c v] (concat
 	(filter #(= v (:val %1)) (:bricks c))
  (filter #(= v (eval (:val %1))) (:blocks c))))
 ([v] (free-nodes @CYTO v)))

(defn pump-node
 "Pump the attractiveness of the node n"
 ([c n]
 	(-> c
 		(update-in [:blocks] (partial -inc-attr DEFAULT_ATTRACTION_INC) n)
 		(update-in [:bricks] (partial -inc-attr DEFAULT_ATTRACTION_INC) n)
 		(update-in [:targets] (partial -inc-attr DEFAULT_ATTRACTION_INC) n)
 	))
 ([v] (reset! CYTO (pump-node @CYTO v))))

(defn closest-node
 "Return the node which most closely produces v"
	([c v] (-closest-node (concat (:bricks c) (:blocks c)) eval v))
	([v] (closest-node @CYTO v)))

; ----- Other functions -----

(defn decay
 "Causes all attractiveness of all bricks, blocks, targets to drop"
 ([c]
	 (-> c
	 	(update-in [:blocks] (partial -dec-attr DEFAULT_ATTRACTION_DEC))
	 	(update-in [:bricks] (partial -dec-attr DEFAULT_ATTRACTION_DEC))
	 	(update-in [:targets] (partial -dec-attr DEFAULT_ATTRACTION_DEC))))
 ([] (reset! CYTO (decay @CYTO))))

(defn get-solutions
 "Return all blocks which are complete and valid solutions"
 ([c]
	(filter
		#(and
			(= (get-target c) (eval (:val %1))) ; block evaluates to the target
			(empty?
				(clojure.set/intersection
					(set (-bricks-for-block (:val %1)))
					(set (get-target2 c))))) (:blocks c))) ; there's no bricks used which are secondary targets
 ([] (get-solutions @CYTO)))

; Contributors to temperature:
; # secondary targets
; # nodes which are highly attractive
; # free nodes
;
; High temperature --> less promising; dismantler codelets loaded into coderack, to dismantle probabilistically chosen targets
; Temperature is on a scale of 0..1

(defn get-temperature
 "What's the temperature of the cytoplasm c?"
 ([c] 
 (+
  (* 0.05 (count (:blocks c))) ; Add 0.05 for each block we have; lots of blocks --> trigger dismantlers
 	(* 0.1 (count (:bricks c))) ; Add 0.1 for each free brick
; 	(* -0.05 (count (filter (partial < 0.3) ; Subtract 0.05 for each node with an :attr > 0.3
; 								(filter (complement nil?) (mapcat #(map :attr (-blocktree-nodes %1)) bl)))))
 	; TODO add secondary targets stuff in
 	
 ))
 ([] (get-temperature @CYTO)))


(add-brick 1)
(add-brick 3)
(add-brick 5)
(add-brick 6)
(add-brick 7)
(add-brick 9)
(add-brick 15)

(add-block '(+ 1 5))
(add-block '(+ 6 7))
