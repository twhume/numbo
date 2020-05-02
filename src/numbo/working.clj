(ns numbo.working
	(:require [clojure.zip :as zip])
 (:require [numbo.misc :as misc])
	(:require [rhizome.viz :as rh]))

; 3rd iteration of working memory.
;
; Insights:
; 1. We can keep bricks and targets somewhere separate to the blocks
; 2. Storage for blocks can therefore be a simpler nested structure

(def BRICKS (atom '()))
(def TARGET (atom nil))
(def BLOCKS (atom '()))

; BRICKS is a list of Entries, TARGET is an Entry
; Entries are maps with a :value, a random :uuid and an :attr(activeness)
;
; Entries in BLOCKS can also have an :op(erator) and a vector of 2 children, each ints or block entries
; (so Entries in BLOCKS are each "BlockTrees")

; ----- Private functions -----

(defn -initial-attr
	"Calculates an initial attractiveness for the number, based on its value"
 [n]
 (cond-> 1
 	(= 0 (mod n 5)) (+ 5)
 	(= 0 (mod n 10)) (+ 5)
 	(= 0 (mod n 100)) (+ 5)
 ))

(defn -new-entry
 "Creates a new memory entry structure"
 ([v] (hash-map :value v :uuid (misc/uuid) :attr (-initial-attr v)))
 ([v o p] (assoc (-new-entry v) :op o :params p)))

(defn -make-blocktree-zipper
 "Make a clojure zipper from the blocktree bt"
	[bt]
	(zip/zipper #(not (empty? (:params %1)))
		:params  (fn [n s] (assoc n :params (vec s)))
		bt))

(defn -find-blocktree-loc
 "Given a blocktree zipper z, find the entry with UUID u"
	[z u]
	(loop [cur z]
	 (cond
	 	(zip/end? cur) nil
	 	(= u (:uuid (zip/node cur))) cur
	 	:else (recur (zip/next cur))))) 

(defn -add-blocktree-entry
 "Add the supplied block entry be to the block tree bt as the p child of block u"
 [u p be bt]
 (let [zipper (-make-blocktree-zipper bt)
 						node (-find-blocktree-loc zipper u)]
 						(if (nil? node) bt ; If we can't add to the tree, just return it as is
 							 (zip/root 
	 							 (cond
	 							  (= p 0) (zip/replace (zip/down node) be)
	 							  (= p 1) (zip/replace (zip/right (zip/down node)) be)
	 							  :else (do 
	 							  	(println "Bad child position " p)
	 							  	node
	 							 ))))))

; ----- Public functions -----

(defn reset
	"Resets the working memory"
	[]
	(do
		(reset! BRICKS '())
		(reset! TARGET nil)
		(reset! BLOCKS '())))

(defn add-brick
	"Adds a single brick to memory"
	([br val free] (conj br (assoc (-new-entry val) :free free)))
	([br val] (add-brick br val true))
 ([val] (reset! BRICKS (add-brick @BRICKS val))))

(defn set-target
 "Sets the target value in memory"
 ([v] (reset! TARGET (-new-entry v))))

(defn add-block
 "Adds a new block to memory"
	([bl value op p] (conj bl (-new-entry value op p)))
	([value op p] (reset! BLOCKS (add-block @BLOCKS value op p))))

(defn add-child-block
 "Adds a child to a block in memory bl, by its uuid"
 ([blocks par-uuid par-param value op p]
		(let [new-entry (-new-entry value op p)]
			(map (partial -add-blocktree-entry par-uuid par-param new-entry) blocks)))
 ([par-uuid par-param value op p]
 	(reset! BLOCKS (add-child-block @BLOCKS par-uuid par-param value op p))))

(defn get-random-brick
 "Return a random brick, only free ones if f"
 ([br f] (let [possibles (if f (filter :free br) br)]
 									(if (empty? possibles) nil
 									 (rand-nth possibles))))
 ([f] (get-random-brick @BRICKS f)))

(defn get-largest-brick
	"Return the value of the largest free brick in memory, nil if there's none"
	([br]
	 (let [free-bricks (filter :free br)]
	 	(if (empty? free-bricks) nil (apply max-key :value free-bricks))))
	([] (get-largest-brick @BRICKS)))

(defn print-state
 "(for debug purposes) print the current WM state"
 []
 (do
	 (println "TARGET:" @TARGET)
	 (println "BRICKS:" @BRICKS)
	 (println "BLOCKS:" @BLOCKS)))

;------ OLD STUFF BELOW HERE ----


; default amount by which to increase attractiveness of a node, when it's pumped
;(def DEFAULT_ATTRACTION_INC 5)
; default starting attraction
;(def DEFAULT_ATTRACTION 1)


; Contributors to temperature:
; # secondary targets
; # nodes which are highly attractive
; # free nodes
;
; High temperature --> less promising; dismantler codelets loaded into coderack, to dismantle probabilistically chosen targets


(defn get-temperature
 "What's the temperature of the working memory m?"
 [m])

;(defn pump-node
; "Pump a node n in memory w, by increasing its attractiveness"
; ([w n]
;	 (let [pumped-n (assoc n :attractiveness (+ (:attractiveness n) DEFAULT_ATTRACTION_INC))]
; 		(conj (remove #{n} w) pumped-n))))


