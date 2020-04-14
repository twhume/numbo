(ns numbo.core
	(:require [numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.working :as wm]
											[numbo.pnet :as pn]))

(defn run-until
 [pred]
 (loop []
 	(cr/process-next-codelet)
 	(wm/print-state)
 	(if (not (pred)) (recur))))

(defn run-until-empty-cr
 []
 (run-until (fn[] (empty? @cr/CODERACK))))

(defn -main
  []
  (do

(println "Starting")

(pn/initialize-pnet pn/initial-pnet)

(cl/create-node :target 10)
(cl/create-node :brick 1)
(cl/create-node :brick 2)
(cl/create-node :brick 3)
(cl/create-node :brick 2)

(run-until-empty-cr)


(println "done")

))