(ns numbo.pnet
	(:require [clojure.tools.logging :as log]
											[numbo.misc :as misc]))

; Pnet is a map of name -> node, where name is a keyword name of the node, and node is a map:
; :activation - default 0
; :weight - default 0
; :links - each a tuple of destination (keyword name of dest note) and type (keyword from link-types)

(def node-types '(:number :calculation :operator))
(def link-types '(:operator :result :similar :param))
(def operator-map (hash-map :plus + :minus - :times *))
(def operator-name-map (hash-map :plus "+" :minus "-" :times "*"))
(def op-lookups {+ "+" - "-" * "*"})


(def PNET (atom '{}))

(def DEFAULT_DECAY 0.05)
(def DEFAULT_ACTIVATION 0)
(def DEFAULT_INC 0.2)

; Initial values for the Pnet - others (e.g. activation) can be added programmatically

(def initial-pnet '{

	:1 {
		:type :number
		:links (
			(:minus-3-1 :param)
			(:minus-4-1 :param)
			(:minus-5-1 :param)
			(:minus-6-1 :param)
			(:minus-7-1 :param)
			(:minus-8-1 :param)
			(:minus-9-1 :param)
			(:minus-10-1 :param)
			(:minus-50-1 :param)
		 (:plus-1-1 :param)
			(:plus-1-2 :param)
			(:plus-1-3 :param)
			(:plus-1-4 :param)
			(:plus-1-5 :param)
			(:plus-1-6 :param)
			(:plus-1-7 :param)
			(:plus-1-8 :param)
			(:plus-1-9 :param)
		)
	}

	:2 {
		:type :number
		:links (
		 (:plus-1-2 :param)
			(:plus-2-2 :param)
			(:plus-2-3 :param)
			(:plus-2-4 :param)
			(:plus-2-5 :param)
			(:plus-2-6 :param)
			(:plus-2-7 :param)
			(:plus-2-8 :param)
			(:times-2-2 :param)
			(:times-2-3 :param)
			(:times-2-4 :param)
			(:times-2-5 :param)
			(:times-2-10 :param)
			(:times-2-12 :param)
			(:times-2-20 :param)
			(:minus-3-1 :result)
		 (:plus-1-1 :result)
		)
	}

	:3 {
		:type :number
		:links (
		 (:plus-1-3 :param)
			(:plus-2-3 :param)
			(:plus-3-3 :param)
			(:plus-3-4 :param)
			(:plus-3-5 :param)
			(:plus-3-6 :param)
			(:plus-3-7 :param)
			(:times-2-3 :param)
			(:times-3-3 :param)
			(:times-3-10 :param)
			(:times-3-20 :param)
			(:minus-4-1 :result)
		 (:plus-1-2 :result)
		 (:4 :similar)
		)
	}

	:4 {
		:type :number
		:links (
			(:plus-1-4 :param)
			(:plus-2-4 :param)
			(:plus-3-4 :param)
			(:plus-4-4 :param)
			(:plus-4-5 :param)
			(:plus-4-6 :param)
			(:times-2-4 :param)
			(:times-4-4 :param)
			(:times-4-10 :param)
			(:times-4-20 :param)
			(:minus-5-1 :result)
			(:plus-1-3 :result)
			(:plus-2-2 :result)
			(:times-2-2 :result)
		 (:3 :similar)
		 (:5 :similar)
		)
	}

	:5 {
		:type :number
		:links (
			(:plus-1-5 :param)
			(:plus-2-5 :param)
			(:plus-3-5 :param)
			(:plus-4-5 :param)
			(:plus-5-5 :param)
			(:plus-5-10 :param)
			(:times-2-5 :param)
			(:times-5-5 :param)
			(:times-5-6 :param)
			(:times-5-10 :param)
			(:times-5-20 :param)
			(:minus-6-1 :result)
			(:plus-1-4 :result)
			(:plus-2-3 :result)
		 (:4 :similar)
		 (:6 :similar)
		)
	}

	:6 {
		:type :number
		:links (
			(:plus-1-6 :param)
			(:plus-2-6 :param)
			(:plus-3-6 :param)
			(:plus-4-6 :param)
			(:minus-7-1 :result)
			(:times-2-3 :result)
			(:plus-3-3 :result)
			(:plus-2-4 :result)
			(:plus-1-5 :result)
			(:minus-7-1 :result)
			(:times-5-6 :param)
			(:times-6-10 :param)
		 (:5 :similar)
		 (:7 :similar)
		)
	}

	:7 {
		:type :number
		:links (
		 (:6 :similar)
		 (:8 :similar)
			(:minus-8-1 :result)
			(:minus-7-1 :param)
		 )
	}

	:8 {
		:type :number
		:links (
		 (:7 :similar)
		 (:9 :similar)
			(:minus-9-1 :result)
		 )
	}

	:9 {
		:type :number
		:links (
			(:minus-10-1 :result)
			(:times-3-3 :result)
			(:8 :similar)
		)
	}

	:10 {
		:type :number
		:links (
			(:times-2-10 :param)
			(:times-3-10 :param)
			(:times-4-10 :param)
			(:times-5-10 :param)
			(:times-6-10 :param)
			(:times-7-10 :param)
			(:times-8-10 :param)
			(:times-9-10 :param)
			(:times-10-10 :param)
			(:plus-1-9 :result)
			(:plus-2-8 :result)
			(:plus-3-7 :result)
			(:plus-4-6 :result)
			(:plus-5-5 :result)
			(:times-2-5 :result)
			(:9 :similar)
			(:11 :similar)
		)
	}

	:11 {
		:type :number
		:links (
			(:10 :similar)
			(:12 :similar))
	}

	:12 {
		:type :number
		:links (
			(:times-2-6 :result)
			(:times-3-4 :result)
			(:times-2-12 :param)
			(:11 :similar)
			(:15 :similar)
		)
	}

	:15 {
		:type :number
		:links (
			(:plus-7-8 :result)
			(:plus-5-10 :result)
			(:times-3-5 :result)
			(:times-10-15 :param)
			(:12 :similar)
			(:16 :similar)
			(:20 :similar)
		)
	}

	:16 {
		:type :number
		:links (
			(:times-4-4 :result)
			(:15 :similar)
		)
	}

	:20 {
		:type :number
		:links (
			(:times-4-5 :result)
			(:times-2-10 :result)
			(:times-2-20 :param)
			(:times-3-20 :param)
			(:times-4-20 :param)
			(:times-5-20 :param)
			(:15 :similar)
			(:25 :similar)
		)
	}

	:24 {
		:type :number
		:links (
			(:times-2-12 :result)
			(:25 :similar)
		)
	}

	:25 {
		:type :number
		:links (
			(:times-5-5 :result)
			(:times-2-12 :similar)
			(:20 :similar)
			(:24 :similar)
			(:30 :similar)
		)
	}

	:30 {
		:type :number
		:links (
			(:times-3-10 :result)
			(:25 :similar)
			(:40 :similar)
		)
	}

	:40 {
		:type :number
		:links (
			(:times-4-10 :result)
			(:times-2-20 :result)
			(:30 :similar)
			(:50 :similar)
		)
	}

	:49 {
		:type :number
		:links (
			(:minus-50-1 :result)
			(:times-7-7 :result)
			(:50 :similar)
		)
	}

	:50 {
		:type :number
		:links (
			(:times-5-10 :result)
			(:40 :similar)
			(:49 :similar)
			(:60 :similar)
		)
	}

	:60 {
		:type :number
		:links (
			(:times-6-10 :result)
			(:times-3-20 :result)
			(:50 :similar)
			(:70 :similar)
		)
	}

	:70 {
		:type :number
		:links (
			(:times-7-10 :result)
			(:60 :similar)
			(:80 :similar)
		)
	}

	:80 {
		:type :number
		:links (
			(:times-4-20 :result)
			(:times-8-10 :result)
			(:70 :similar)
			(:81 :similar)
			(:90 :similar)
		)
	}

	:81 {
		:type :number
		:links (
			(:times-9-9 :result)
			(:80 :similar)
		)
	}

	:90 {
		:type :number
		:links (
			(:times-9-10 :result)
			(:80 :similar)
			(:100 :similar)
		)
	}

	:100 {
		:type :number
		:links (
			(:times-5-20 :result)
			(:times-10-10 :result)
			(:90 :similar)
			(:150 :similar)
		)
	}

	:150 {
		:type :number
		:links (
			(:times-10-15 :result)
			(:100 :similar)
		)
	}


	:minus-3-1 {
		:type :calculation
		:links (
			(:3 :param)
			(:1 :param)
			(:2 :result)
			(:minus :operator)
		)
	}

	:minus-4-1 {
		:type :calculation
		:links (
			(:4 :param)
			(:1 :param)
			(:3 :result)
			(:minus :operator)
		)
	}

	:minus-5-1 {
		:type :calculation
		:links (
			(:5 :param)
			(:1 :param)
			(:4 :result)
			(:minus :operator)
		)
	}

	:minus-6-1 {
		:type :calculation
		:links (
			(:6 :param)
			(:1 :param)
			(:5 :result)
			(:minus :operator)
		)
	}

	:minus-7-1 {
		:type :calculation
		:links (
			(:7 :param)
			(:1 :param)
			(:6 :result)
			(:minus :operator)
		)
	}

	:minus-8-1 {
		:type :calculation
		:links (
			(:8 :param)
			(:1 :param)
			(:7 :result)
			(:minus :operator)
		)
	}

	:minus-9-1 {
		:type :calculation
		:links (
			(:9 :param)
			(:1 :param)
			(:8 :result)
			(:minus :operator)
		)
	}

	:minus-10-1 {
		:type :calculation
		:links (
			(:10 :param)
			(:1 :param)
			(:9 :result)
			(:minus :operator)
		)
	}

	:minus-50-1 {
		:type :calculation
		:links (
			(:50 :param)
			(:1 :param)
			(:49 :result)
			(:minus :operator)
		)
	}

	:plus-1-1 {
		:type :calculation
		:links (
			(:1 :param)
			(:1 :param)
			(:2 :result)
			(:plus :operator)
		)
	}

	:plus-1-2 {
		:type :calculation
		:links (
			(:1 :param)
			(:2 :param)
			(:3 :result)				
			(:plus :operator)
	 )
	}

	:plus-1-3 {
		:type :calculation
		:links (
			(:1 :param)
			(:3 :param)
			(:4 :result)
			(:plus :operator)
		)
	}

	:plus-1-4 {
		:type :calculation
		:links (
			(:1 :param)
			(:4 :param)
			(:5 :result)
			(:plus :operator)
		)
	}

	:plus-1-5 {
		:type :calculation
		:links (
			(:1 :param)
			(:5 :param)
			(:6 :result)
			(:plus :operator)
		)
	}

	:plus-1-6 {
		:type :calculation
		:links (
			(:1 :param)
			(:6 :param)
			(:7 :result)
			(:plus :operator)
		)
	}

	:plus-1-7 {
		:type :calculation
		:links (
			(:1 :param)
			(:7 :param)
			(:8 :result)
			(:plus :operator)
		)
	}

	:plus-1-8 {
		:type :calculation
		:links (
			(:1 :param)
			(:8 :param)
			(:9 :result)
			(:plus :operator)
		)
	}

	:plus-1-9 {
		:type :calculation
		:links (
			(:1 :param)
			(:9 :param)
			(:10 :result)
			(:plus :operator)
		)
	}

	:plus-2-2 {
		:type :calculation
		:links (
			(:2 :param)
			(:2 :param)
			(:4 :result)
			(:plus :operator)
	 )
	}

	:plus-2-3 {
		:type :calculation
		:links (
			(:2 :param)
			(:3 :param)
			(:5 :result)				
			(:plus :operator)
		)
	}

	:plus-2-4 {
		:type :calculation
		:links (
			(:2 :param)
			(:4 :param)
			(:6 :result)				
			(:plus :operator)
		)
	}

	:plus-2-5 {
		:type :calculation
		:links (
			(:2 :param)
			(:5 :param)
			(:7 :result)				
			(:plus :operator)
		)
	}

	:plus-2-6 {
		:type :calculation
		:links (
			(:2 :param)
			(:6 :param)
			(:8 :result)				
			(:plus :operator)
		)
	}

	:plus-2-7 {
		:type :calculation
		:links (
			(:2 :param)
			(:7 :param)
			(:9 :result)				
			(:plus :operator)
		)
	}

	:plus-2-8 {
		:type :calculation
		:links (
			(:2 :param)
			(:8 :param)
			(:10 :result)				
			(:plus :operator)
		)
	}

	:plus-3-3 {
		:type :calculation
		:links (
			(:3 :param)
			(:3 :param)
			(:6 :result)
			(:plus :operator)
		)
	}

	:plus-3-4 {
		:type :calculation
		:links (
			(:3 :param)
			(:4 :param)
			(:7 :result)
			(:plus :operator)
		)
	}

	:plus-3-5 {
		:type :calculation
		:links (
			(:3 :param)
			(:5 :param)
			(:8 :result)
			(:plus :operator)
		)
	}

	:plus-3-6 {
		:type :calculation
		:links (
			(:3 :param)
			(:6 :param)
			(:9 :result)
			(:plus :operator)
		)
	}

	:plus-3-7 {
		:type :calculation
		:links (
			(:3 :param)
			(:7 :param)
			(:10 :result)
			(:plus :operator)
		)
	}

	:plus-4-4 {
		:type :calculation
		:links (
			(:4 :param)
			(:4 :param)
			(:8 :result)
			(:plus :operator)
		)
	}

	:plus-4-5 {
		:type :calculation
		:links (
			(:4 :param)
			(:5 :param)
			(:9 :result)
			(:plus :operator)
		)
	}

	:plus-4-6 {
		:type :calculation
		:links (
			(:4 :param)
			(:6 :param)
			(:10 :result)
			(:plus :operator)
		)
	}

	:plus-5-5 {
		:type :calculation
		:links (
			(:5 :param)
			(:5 :param)
			(:10 :result)
			(:plus :operator)
		)
	}

	:plus-5-10 {
		:type :calculation
		:links (
			(:5 :param)
			(:10 :param)
			(:15 :result)
			(:plus :operator)
		)
	}

	:plus-7-8 {
		:type :calculation
		:links (
			(:7 :param)
			(:8 :param)
			(:15 :result)
			(:plus :operator)
		)
	}

	:times-2-2 {
		:type :calculation
		:links (
			(:2 :param)
			(:2 :param)
			(:4 :result)
			(:times :operator)
		)
	}

	:times-2-3 {
		:type :calculation
		:links (
			(:2 :param)
			(:3 :param)
			(:6 :result)
			(:times :operator)
		)
	}

	:times-2-4 {
		:type :calculation
		:links (
			(:2 :param)
			(:4 :param)
			(:8 :result)
			(:times :operator)
		)
	}

	:times-2-5 {
		:type :calculation
		:links (
			(:2 :param)
			(:5 :param)
			(:10 :result)
			(:times :operator)
		)
	}

	:times-2-6 {
		:type :calculation
		:links (
			(:2 :param)
			(:6 :param)
			(:12 :result)
			(:times :operator)
		)
	}

	:times-2-10 {
		:type :calculation
		:links (
			(:2 :param)
			(:10 :param)
			(:20 :result)
			(:times :operator)
		)
	}

	:times-2-12 {
		:type :calculation
		:links (
			(:2 :param)
			(:12 :param)
			(:times :operator)
			(:24 :result)
		)
	}

	:times-2-20 {
		:type :calculation
		:links (
			(:2 :param)
			(:20 :param)
			(:40 :result)
			(:times :operator)
  )
	}

	:times-3-3 {
		:type :calculation
		:links (
			(:3 :param)
			(:3 :param)
			(:9 :result)
			(:times :operator)
  )
	}

	:times-3-4 {
		:type :calculation
		:links (
			(:3 :param)
			(:4 :param)
			(:12 :result)
			(:times :operator)
  )
	}

	:times-3-5 {
		:type :calculation
		:links (
			(:3 :param)
			(:5 :param)
			(:15 :result)
			(:times :operator)
  )
	}

	:times-3-10 {
		:type :calculation
		:links (
			(:3 :param)
			(:10 :param)
			(:30 :result)
			(:times :operator)
		)
	}

	:times-3-20 {
		:type :calculation
		:links (
			(:3 :param)
			(:20 :param)
			(:60 :result)
			(:times :operator)
  )
	}

	:times-4-4 {
		:type :calculation
		:links (
			(:4 :param)
			(:4 :param)
			(:16 :result)
			(:times :operator)
  )
	}

	:times-4-5 {
		:type :calculation
		:links (
			(:4 :param)
			(:5 :param)
			(:20 :result)
			(:times :operator)
  )
	}

	:times-4-10 {
		:type :calculation
		:links (
			(:4 :param)
			(:10 :param)
			(:40 :result)
			(:times :operator)
  )
	}

	:times-4-20 {
		:type :calculation
		:links (
			(:4 :param)
			(:20 :param)
			(:80 :result)
			(:times :operator)
  )
	}

	:times-5-5 {
		:type :calculation
		:links (
			(:5 :param)
			(:5 :param)
			(:25 :result)
			(:times :operator)
  )
	}

	:times-5-6 {
		:type :calculation
		:links (
			(:5 :param)
			(:6 :param)
			(:30 :result)
			(:times :operator)
  )
	}

	:times-5-10 {
		:type :calculation
		:links (
			(:5 :param)
			(:10 :param)
			(:50 :result)
			(:times :operator)
  )
	}

	:times-5-20 {
		:type :calculation
		:links (
			(:5 :param)
			(:20 :param)
			(:100 :result)
			(:times :operator)
  )
	}

	:times-6-10 {
		:type :calculation
		:links (
			(:6 :param)
			(:10 :param)
			(:60 :result)
			(:times :operator)
  )
	}

	:times-7-7 {
		:type :calculation
		:links (
			(:7 :param)
			(:7 :param)
			(:49 :result)
			(:times :operator)
  )
	}

	:times-7-10 {
		:type :calculation
		:links (
			(:7 :param)
			(:10 :param)
			(:70 :result)
			(:times :operator)
  )
	}

	:times-8-10 {
		:type :calculation
		:links (
			(:8 :param)
			(:10 :param)
			(:80 :result)
			(:times :operator)
  )
	}

	:times-9-9 {
		:type :calculation
		:links (
			(:9 :param)
			(:9 :param)
			(:81 :result)
			(:times :operator)
  )
	}

	:times-9-10 {
		:type :calculation
		:links (
			(:9 :param)
			(:10 :param)
			(:90 :result)
			(:times :operator)
  )
	}

	:times-10-10 {
		:type :calculation
		:links (
			(:10 :param)
			(:10 :param)
			(:100 :result)
			(:times :operator)
  )
	}

	:times-10-15 {
		:type :calculation
		:links (
			(:10 :param)
			(:15 :param)
			(:150 :result)
			(:times :operator)
  )
	}

	:plus {
		:type :operator
	}

	:times {
		:type :operator
	}

	:minus {
		:type :operator
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

(defn get-operators
 "Returns a list of all valid operators for the Pnet"
 []
 (keys operator-map))

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
								(not-every? (set node-types) used-node-types) (do (log/warn "Bad node types:" (remove (set node-types) used-node-types)) false)
								(not-every? (set link-types) used-types) (do (log/warn "Bad link types:" (remove (set link-types) used-types)) false)
								(not-every? (set all-nodes) used-links) (do (log/warn "Unknown nodes referenced in links:" (remove (set all-nodes) used-links)) false)
								(not= (count numbers) (count (filter #(= :number (:type %)) (vals p)))) (do (log/warn "Not every :number node resolves to a number") false)
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
	 (let [weights-and-activations (-update-values pnet (fn [x] (assoc x :activation DEFAULT_ACTIVATION :weight 1)))]
	 	(apply assoc '{} (mapcat #(list %1 (assoc (get weights-and-activations %1) :name %1)) (keys weights-and-activations)))))
 ([] (reset! PNET (initialize-pnet initial-pnet))))

(defn -get-neighbors
 "Return the neighbors of node n in pnet n"
	[p n]
	(map first (:links (get p n))))

(defn -update-activation
 "Update the activation of node n by an increment i"
 [i n]
 (update n :activation (fn [x] (misc/normalized (+ (* i DEFAULT_INC) x)))))

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
	 (update n (partial -update-activation 5))
  (-map-values neighbors (partial -update-activation 3))
  (-map-values neighbors-2 (partial -update-activation 1))
 )))
 ([n] (reset! PNET (activate-node @PNET n))))


(defn -get-random-by-type
	"Get a random node, sampled probabilistically by activation from all nodes of :type t"
 [p t]
	(let [op-range (misc/make-percent-ranges (filter #(= t (:type %)) (vals p)) :activation)]
	 (if (not-empty op-range)
	  (misc/random-val-in-range op-range))))

(defn get-random-op
	"Get a random operator, sampled probabilistically by activation"
 ([p] (-get-random-by-type p :operator))
 ([] (get-random-op @PNET)))

(defn get-random-calc
	"Get a random calculation, sampled probabilistically by activation"
 ([p] (-get-random-by-type p :calculation))
 ([] (get-random-calc @PNET)))

(defn filter-links-for 
 [l t]
 (map first (filter #(= t (second %)) l)))

(defn format-calc
 "Turn a calculation into a string"
 [c]
 (let [l (:links c)]
	 (str (misc/int-k (first (filter-links-for l :param))) 
	 	(get operator-name-map (first (filter-links-for l :operator)))
	 	(misc/int-k (second (filter-links-for l :param)))
	 	"="
	 	(misc/int-k (first (filter-links-for l :result)))
 	)))

(defn get-similar
 "Get a list of nodes which have :similar links from n"
 ([p n] (map first (filter #(= :similar (second %1)) (:links (get p n)))))
 ([n] (get-similar @PNET n)))
; Used in graphing

(defn get-link-type
 "Return the type of link going from node names n1 to n2 in pnet p"
 [p n1 n2]
 (let [src (get p n1)
 						link (first (filter #(= n2 (first %1))(:links src)))]
 						(second link)))

(defn decay
 "Reduce the :activation of all nodes in the PNet"
 ([pn] (-update-values pn (fn [x] (assoc x :activation (misc/normalized (:activation x) (- 0 DEFAULT_DECAY))))))
 ([] (reset! PNET (decay @PNET))))


(defn closest-keyword
 "Return the keyword of a Pnet node with the closest value to v"
	[v] 
	(keyword (str (misc/closest (get-numbers) v))))
