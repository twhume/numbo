(ns numbo.coderack
	(:require [clojure.tools.logging :as log]
											[numbo.config :as cfg :refer [CONFIG]]
											[numbo.cyto :as cy]
											[numbo.history :as hist]
											[numbo.misc :as misc]
											[numbo.pnet :as pn]))

(def CODERACK (misc/thread-local (atom '())))
(def ITERATIONS (misc/thread-local (atom 1)))

(defn -urg-invert
	"Inverts urgency of codelet c according to configs"
	[c]
	(assoc c :urgency (misc/invert-key :urgency (:URGENCY_HIGH @@CONFIG) (:URGENCY_LOW @@CONFIG) c)))

(def PRIORITY-SAMPLER (misc/thread-local (atom (misc/mk-sampler @CODERACK :urgency))))
(def INV-PRIORITY-SAMPLER (misc/thread-local (atom (misc/mk-sampler2 @CODERACK :urgency identity -urg-invert))))

(defn reset
 "Reset the coderack"
 []
 (do
 	(reset! @CODERACK '())
 	(reset! @ITERATIONS 1)
 	(reset! @PRIORITY-SAMPLER (misc/mk-sampler @CODERACK :urgency))
 	(reset! @INV-PRIORITY-SAMPLER (misc/mk-sampler @CODERACK :urgency identity -urg-invert)))

 	)

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
 	(let [codelet (@@PRIORITY-SAMPLER)]
 	(do
 		(log/info  "Iteration" @@ITERATIONS ":" (:desc codelet) "(of" (count @@CODERACK) "in rack)")
 		(log/debug  "Iteration" @@ITERATIONS "cyto=" @@cy/CYTO)
 	 (if ((complement nil?) codelet) (-execute (:fn codelet)))
 	 (hist/add-step @@pn/PNET @@cy/CYTO @@CODERACK codelet @@ITERATIONS (cy/get-temperature))
 	 (reset! @CODERACK (-remove-codelet @@CODERACK codelet))
 		(swap! @ITERATIONS inc)
 	 ))))

(defn add-codelet
 "Adds a new codelet to the coderack"
 ([r c] (conj r c))
 ([c] (reset! @CODERACK (add-codelet @@CODERACK c))))

(defn decay
 "Decays the coderack - while it's over MAX_SIZE, remove a low-pri element"
 ([r]
 	(loop [cur r]
 		(if (> (count cur) (:CODERACK_SIZE @@CONFIG)) ; if we are too big
	 			(recur (-remove-codelet cur (@@INV-PRIORITY-SAMPLER))) ; cut a low priority codelet
			 		cur)))
 ([] (reset! @CODERACK (decay @@CODERACK))))
