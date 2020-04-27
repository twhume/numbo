(ns numbo.wm2
 (:require [clojure.zip :as zip])
 (:require [numbo.misc :as misc])
	(:require [rhizome.viz :as rh]))

; Zipper-based working memory implementation;
; once you add blocks in the graph complexity justifies this.
;
; An entry in memory is a map, keys are:
;
; :type - :brick, :target, :secondary (target) or :block
; :value - the value of a brick, target or secondary-target. For a block, same as its result
; :attract - the attractiveness of this entry
; :status - tracks whether a brick is :free or :taken
;
; :uuid - an internally-used UUID. We need these because we may have bricks or blocks
; with the same value that need to be graphed differently
;


(def MEMORY (atom (zip/seq-zip '())))

(defn -get-nodes
	"Return a sequence of all the zip locations in the memory"
	[m]
	(filter (complement zip/branch?) (take-while (complement zip/end?) (iterate zip/next m))))

(defn -initial-attract
 "Give values an initial attractiveness"
 [v]
 1
 )

; By default new :brick entries are status :free, all entries have an initial attractiveness

(defn -new-entry
 "Creates a new data structure for a memory entry, type t value v"
 [t v]
 { :type t :value v :attract (-initial-attract v) :status :free :uuid (misc/uuid) })

(defn get-vals
	"Return a sequence of all the items in memory"
 [m]
 (map zip/node (-get-nodes m)))

(defn find-node
 "Returns the loc of a given uuid u in a memory"
 [mem u]
 (first (filter #(= u (:uuid (zip/node %1))) (-get-nodes mem))))

; Puts it in the set of children below the root - i.e. top level nodes

(defn add-target
 "Adds a target to the memory"
	([mem value] (zip/insert-child mem (-new-entry :target value)))
 ([value] (reset! MEMORY (add-brick @MEMORY value))))

; Same as add-target right now
; TODO fix it so it passes in the right type
; TODO also make sure these share code

(defn add-brick
 "Adds a brick to the memory"
	([mem value] (zip/insert-child mem (-new-entry :brick value)))
 ([value] (reset! MEMORY (add-brick @MEMORY value))))


; ----- Below here, functions will end up in viz.clj -----

(defn -list-child-uuids
 "Given a node n, return a list of UUIDs of its children, if any"
 [n]
 (if (zip/branch? n)
 			(map #(:uuid (zip/node %)) (zip/children n))
 			'[]))

(defn -to-graph
 "Convert a working memory structure into a graph for Rhizome"
 [m]
 (into '{}
 	(map #(vector (:uuid (zip/node %)) (-list-child-uuids %)) (-get-nodes m))))

(defn -attractiveness-to-color
	"Handles coloring nodes by attractiveness"
	[a]
	(let [r (int ( * 255 (float (/ (- 3 a) 3))))
							g (- 255 r)
							b (- 255 r)]
		(format "#FF%02X%02X" r r)))

(defn -get-wm-style
 "Works out appropriate style for a type t"
 [t]
 (case t
 	:target "bold"
 	:secondary "bold"
 	:brick "solid"
 	:block "dashed"
 ))

(defn view-graph
 "Show the graph for a memory structure m"
 [m]
 (let [g (-to-graph m)]
	 (rh/view-graph (keys g) g
	 	:directed? false
 	 :options {:concentrate true}
 		:node->descriptor (fn [u]
 			(let [n (zip/node (find-node m u))]
	 			{:label (:value n)
	 			 :style (str "filled," (-get-wm-style (:type n)))
	 			 :fillcolor (-attractiveness-to-color (:attract n))})
 ))))

; add blocks with children which are ints
; add-block() method with root node as target
; add blocks with children which are blocks
; add-block() method with a child node as target (check the node type to ensure it's valid!)
; add blocks with children which are a mix of blocks and ints

;get-bricks (free?)
;get-root-blocks
;get-target
;get-secondary-targets
;get-blocks

; Create a graph from a simple case
; make it more complex
; add methods to manipulate it
; ensure graph still works

(add-brick 1)
(add-brick 2)
(add-brick 3)

