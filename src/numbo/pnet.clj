(ns numbo.pnet)

; Pnet is a map of name -> node, where name is a keyword name of the node, and node is a map:
; :activation - default 0
; :weight - default 0
; :links - each a tuple of destination (keyword name of dest note) and type (keyword from link-types)

(def node-types '(:number :calculation))
(def link-types '(:operand :result :similar))

(def PNET (atom '{}))

; Initial values for the Pnet - others (e.g. activation) can be added programmatically
; TODO: I don't understand why so many of the links in the plus- ones are :results and not :operands
(def initial-pnet '{

	:1 {
		:type :number
		:links (
		 (:plus-1-1 :result)
			(:plus-1-2 :result)
			(:plus-1-3 :result))
	}

	:2 {
		:type :number
		:links (
		 (:plus-1-2 :result)
			(:plus-2-2 :result)
			(:plus-2-3 :result)
			(:times-2-2 :operand)
			(:times-2-3 :operand)
		 (:plus-1-1 :result)
			)
	}

	:3 {
		:type :number
		:links (
		 (:plus-1-3 :result)
			(:plus-2-3 :result)
			(:plus-3-3 :result)
			(:times-2-3 :operand)
			(:times-3-3 :operand)
		 (:plus-1-2 :result)
		 (:4 :similar)
		)
	}

	:4 {
		:type :number
		:links (
			(:plus-1-2 :result)
			(:plus-2-2 :result)
			(:times-2-2 :result)
		 (:3 :similar)
		 (:5 :similar)
		)
	}

	:5 {
		:type :number
		:links (
			(:plus-2-3 :result)
		 (:4 :similar)
		 (:6 :similar)
		)
	}

	:6 {
		:type :number
		:links (
			(:times-2-3 :result)
		 (:5 :similar)
		 (:7 :similar)
		)
	}

	:7 {
		:type :number
		:links (
		 (:6 :similar)
		 (:8 :similar)
		 )
	}

	:8 {
		:type :number
		:links (
		 (:7 :similar)
		 (:9 :similar)
		 )
	}

	:9 {
		:type :number
		:links (
			(:times-3-3 :result)
			(:8 :similar)
		)
	}


	:plus-1-1 {
		:type :calculation
		:links (
			(:1 :result)
			(:2 :result)
		)
	}

	:plus-1-2 {
		:type :calculation
		:links (
			(:1 :result)
			(:2 :result)
			(:3 :result)				
	 )
	}

	:plus-1-3 {
		:type :calculation
		:links (
			(:1 :result)
			(:3 :result)
			(:4 :result)				
		)
	}

	:plus-2-2 {
		:type :calculation
		:links (
			(:2 :result)
			(:4 :result)
	 )
	}

	:plus-2-3 {
		:type :calculation
		:links (
			(:2 :result)
			(:3 :result)
			(:5 :result)				
		)
	}

	:plus-3-3 {
		:type :calculation
		:links (
			(:3 :result)
			(:6 :result)
		)
	}

	:times-2-2 {
		:type :calculation
		:links (
			(:2 :operand)
			(:4 :result)
		)
	}

	:times-2-3 {
		:type :calculation
		:links (
			(:2 :operand)
			(:3 :operand)
			(:6 :result)
		)
	}

	:times-3-3 {
		:type :calculation
		:links (
			(:3 :operand)
			(:9 :result)
  )
	}

	})

; Invalid if:
; - a link type is specified which isn't in all-types
; - nodes are referenced in links but are never defined
; (not yet)- nodes with no connection mentioned (in map or :links)

(defn get-numbers
 "Return all the numbers in the Pnet"
 ([p] (map #(Integer/parseInt (name (:name %))) (filter #(= :number (:type %)) (vals p))))
 ([] (get-numbers @PNET)))

(defn -validate-pnet
 "true if this is a valid initialized pnet, false with printed error messages if not"
[p]
	(let [all-nodes (keys p)
							used-node-types (map #(:type %) (vals p))
							all-links (apply concat (map #(:links (second %)) p))
							used-types (distinct (map second all-links))
							used-links (distinct (map first all-links))
							numbers (get-numbers p) ; just here so it's run on validation
							]
							(cond
								(not-every? (set node-types) used-node-types) (do (println "Bad node types:" (remove (set node-types) used-node-types)) false)
								(not-every? (set link-types) used-types) (do (println "Bad link types:" (remove (set link-types) used-types)) false)
								(not-every? (set all-nodes) used-links) (do (println "Unknown nodes referenced in links:" (remove (set all-nodes) used-links)) false)
								(not= (count numbers) (count (filter #(= :number (:type %)) (vals p)))) (do (println "Not every :number node resolves to a number") false)
									:else true								
							)))

(defn -update-values
	"Apply function f to all values in the map m"
	[m f]
 (into {} (for [[k v] m] [k (f v)])))

; Initialization means
; setting activation on all nodes to 1
; setting weight to 1
; adding a :name value equal to the key for each node - useful when debugging

(defn initialize-pnet
 "Fill in the default values"
 ([pnet]
	 (let [weights-and-activations (-update-values pnet (fn [x] (assoc x :activation 1 :weight 1)))]
	 	(apply assoc '{} (mapcat #(list %1 (assoc (get weights-and-activations %1) :name %1)) (keys weights-and-activations)))))
 ([] (reset! PNET (initialize-pnet initial-pnet))))

; TODO make functions to act on a pnet - e.g. to activate a node and have its activation spread

(defn -get-neighbors
 "Return the neighbors of node n in pnet n"
	[p n]
	(map first (:links (get p n))))

; TODO might be nicer if this took a sequence and we always passed in a sequence - would let us use iterate()

(defn -update-activation
 "Update the activation of node n by a factor f"
 [f n]
 (update n :activation (partial * f)))

(defn -map-values
	[m keys f & args]
	(reduce #(apply update-in %1 [%2] f args) m keys))

; Used in unit tests

(defn -find-with-activation
 "Return a sequence of all nodes in pnet p with activation a"
 [p a]
	(filter (fn [[k v]] (= a (:activation v))) p))

(defn -set-with-activation
 "Return a set of node names with value v from pnet p"
 [p v]
		(into #{} (map key (-find-with-activation p v))))

; Implements decaying preading activation in a pnet
; Double the weight of the main node, +50% of neighbors, +10% of their neighbors
; All arbitrary factors atm

(defn activate-node
 "Activate a node n in a pnet p"
 ([p n]
 (let [neighbors (-get-neighbors p n)
 						node-and-neighbors (set (conj neighbors n))
 						neighbors-2 (remove node-and-neighbors (distinct (mapcat (partial -get-neighbors p) neighbors)))]
 (-> p
	 (update n (partial -update-activation 2))
  (-map-values neighbors (partial -update-activation 1.5))
  (-map-values neighbors-2 (partial -update-activation 1.1))
 )))
 ([n] (reset! PNET (activate-node @PNET n))))
;TODO this will need to update based not just on activation but also weight

