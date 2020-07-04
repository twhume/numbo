(ns numbo.core
	(:require [clojure.tools.logging :as log]
											[numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.cyto :as cy]
											[numbo.history :as hist]
											[numbo.misc :as misc]
											[numbo.pnet :as pn]
											[numbo.viz :as viz]
											[random-seed.core :refer :all])
	(:refer-clojure :exclude [rand rand-int rand-nth]))

(def seed (rand-int Integer/MAX_VALUE))
(def seed 1572254847)

(log/info "Starting with random seed" seed)
(set-random-seed! seed)

(defn dump
	"Dump state to console"
	[]
	(do
		(log/debug "ITERATION=" @cr/ITERATIONS)
		(log/debug "CYTO=" @cy/CYTO)
		(log/debug "PNET=" @pn/PNET)
		(log/debug "CODERACK=" @cr/CODERACK)
	))

; Tick is called every n iterations and takes charge of starting random tasks. Each tick:
; - there is a temp% chance that a rand-block codelet gets added to the coderack
; - there is a temp% chance that a rand-syntactic-comparison codelet gets added to the coderack
; - attraction of all nodes in cytoplasm decays
; - activation of all nodes in PNet decays

(defn tick
	"Schedule random regular tasks"
	[]
	(do
		(log/debug "tick")

		(cond
;			(= 0 (mod @cr/ITERATIONS 5)) (do
;				(if (< (rand) (cy/get-temperature)) (cl/rand-block)))

			(= 0 (mod @cr/ITERATIONS 1)) (do
																																	(if (< (rand) (cy/get-temperature)) (cl/dismantler)) ; if it's getting too hot, dismantle something
																														 		(if (> (rand) (cy/get-temperature)) ((rand-nth (list cl/rand-block cl/rand-syntactic-comparison cl/seek-facsimile)))))

			; Pump a random brick every few iterations

			(= 0 (mod @cr/ITERATIONS 2)) (do 
																																	(if (> (rand) (cy/get-temperature)) ; When it's not so hot, pump a brick target
																																		(let [br (cy/random-brick)]
																																			(if br
																																				(cl/activate-pnet (pn/closest-keyword br))))))

		; Pump a target (chosen randomly from the primary and secondary targets) 1 in 10 iterations

			(= 0 (mod @cr/ITERATIONS 1)) (do 
																																		(let [t (cy/random-target)]
																																		 (if ((complement nil?) t)
																																		 	(do
																																		 		(log/debug "tick activating target" t)
																																			 	(cl/activate-pnet (pn/closest-keyword t)))))))

  (cy/decay)
		(pn/decay)
))

(defn run-until
 [pred]
 (loop []
  (do
	 	(cr/process-next-codelet)

	 	(tick)
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
	(cy/reset)
	(hist/reset)
	(cr/reset)

	(cl/load-target 114)

	(cl/load-brick 11)
	(cl/load-brick 20)
	(cl/load-brick 1)
	(cl/load-brick 6)
	(cl/load-brick 7)


	(run-for-iterations 5000)

	(viz/-main)
	(catch Exception e
	 (do
			(log/error "Caught " e)
			(dump)
)))))
