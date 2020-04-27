(ns numbo.wm2
	(:require [rhizome.viz :as rh])
	(:require [clojure.zip :as zip]))

; Playing with new working memory implementation

(def MEMORY (atom (zip/seq-zip '())))

(defn -get-nodes
	"Return a sequence of all the zip locations in the memory"
	[m]
	(filter (complement zip/branch?) (take-while (complement zip/end?) (iterate zip/next m))))

(defn -new-node
 "Eventually will create a new node. Right now just takes an int"
 [v]
 v)

(defn get-vals
	"Return a sequence of all the items in memory"
 [m]
 (map zip/node (-get-nodes m)))

(defn find-node
 "Returns the loc of a given node in a memory"
 [mem node]
 (filter #(= node (zip/node %1)) (-get-nodes mem)))

; Puts it in the set of children below the root - i.e. top level nodes

(defn add-target
 "Adds a target to the memory"
	([mem value] (zip/insert-child mem (-new-node value)))
 ([value] (reset! MEMORY (add-brick @MEMORY value))))

; Same as add-target right now
; TODO fix it so it passes in the right type
; TODO also make sure these share code

(defn add-brick
 "Adds a brick to the memory"
	([mem value] (zip/insert-child mem (-new-node value)))
 ([value] (reset! MEMORY (add-brick @MEMORY value))))


(defn -to-graph
 "Convert a working memory structure into a graph for Rhizome"
 [m]
 (into '{}
 	(map #(vector (zip/node %)
 		(if (zip/branch? %)
 			(map zip/node (zip/children %))
 			'[])) (-get-nodes m))))

(defn view-graph
 "Show the graph for a memory structure m"
 [m]
 (let [g (-to-graph m)]
	 (rh/view-graph (keys g) g
	 	:directed? false
 	 :options {:concentrate true}
 		:node->descriptor (fn [n]
 			{:label n})

 )))

; change to use data structures for nodes, instead of ints
; fix graph
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
