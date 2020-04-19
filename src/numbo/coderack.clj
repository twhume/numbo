(ns numbo.coderack
	(:require [numbo.misc :as misc]))

(def CODERACK (atom '()))
(def ITERATIONS (atom 0))

(defn -select-next-codelet
 "Grabs the next codelet from the rack r, probabilistically according to its urgency"
 ([rack]
 	(let [urgencies (misc/make-ranges rack :urgency)]
 	 (if (not-empty urgencies)
 	  (misc/random-val-in-range urgencies)))))

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
