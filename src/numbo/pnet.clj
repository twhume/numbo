(ns numbo.pnet)

; Pnet is a map of name -> node, where name is a keyword name of the node, and node is a map:
; :activation - default 0
; :weight - default 0
; :links - each a tuple of destination (keyword name of dest note) and type (keyword from link-types)


(def link-types '(:operand :result :similar))

; Initial values for the Pnet - others (e.g. activation) can be added programmatically
; TODO: I don't understand why so many of the links in the plus- ones are :results and not :operands
(def initial-pnet '{

	:1 {
		:links (
		 (:plus-1-1 :result)
			(:plus-1-2 :result)
			(:plus-1-3 :result))
	}

	:2 {
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
			:links (
				(:plus-1-2 :result)
				(:plus-2-2 :result)
				(:times-2-2 :result)
			 (:3 :similar)
			 (:5 :similar)
			)
	}

	:5 {
			:links (
				(:plus-2-3 :result)
			 (:4 :similar)
			 (:6 :similar)
			)
	}

	:6 {
			:links (
				(:times-2-3 :result)
			 (:5 :similar)
			 (:7 :similar)
			)
	}

	:7 {
			:links (
			 (:6 :similar)
			 (:8 :similar)
			 )
	}

	:8 {
			:links (
			 (:7 :similar)
			 (:9 :similar)
			 )
	}

	:9 {
			:links (
				(:times-3-3 :result)
				(:8 :similar)
			)
	}


	:plus-1-1 {
			:links (
				(:1 :result)
				(:2 :result)
			)
	}

	:plus-1-2 {
			:links (
				(:1 :result)
				(:2 :result)
				(:3 :result)				
		 )
	}

	:plus-1-3 {
			:links (
				(:1 :result)
				(:3 :result)
				(:4 :result)				
			)
	}

	:plus-2-2 {
			:links (
				(:2 :result)
				(:4 :result)
		 )
	}

	:plus-2-3 {
			:links (
				(:2 :result)
				(:3 :result)
				(:5 :result)				
		)
	}

	:plus-3-3 {
			:links (
				(:3 :result)
				(:6 :result)
			)
	}

	:times-2-2 {
			:links (
				(:2 :operand)
				(:4 :result)
			)
	}

	:times-2-3 {
			:links (
				(:2 :operand)
				(:3 :operand)
				(:6 :result)
			)
	}

	:times-3-3 {
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

(defn validate-pnet
 "true if this is a valid pnet, false with printed error messages if not"
[p]
	(let [all-nodes (keys p)
							all-links (apply concat (map #(:links (second %)) p))
							used-types (distinct (map second all-links))
							used-links (distinct (map first all-links))]

							(cond
								(not-every? (set link-types) used-types) (do (println "Bad link types:" (remove (set link-types) used-types)) false)
								(not-every? (set all-nodes) used-links) (do (println "Unknown nodes referenced in links:" (remove (set all-nodes) used-links)) false)
								:else true								
							)))

(defn -update-values
	"Apply function f to all values in the map m"
	[m f]
 (into {} (for [[k v] m] [k (f v)])))

; Initialization means
; setting activation on all nodes to 0
; setting weight to 1
; adding a :name value equal to the key for each node - useful when debugging

(defn initialize-pnet
 "Fill in the default values"
 [pnet]
 (let [weights-and-activations (-update-values pnet (fn [x] (assoc x :activation 0 :weight 1)))]
 	(apply assoc '{} (mapcat #(list %1 (assoc (get weights-and-activations %1) :name %1)) (keys weights-and-activations)))
 ))

; TODO make functions to act on a pnet - e.g. to activate a node and have its activation spread

(defn -get-neighbors
 "Return the neighbors of node n in pnet n"
	[p n]
	(map first (:links (get p n))))

; TODO might be nicer if this took a sequence and we always passed in a sequence - would let us use iterate()

(defn -update-weight
 "Update the weight of node n by a factor f"
 [f n]
 (update n :weight (partial * f)))

(defn -map-values
	[m keys f & args]
	(reduce #(apply update-in %1 [%2] f args) m keys))

; Used in unit tests

(defn -find-with-weight
 "Return a sequence of all nodes in pnet p with weight w"
 [p w]
	(filter (fn [[k v]] (= w (:weight v))) p))

; Implements decaying preading activation in a pnet
; Double the weight of the main node, +50% of neighbors, +10% of their neighbors
; All arbitrary factors atm

(defn activate-node
 "Activate a node n in a pnet p"
 [p n]
 (let [neighbors (-get-neighbors p n)
 						node-and-neighbors (set (conj neighbors n))
 						neighbors-2 (remove node-and-neighbors (distinct (mapcat (partial -get-neighbors p) neighbors)))]
 (-> p
	 (update n (partial -update-weight 2))
  (-map-values neighbors (partial -update-weight 1.5))
  (-map-values neighbors-2 (partial -update-weight 1.1))
 )))

