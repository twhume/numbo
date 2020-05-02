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

; BRICKS is a list of Entries, TARGET is an Entry
; Entries are maps with a :value, a random :uuid and an :attr(activeness)
;
; Entries in BLOCKS can also have an :op(erator) and a vector of 2 children, each ints or block entries
; (so Entries in BLOCKS are each "BlockTrees")

; ----- Private functions -----

(defn -initial-attr
 "Default attractiveness is based on the value - e.g. multiples of 5 are higher"
 [v]
 1)

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
	([br v] (conj br (assoc (-new-entry v) :free true)))
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

;----- all this should go into viz.clj eventually -----

(defn -is-virt-uuid?
 "Is the supplied string a virtual UUID?"
	[s]
	(cond
		(.endsWith s "_param0") true
		(.endsWith s "_param1") true
		(.endsWith s "_op") true
		:else false
))

(defn -get-virt-uuid
 "Returns the UUID part of a virtual node UUID"
 [u]
 (clojure.string/replace u #"_.*$" ""))

(defn -get-virt-param
 "Returns the param part of a virtual node UUID"
 [u]
 (clojure.string/replace u #"^.*_" ""))

(defn -node-to-graph
	"Converts the node at a zipper loc to its Rhizome representation"
	[loc]
	(let [node (zip/node loc)]
		(list (:uuid node) (vector (str (:uuid node) "_op") (:params node))))
)

; Either the UUIDs of sub-blocks or "virtual UUIDs" referring to params

(defn -block-children
	"Returns vector of UUID representations of node n"
	[n]
	(let [children (:params n)
							uuid (:uuid n)]
		(vec (map-indexed #(if (int? %2) (str uuid "_param" %1) (:uuid %2)) children))))

(defn -blocktree-to-graph
	"Convert a single blocktree into a Rhizome-format graph"
	[bt]
	(let [zipper (-make-blocktree-zipper bt)]
		(loop [cur zipper out '{}]
		 (if (zip/end? cur) out
		 	(let [node (zip/node cur)
		 							node-uuid (:uuid node)
		 							node-op-uuid (str node-uuid "_op")
		 							node-children (-block-children node)
		 							]
			  	(recur
			  		(zip/next cur)
			  		(do
			  			(if (int? node) out
					  	(assoc out
					  		node-uuid (vector node-op-uuid)
					  		node-op-uuid node-children
					  		(first node-children) '[]
					  		(second node-children) '[]
			  		)))))))))

(defn -to-graph
 "Convert a target, bricks and blocks into a graph for Rhizome"
 [ta br bl]
 (let [bt-graphs (map -blocktree-to-graph bl)]
 	(apply merge bt-graphs)))

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

(def -op-names '{ :times "X" :plus "+" :minus "-"})

(defn -get-node-label
 "Given the UUID u of a block in the list of blocks bl, return a pair of its [label,type]"
	[bl u]

	(let [node-uuid (if (-is-virt-uuid? u) (-get-virt-uuid u) u)
							entry (zip/node (first (filter (complement nil?) (map #(-find-blocktree-loc (-make-blocktree-zipper %) node-uuid) bl))))]
							(if (-is-virt-uuid? u)
								(condp = (-get-virt-param u)
									"op" [((:op entry) -op-names) :op]
									"param0" [(first (:params entry)) :param]
									"param1" [(second (:params entry)) :param]
									"ERROR")
							[(:value entry) :result])))

(defn view-graph
 "Show the graph for the working memory target, bricks and blocks"
 ([ta br bl]
 (let [g (-to-graph ta br bl)]
	 (rh/view-graph (keys g) g
	 	:directed? false
 	 :options {:concentrate true :layout "neato" :mode "hier" :model "circuit"}
 		:node->descriptor (fn [u]

 		  (let [[label type] (-get-node-label bl u)]
 		  		(condp = type
 		  		 :op (hash-map :label label :style "rounded,filled" :fontcolor "white" :fontsize 12 :fixedsize "true" :labelloc "c" :width 0.4 :height 0.4 :color "black" )
 		  		 :param (hash-map :label label :style "solid" :fontcolor "black" :fontsize 12 :fixedsize "false" :labelloc "c" :width 0.4 :height 0.4 :color "red" )
 		  		 :result (hash-map :label label :style "rounded,filled" :fontcolor "white" :fontsize 12 :fixedsize "false" :labelloc "c" :width 0.4 :height 0.4 :color "black" )
 		  		 :else (println "WEIRD TYPE" type)))))))
 ([] (view-graph @TARGET @BRICKS @BLOCKS)))


;TODO: write unit tests
;TODO: also graph TARGET and BRICKS
;TODO: integrate and replace existing WM implementation

(set-target 100)
(add-brick 1)
(add-brick 5)
(add-brick 10)
(add-brick 7)

(add-block 50 :times [5 10])
(add-block 70 :plus [7 10])
(def u (:uuid (first (add-block 35 :times [5 7]))))
(add-child-block u 1 99 :minus [3 33])


