(ns numbo.wm3
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

; BRICKS and TARGETS are lists of Entries.
; Entries are maps with a :value, a random :uuid and an :attr(activeness)
; Entries in BLOCKS can also have an :op(erator) and a vector of 2 children, each ints or block entries
; (so Entries in BLOCKS are each trees)

(defn -initial-attr
 "Default attractiveness is based on the value - e.g. multiples of 5 are higher"
 [v]
 1
 )

(defn -replace-block
 "Replace block a in list bl with block b"
 [bl a b]
 (conj (remove #{a} bl) b))

(defn -new-entry
 "Creates a new memory entry structure"
 ([v] (hash-map :value v :uuid (misc/uuid) :attr (-initial-attr v)))
 ([v o p] (assoc (-new-entry v) :op o :params p)))

(defn -find-block
 "Finds a block in blocks-list bl using its UUID u"
 [bl u]
 (let [root-matches (filter #(= u (:uuid %1)) bl)]
 	(if (nil? root-matches) nil (first root-matches))))

(defn reset
	"Resets the working memory"
	[]
	(do
		(reset! BRICKS '())
		(reset! TARGET nil)
		(reset! BLOCKS '())))

(defn add-brick
	"Adds a single brick to memory"
	([br v] (conj br (-new-entry v)))
 ([v] (reset! BRICKS (add-brick @BRICKS v))))

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
		(let [parent (-find-block blocks par-uuid)
								new-node (assoc parent :params (assoc (:params parent) par-param (-new-entry value op p)))]
								(-replace-block blocks parent new-node)))
 ([par-uuid par-param value op p]
 	(reset! BLOCKS (add-child-block @BLOCKS par-uuid par-param value op p))))

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
	 							 ))
 							))))

(defn add-child-blocks
 "Adds a child to a block in memory bl, by its uuid"
 ([blocks par-uuid par-param value op p]
		(let [new-entry (-new-entry value op p)]
			(map (partial -add-blocktree-entry par-uuid par-param new-entry) blocks)))
 ([par-uuid par-param value op p]
 	(reset! BLOCKS (add-child-blocks @BLOCKS par-uuid par-param value op p))))


;TODO: write graph converter for resulting structures, using PROPER Hofstadter notation!


(set-target 100)
(add-brick 1)
(add-brick 5)
(add-brick 10)
(add-brick 7)

(add-block 50 :times [5 10])
(add-block 70 :times [7 10])
(add-block 35 :times [5 7])


