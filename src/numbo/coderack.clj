(ns numbo.coderack
	(:require [numbo.codelet :refer :all]))

(def CODERACK (atom '()))

; What are urgency value ranges?
; Imagine we have a set of nodes in the rack with urgencies 1 1 2 5 10
; The urgency value ranges are 1 2 4 9 19
; (minimum urgency must be 1 rather than 0, so that 0-urgency nodes have a chance of running)
; A random value from 0..19 would be generated to choose one weighted by its urgency

(defn -select-node-for-val
 "Given a sequence of urgency value ranges and nodes, and a value v within the ranges, return a node"
	[urgencies v]
 (second (first (filter #(< v (first %)) urgencies))))

; Take a coderack and return a list of (range, node) tuples
(defn -make-urgencies
 [rack]
 	 (map #(list %1 %2) (reductions + (map :urgency rack)) rack))

(defn -select-next-node
 "Grabs the next codelet from the rack r, probabilistically according to its urgency"
 ([rack]
 	(let [urgencies (-make-urgencies rack)]
 	 (if (not-empty urgencies)
 	  (-select-node-for-val urgencies (rand-int (first (last urgencies))))))))

(defn process-next-node
 "Grabs and executes a codelet from the rack, removing it afterwards"
 ([]
 	(let [node (-select-next-node @CODERACK)]
 	 ; TODO operate the node
 	 (reset! CODERACK (let [[n m] (split-with (partial not= node) @CODERACK)] (concat n (rest m)))))))