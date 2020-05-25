(ns numbo.core
	(:require [numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.history :as hist]
											[numbo.working :as wm]
											[numbo.pnet :as pn]
											[numbo.viz :as viz]))

(defn dump
	"Dump state to console"
	[]
	(do
		(println "ITERATION=" @cr/ITERATIONS)
		(println "BLOCKS=" @wm/BLOCKS)
		(println "BRICKS=" @wm/BRICKS)
		(println "TARGET=" @wm/TARGET)
		(println "PNET=" @pn/PNET)
		(println "CODERACK=" @cr/CODERACK)
	))

; Tick is called every n iterations and takes charge of starting random tasks. Each tick:
; - there is a temp% chance that a rand-block codelet gets added to the coderack
; - there is a temp% chance that a rand-syntactic-comparison codelet gets added to the coderack
; - attraction of all nodes in WM decays
; - activation of all nodes in PNet decays

(defn tick
	"Schedule random regular tasks"
	[]
	(do
;		(if (> (rand) (wm/get-temperature)) (cl/rand-block))
;		(if (> (rand) (wm/get-temperature)) (cl/rand-syntactic-comparison))
		(if (> (rand) (wm/get-temperature)) (cl/seek-facsimile))
  (wm/decay)
		(pn/decay)
	))

(defn run-until
 [pred]
 (loop []
  (do
	 	(cr/process-next-codelet)

	 	(if (= 0 (mod @cr/ITERATIONS 1)) (tick))
	; 	(wm/print-state)
	 	(if (not (pred)) (recur)))))

(defn run-until-empty-cr
 []
 (run-until (fn[] (empty? @cr/CODERACK))))

(defn run-for-iterations
 [n]
 (run-until (fn[] (>= @cr/ITERATIONS n))))


(defn -main
  []
  (do

; re-init everything every time so we can run from the REPL
(try
	(pn/initialize-pnet)
	(wm/reset)
	(hist/reset)
	(cr/reset)

	(cl/load-target 114)

	(cl/load-brick 11)
	(cl/load-brick 20)
	(cl/load-brick 7)
	(cl/load-brick 1)
	(cl/load-brick 6)


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
	(run-for-iterations 100)

	(viz/-main)
	(catch Exception e
	 (do
			(println "Caught " e)
			(dump)
)))))
