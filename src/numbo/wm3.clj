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
; Entries in BLOCKS can also have an :op(erator) and 2 children, each themselves block entries
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
 ([v o p1 p2] (assoc (-new-entry v) :op o :param1 p1 :param2 p2)))

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
	([bl value op param1 param2] (conj bl (-new-entry value op param1 param2)))
	([value op param1 param2] (reset! BLOCKS (add-block @BLOCKS value op param1 param2))))

(defn add-child-block
 "Adds a child to a block in memory bl, by its uuid"
 [blocks par-uuid par-param value op param1 param2]
 (let [parent (-find-block blocks par-uuid)
 						new-node (assoc parent par-param (-new-entry value op param1 param2))]
 						(-replace-block blocks parent new-node)))


(set-target 100)
(add-brick 1)
(add-brick 5)
(add-brick 10)
(add-brick 7)

(add-block 50 :times 5 10)
(add-block 70 :times 7 10)
(add-block 35 :times 5 7)



(def blocks '(1 (2 (4) (5)) (3 (6) (7))))

