(ns numbo.core
	(:require [clojure.tools.logging :as log]
											[numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.history :as hist]
											[numbo.misc :as misc]
											[numbo.working :as wm]
											[numbo.pnet :as pn]
											[numbo.viz :as viz]))

(defn dump
	"Dump state to console"
	[]
	(do
		(log/debug "ITERATION=" @cr/ITERATIONS)
		(log/debug "BLOCKS=" @wm/BLOCKS)
		(log/debug "BRICKS=" @wm/BRICKS)
		(log/debug "TARGET=" @wm/TARGET)
		(log/debug "PNET=" @pn/PNET)
		(log/debug "CODERACK=" @cr/CODERACK)
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
		(log/debug "tick")

		(cond
			(= 0 (mod @cr/ITERATIONS 3)) (do
	;		(if (> (rand) (wm/get-temperature)) (cl/rand-block))
				(if (> (rand) (wm/get-temperature)) (cl/rand-syntactic-comparison))
				(if (> (rand) (wm/get-temperature)) (cl/seek-facsimile))
				(if (> (rand) (wm/get-temperature)) (cl/rand-block))
				(if (< (rand) (wm/get-temperature)) (cl/dismantler)))

			(= 0 (mod @cr/ITERATIONS 2)) (do 
				(if (> (rand) (wm/get-temperature)) (cl/activate-pnet (keyword (str (misc/closest (pn/get-numbers) (:value (wm/get-random-brick false)))))))
				(if (and
					(> (rand) 0.9)
					(not (nil? @wm/TARGET)))
						(cl/activate-pnet (keyword (str (misc/closest (pn/get-numbers) (:value @wm/TARGET))))))))

  (wm/decay)
		(pn/decay)

	))

(defn run-until
 [pred]
 (loop []
  (do
	 	(cr/process-next-codelet)

	 	(tick)
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
	(cl/load-brick 1)
	(cl/load-brick 6)
	(cl/load-brick 7)


	(run-for-iterations 250)

	(viz/-main)
	(catch Exception e
	 (do
			(log/error "Caught " e)
			(dump)
)))))
