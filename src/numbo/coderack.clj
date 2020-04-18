(ns numbo.coderack)

(def CODERACK (atom '()))
(def ITERATIONS (atom 0))

; What are urgency value ranges?
; Imagine we have a set of codelet in the rack with urgencies 1 1 2 5 10
; The urgency value ranges are 1 2 4 9 19
; (minimum urgency must be 1 rather than 0, so that 0-urgency nodes have a chance of running)
; A random value from 0..19 would be generated to choose one weighted by its urgency

(defn -select-codelet-for-val
 "Given a sequence of urgency value ranges and nodes, and a value v within the ranges, return a node"
	[urgencies v]
 (second (first (filter #(< v (first %)) urgencies))))

; Take a coderack and return a list of (range, node) tuples
(defn -make-urgencies
 [rack]
 	 (map #(list %1 %2) (reductions + (map :urgency rack)) rack))

(defn -select-next-codelet
 "Grabs the next codelet from the rack r, probabilistically according to its urgency"
 ([rack]
 	(let [urgencies (-make-urgencies rack)]
 	 (if (not-empty urgencies)
 	  (-select-codelet-for-val urgencies (rand-int (first (last urgencies))))))))

(defn -execute
 "Executes the function f, warning and doing nothing if f is nil"
 [f]
 (if f (f) (println "Warning: skipping nil function")))

(defn process-next-codelet
 "Grabs and executes a codelet from the rack, removing it afterwards"
 ([]
 	(let [codelet (-select-next-codelet @CODERACK)]
 	(do
 		(swap! ITERATIONS inc)
 	 (-execute (:fn codelet))
 	 (reset! CODERACK (let [[n m] (split-with (partial not= codelet) @CODERACK)] (concat n (rest m))))
 	 (println "Tick" @ITERATIONS)
 	 ))))

(defn add-codelet
 "Adds a new codelet to the coderack"
 ([r c] (conj r c))
 ([c] (reset! CODERACK (add-codelet @CODERACK c))))
