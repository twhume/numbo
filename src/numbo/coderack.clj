(ns numbo.coderack
	(:require [clojure.tools.logging :as log]
											[numbo.misc :as misc]
											[numbo.history :as hist]
											[numbo.pnet :as pn]
											[numbo.cyto :as cy]))

(def CODERACK (atom '()))
(def ITERATIONS (atom 1))
(def MAX_SIZE 30)

(defn reset
 "Reset the coderack"
 []
 (do
 	(reset! CODERACK '())
 	(reset! ITERATIONS 1)))

(defn -select-next-codelet
 "Grabs the next codelet from the rack r, probabilistically according to its urgency"
 ([rack]
 	(let [urgencies (misc/make-ranges rack :urgency)]
 	 (if (not-empty urgencies)
 	  (misc/random-val-in-range urgencies)))))

(defn -remove-codelet
	"Returns rack r without codelet c"
	[r c]
	(let [[n m] (split-with (partial not= c) r)]
		(concat n (rest m))))

(defn -execute
 "Executes the function f, warning and doing nothing if f is nil"
 [f]
 (if f (f) (log/warn "Warning: skipping nil function")))

(defn process-next-codelet
 "Grabs and executes a codelet from the rack, removing it afterwards"
 ([]
 	(let [codelet (-select-next-codelet @CODERACK)]
 	(do
 		(log/info  "Iteration" @ITERATIONS ":" (:desc codelet))
 		(log/debug  "Iteration" @ITERATIONS "cyto=" @cy/CYTO)
 	 (if ((complement nil?) codelet) (-execute (:fn codelet)))
 	 (hist/add-step @pn/PNET @cy/CYTO @CODERACK codelet @ITERATIONS (cy/get-temperature))
 	 (reset! CODERACK (-remove-codelet @CODERACK codelet))
 		(swap! ITERATIONS inc)
 	 ))))

(defn add-codelet
 "Adds a new codelet to the coderack"
 ([r c] (conj r c))
 ([c] (reset! CODERACK (add-codelet @CODERACK c))))

(defn -invert-urgency
 "Invert the urgency value of the passed codelet"
 [c]
 (assoc c :urgency (- 6 (:urgency c)))) ; DIRTY CONSTANT TODO REMOVE

(defn decay
 "Decays the coderack - if it's over MAX_SIZE, remove a low-pri element"
 ([r]
	 (if (> (count r) MAX_SIZE)
	 	(-remove-codelet r
	 	 (-invert-urgency
		 	 (misc/random-val-in-range
		 	 	(misc/make-ranges
						 (map -invert-urgency r)
		 	 	 :urgency)))) r))
 ([] (reset! CODERACK (decay @CODERACK))))
