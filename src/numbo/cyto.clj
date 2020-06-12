(ns numbo.cyto
 (:require [clojure.tools.logging :as log]
 										[numbo.misc :as misc]
 										[random-seed.core :refer :all])
 (:refer-clojure :exclude [rand rand-int rand-nth]))




; default amount by which to increase attractiveness of a node, when it's pumped
(def DEFAULT_ATTRACTION_INC 0.7)
; default amount by which to decay attractiveness of a node, each timestep
(def DEFAULT_ATTRACTION_DEC 0.02)
; default starting attraction
(def DEFAULT_ATTRACTION 0.4)



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
 (let [[n m] (split-with #(not= v (:val %1)) s)] (concat n (rest m))))


;(defn -decay-attr
; "Decay the attractiveness of the map br"
; [br]
; (if (nil? br) nil ; nil in, nil out - useful because before being set our TARGET is nil
; 	(assoc br :attr (misc/normalized (:attr br) (* -1 DEFAULT_ATTRACTION_DEC)))))


; ----- Public functions -----

(defn new-cyto
	"Create a blank cytoplasm"
	[]
	'{:bricks ()
	  :blocks ()
	  :targets ()}
)

(def CYTO (atom (new-cyto)))

(defn reset
	"Resets the cytoplasm"
	[]
	(reset! CYTO (new-cyto)))

; ----- Functions for bricks -----

(defn add-brick
 "Add a brick with value v"
 ([c v] (update-in c [:bricks] (partial conj) (-new-node v)))
 ([v] (reset! CYTO (add-brick @CYTO v))))

(defn brick-free?
	"Is a brick with value v free? Are n copies of it free?"
 ([c v n] (-contains-n-vals? (:bricks c) v n))
 ([v n] (brick-free? @CYTO v n))
 ([v] (brick-free? @CYTO v 1)))

(defn largest-brick
 "Return the value of the largest free brick"
 ([c] (first (reverse (sort (map :val (:bricks c))))))
 ([] (largest-brick @CYTO)))

; The complicated stuff in here I cribbed from misc/select-val-in-range and random-val-in-range

(defn random-brick
	"Return a random brick, or n random bricks, probabilistically weighted by attraction"
	([c n]
		(loop [ranges (misc/make-percent-ranges (:bricks c) :attr) ret '()]
			(let [entry (first (filter #(< (rand-int (first (last ranges))) (first %)) ranges))]
				(if (or (empty? ranges) (= n (count ret))) ret
					(recur (misc/seq-remove ranges entry) (conj ret (:val (second entry))))))))
 ([n] (random-brick @CYTO n))
	([] (random-brick @CYTO 1)))

; Not efficient but we'll be going over 5 bricks tops...

(defn closest-brick
	"Returns the closest brick to the value v"
	([c v] (ffirst (sort-by val (into '{} (map #(hash-map (:val %1) (Math/abs (- v (:val %1)))) (:bricks c))))))
	([v] (closest-brick @CYTO v)))

; ----- Functions for blocks -----

; b should be a list of [op b1 b2] where b1 and b2 can be blocks themselves, or integers

(defn add-block
	"Add a new block b to the cytoplasm c"
	([c b]
		(if (every? true? (map (partial brick-free?) (rest b))) ; if all the parameters are free bricks
		 (-> c
				(update-in [:blocks] (partial conj) (-new-node b)) ; add the new block to the cyto
				(update-in [:bricks] (partial -remove-first) (second b)) ; remove the first brick, because it's taken
				(update-in [:bricks] (partial -remove-first) (nth b 2))) ; remove the second brick, because it's taken
		 (do
		 	(log/warn "add-block failed, bricks not free for " b)
		 c))) ; otherwise don't
	([b] (reset! CYTO (add-block @CYTO b))))

(defn block-exists?
 "Does the cytoplasm contain the supplied block b?"
 ([c b] (-contains-n-vals? (:blocks c) b 1))
 ([b] (block-exists? @CYTO b)))

;(defn add-brick
;	"Adds a single brick to memory"
;	([br val free] (conj br (assoc (new-entry val) :free free)))
;	([br val] (add-brick br val true))
; ([val] (reset! BRICKS (add-brick @BRICKS val))))

;(defn update-brick
; "Updates the supplied brick br into the bricks list bl"
; ([bl br] (map #(if (= (:uuid br) (:uuid %1)) br %1) bl))
; ([br] (reset! BRICKS (update-brick @BRICKS br))))


;(defn add-block
; "Adds a new block to memory"
;	([bl b] (conj bl b))
;	([b] (reset! BLOCKS (add-block @BLOCKS b))))


;(defn get-random-brick
; "Return a random brick, only free ones if f"
; ([br f] (let [possibles (if f (filter #(= true (:free %1)) br) br)]
; 									(if (empty? possibles) nil
; 									 (rand-nth possibles))))
; ([f] (get-random-brick @BRICKS f)))

;(defn get-random-block
; "Return a random block"
; ([bl] (if (empty? bl) nil (rand-nth bl)))
; ([] (get-random-block @BLOCKS)))

;(defn get-largest-brick
;	"Return the value of the largest free brick in memory, nil if there's none"
;	([br]
;	 (let [free-bricks (filter :free br)]
;	 	(if (empty? free-bricks) nil (apply max-key :value free-bricks))))
;	([] (get-largest-brick @BRICKS)))


;(defn pump-node
; "Pump a node with uuid u in memory w, by increasing its attractiveness"
; ([ta br bl u]
;	 (let [[entry src] (find-anywhere ta br bl u)]
;	   (if (nil? entry)
;	   	(do (log/warn "Couldn't find node" u) nil)
;			 	(let [pumped-entry (assoc entry :attr (misc/normalized (:attr entry) DEFAULT_ATTRACTION_INC))]
;				  (condp = src
;				  	:target (update-target pumped-entry)
;				  	:bricks (update-brick pumped-entry)
;				  	:blocks (update-blocks pumped-entry)
;				  	(log/warn "Couldn't find a type to pump for" src)
;				  )))))
; ([u] (pump-node @TARGET @BRICKS @BLOCKS u)))

;(defn get-brick-by-val
; "Get a random brick with the value v"
; ([br v]
;  (let [vals (filter #(= v (:value %1)) br)]
;;  	(if (not-empty vals) (rand-nth vals) nil)))
; ([v] (get-brick-by-val @BRICKS v)))

;(defn get-block-by-result
; "Get a random block with the result v"
; ([bl v]
;  (let [vals (filter #(= v (:value %1)) bl)]
;  	(if (not-empty vals) (rand-nth vals) nil)))
; ([v] (get-block-by-result @BLOCKS v)))

;(defn invert-val
; "Invert the value of key k in map m"
; [k m]
; (assoc m k (- 1 (k m))))

;(defn get-unattractive-block
; "Get a random block with the result v"
; ([bl]
;  (let [bl-range (misc/make-percent-ranges (map (partial invert-val :attr) bl) :attr)]
;  	(if (not-empty bl-range) (invert-val :attr (misc/random-val-in-range bl-range)))))
; ([] (get-unattractive-block @BLOCKS)))

;(defn -get-random-by-type
;	"Get a random node, sampled probabilistically by activation from all nodes of :type t"
; [p t]
;	(let [op-range (misc/make-percent-ranges (filter #(= t (:type %)) (vals p)) :activation)]
;	 (if (not-empty op-range)
;	  (misc/random-val-in-range op-range))))

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

(defn decay
 "Causes all attractiveness of all blocks to drop"
 []
; (do
; 	(reset! TARGET (-decay-attr @TARGET))
;	 (reset! BRICKS (map -decay-attr @BRICKS))
;	 (reset! BLOCKS (map -decay-blocktree @BLOCKS))

	 )



(add-brick 1)
(add-brick 3)
(add-brick 5)
(add-brick 6)
(add-brick 7)
(add-brick 9)
(add-brick 15)

(add-block '(+ 1 5))
(add-block '(+ 6 7))
