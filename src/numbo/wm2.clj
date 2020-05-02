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
;
; Bricks also have:
; :status - tracks whether a brick is :free or :taken
;
; Blocks also have
; :iam - whether this block is the param1 or param2 of its parent - nil if it has no parent
; :param1 - their first parameter
; :param2 - their second parameter
; :op - the operator, one of :plus :times :minus
;
; Internal values needed for graphing:
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

; By default new :brick entries are status :free, all entries have an initial attractiveness,
; :blocks have details of their parameters

(defn -new-entry
 "Creates a new data structure for a memory entry, type t value v"
 ([t v p1 p2 op iam]
 (let [initial { :type t :value v :attract (-initial-attract v) :uuid (misc/uuid) }]
	 (case t
	 	:brick (assoc initial :status :free)
	 	:block (assoc initial :param1 p1 :param2 p2 :op op :iam iam)
	 	initial
 )))
 ([t v p1 p2 op] (-new-entry t v nil nil nil nil))
 ([t v] (-new-entry t v nil nil nil nil)))


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


(defn add-block
 "Adds a new block to the memory, with op param1 param2 assuming it's off the root node"
 ([mem v p1 p2 op] (zip/insert-child mem (-new-entry :block v p1 p2 op)))
 ([v p1 p2 op] (reset! MEMORY (add-block @MEMORY v p1 p2 op))))

(defn add-block-as-child
 "Adds a new block to the memory under loc, with k = param1 or param2, with op param1 param2"
 ([mem loc k v p1 p2 op] (zip/insert-child loc (-new-entry :block v p1 p2 op k)))
 ([loc k v p1 p2 op] (reset! MEMORY (add-block-as-child @MEMORY loc k v p1 p2 op))))


; ----- Below here, functions will end up in viz.clj -----

; When we're plotting the child node of a node can be either another node, referenced by its UUID,
; or a set of values (for a :block). To create a coherent plot we have to give these values virtual
; UUIDs, of the form PARENT-UUID_param1, PARENT-UUID_param2, PARENT-UUID_op

(defn -mk-virt-uuid
 "Makes a string for a virtual UUID from entry e, given keyword k"
 [e k]
 (str (:uuid e) "_" (name k)))

; Later we will need to find these UUIDs, so make a filter for them

(defn is-virt-uuid?
 "Is the supplied string a virtual UUID?"
	[s]
	(cond
		(.endsWith s "_param1") true
		(.endsWith s "_param2") true
		(.endsWith s "_op") true
		:else false
))

(defn get-virt-uuid
 "Returns the UUID part of a virtual node UUID"
 [u]
 (clojure.string/replace u #"_.*$" ""))

(defn get-virt-param
 "Returns the param part of a virtual node UUID"
 [u]
 (clojure.string/replace u #"^.*_" ""))

; There's 3 cases we need to handle here:
; 1. Where the children of a node are another node (e.g. a block may have one of its params be another block)
; 2. Where the children of a node are the parameters of a block 
; 3. The mixed case, where one child is a node, another is a parameter

(defn -list-child-uuids
 "Given a node n, return a list of UUIDs of its children, if any"
 [n]
 (let [e (zip/node n)]
	 (cond
	  (zip/branch? n) (map #(:uuid (zip/node %)) (zip/children n))
		 (= :block (:type e)) (vector (-mk-virt-uuid e :param1) (-mk-virt-uuid e :param2) (-mk-virt-uuid e :op)) 			
	 	:else '[])))

(defn -to-graph
 "Convert a working memory structure into a graph for Rhizome"
 [m]
 (let [initial-tree (map #(vector (:uuid (zip/node %)) (-list-child-uuids %)) (-get-nodes m))
 						virtual-nodes (map #(vector %1 '[]) (filter is-virt-uuid? (mapcat second initial-tree)))]

 	(into '{} (concat initial-tree virtual-nodes))))
 	
; add root nodes for all the MAGIC child IDs

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
 		 (if (is-virt-uuid? u)
					(let [n (zip/node (find-node m (get-virt-uuid u)))
											k (keyword (get-virt-param u))]
		 			{:label (k n)
		 			 :style "filled,solid"
		 			 :fixedsize false
		 			 :width 0.5
		 			 :height 0.25
		 			 :fontsize 10
		 			 :fillcolor (-attractiveness-to-color (:attract n))})


	 			(let [n (zip/node (find-node m u))]
		 			{:label (:value n)
		 			 :style (str "filled," (-get-wm-style (:type n)))
		 			 :fillcolor (-attractiveness-to-color (:attract n))})
 )))))

; add blocks with children which are blocks
; add-block() method with a child node as target (check the node type to ensure it's valid!)
; add blocks with children which are a mix of blocks and ints

;get-bricks (free?)
;get-root-blocks
;get-target
;get-secondary-targets
;get-blocks


(add-brick 1)
(add-brick 2)
(add-brick 3)
(add-block 20 2 10 :times)
(def block-uuid (:uuid (first (filter #(= :block (:type %)) (get-vals @MEMORY)))))
(def block-node (find-node @MEMORY block-uuid))
(println (zip/node block-node))
(println (zip/make-node block-node (zip/node block-node) '[]))


; I suspect that find-node is returning only a node and not the entire memory structure, i.e. not a loc;
; which stops us adding to it


;(add-block-as-child block-node :param2 10 2 5 :times)

;(add-target 20)

