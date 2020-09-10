(ns numbo.coderack
	(:require [clojure.tools.logging :as log]
											[numbo.config :as cfg :refer [config]]
											[numbo.cyto :as cy]
											[numbo.history :as hist]
											[numbo.misc :as misc]
											[numbo.pnet :as pn]))

(def CODERACK (atom '()))
(def ITERATIONS (atom 1))

(defn -urg-invert
	"Inverts urgency of c according to configs"
	[c]
	(do
		(log/debug "-urg-invert c=" c "high=" (:URGENCY_HIGH @config) "low=" (:URGENCY_LOW @config))
	(misc/invert-key :urgency (:URGENCY_HIGH @config) (:URGENCY_LOW @config) c)))

(def priority-sampler (misc/mk-sampler CODERACK :urgency))
(def inv-priority-sampler (misc/mk-sampler CODERACK identity -urg-invert))

(defn reset
 "Reset the coderack"
 []
 (do
 	(reset! CODERACK '())
 	(reset! ITERATIONS 1)))

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
 	(let [codelet (priority-sampler)]
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

(defn decay
 "Decays the coderack - while it's over MAX_SIZE, remove a low-pri element"
 ([r]
 	(loop [cur r]
 		(if (> (count cur) (:CODERACK_SIZE @config)) ; if we are too big
	 			(recur (-remove-codelet cur (inv-priority-sampler))) ; cut a low priority codelet
			 		cur)))
 ([] (reset! CODERACK (decay @CODERACK))))
