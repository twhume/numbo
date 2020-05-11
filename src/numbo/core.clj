(ns numbo.core
	(:require [numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.history :as hist]
											[numbo.working :as wm]
											[numbo.pnet :as pn]
											[numbo.viz :as viz]))

; Tick is called every n iterations and takes charge of starting random tasks. Each tick:
; - there is a temp% chance that a rand-block codelet gets added to the coderack
; - there is a temp% chance that a rand-syntactic-comparison codelet gets added to the coderack
; - attraction of all nodes in WM decays
; - activation of all nodes in PNet decays

(defn tick
	"Schedule random regular tasks"
	[]
	(do
		(println "TICK")
		(if (> (rand) (wm/get-temperature)) (cl/rand-block))
		(if (> (rand) (wm/get-temperature)) (cl/rand-syntactic-comparison))
  (wm/decay)
		(pn/decay)
	))

(defn run-until
 [pred]
 (loop []
  (do
	 	(cr/process-next-codelet)

	 	(if (= 0 (mod @cr/ITERATIONS 5)) (tick))
	; 	(wm/print-state)
	 	(if (not (pred)) (recur)))))

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
(cl/rand-syntactic-comparison)
(run-until-empty-cr)
(cl/rand-block)
(cl/rand-syntactic-comparison)
(run-until-empty-cr)
(cl/rand-block)
(cl/rand-syntactic-comparison)
(run-until-empty-cr)
(cl/rand-block)
(cl/rand-syntactic-comparison)
(run-until-empty-cr)

(viz/-main)
))
