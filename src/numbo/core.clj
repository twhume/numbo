(ns numbo.core
	(:require [numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.history :as hist]
											[numbo.working :as wm]
											[numbo.pnet :as pn]
											[numbo.viz :as viz]))

(defn run-until
 [pred]
 (loop []
 	(cr/process-next-codelet)
; 	(wm/print-state)
 	(if (not (pred)) (recur))))

(defn run-until-empty-cr
 []
 (run-until (fn[] (empty? @cr/CODERACK))))

(defn -main
  []
  (do

; re-init everything every time so we can run from the REPL

(pn/initialize-pnet)
(wm/reset)
(hist/reset)
(cr/reset)

(cl/load-brick 1)
(cl/load-brick 2)
(cl/load-brick 3)
(cl/load-brick 2)
(cl/load-target 10)


(run-until-empty-cr)
(cl/rand-block)
(run-until-empty-cr)

(viz/-main)
))
