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

(pn/initialize-pnet)

(cl/create-node :brick 1)
(cr/process-next-codelet)
(cl/create-node :brick 2)
(cr/process-next-codelet)
(cl/create-node :brick 3)
(cr/process-next-codelet)
(cl/create-node :brick 2)
(cr/process-next-codelet)
(cl/load-target 10)

(println @pn/PNET)

(run-until-empty-cr)
(println "-----")
(println @pn/PNET)


(println "done")

))