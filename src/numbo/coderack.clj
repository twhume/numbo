(ns numbo.coderack
	(:require [clojure.tools.logging :as log]
											[numbo.config :as cfg :refer [config]]
											[numbo.cyto :as cy]
											[numbo.history :as hist]
											[numbo.misc :as misc]
											[numbo.pnet :as pn]))

(def CODERACK (atom '()))
(def ITERATIONS (atom 1))

(defn reset
 "Reset the coderack"
 []
 (do
 	(reset! CODERACK '())
 	(reset! ITERATIONS 1)))

(defn -select-next-codelet
 "Grabs the next codelet from the rack r, probabilistically according to its urgency"
 ([rack] (first (misc/sample rack :urgency))))

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
 (assoc c :urgency (- (+ (:URGENCY_HIGH @config) (:URGENCY_LOW @config)) (:urgency c))))

(defn decay
 "Decays the coderack - while it's over MAX_SIZE, remove a low-pri element"
 ([r]
 	(loop [cur r]
 		(if (> (count cur) (:CODERACK_SIZE @config))
 			(recur 
			 	(-remove-codelet cur
			 	 (-invert-urgency
				 	 (first (misc/sample (map -invert-urgency cur) :urgency)))))
			 		cur)))
 ([] (reset! CODERACK (decay @CODERACK))))
