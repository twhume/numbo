(ns numbo.coderack
	(:require [clojure.tools.logging :as log]
											[numbo.misc :as misc]
											[numbo.history :as hist]
											[numbo.pnet :as pn]
											[numbo.working :as wm]))

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
 ([rack]
 	(let [urgencies (misc/make-ranges rack :urgency)]
 	 (if (not-empty urgencies)
 	  (misc/random-val-in-range urgencies)))))

(defn -execute
 "Executes the function f, warning and doing nothing if f is nil"
 [f]
 (if f (f) (log/warn "Warning: skipping nil function")))

(defn process-next-codelet
 "Grabs and executes a codelet from the rack, removing it afterwards"
 ([]
 	(let [codelet (-select-next-codelet @CODERACK)]
 	(do
 	 (if ((complement nil?) codelet) (-execute (:fn codelet)))
 	 (hist/add-step @pn/PNET @wm/TARGET @wm/BRICKS @wm/BLOCKS @CODERACK codelet @ITERATIONS (wm/get-temperature))
 	 (reset! CODERACK (let [[n m] (split-with (partial not= codelet) @CODERACK)] (concat n (rest m))))
 		(swap! ITERATIONS inc)
 	 ))))

(defn add-codelet
 "Adds a new codelet to the coderack"
 ([r c] (conj r c))
 ([c] (reset! CODERACK (add-codelet @CODERACK c))))
