(ns numbo.core
	(:require [clojure.tools.logging :as log]
											[numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.history :as hist]
											[numbo.misc :as misc]
											[numbo.working :as wm]
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
		(log/debug "BLOCKS=" @wm/BLOCKS)
		(log/debug "BRICKS=" @wm/BRICKS)
		(log/debug "TARGET=" @wm/TARGET)
		(log/debug "TARGET2=" @wm/TARGET2)
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
;			(= 0 (mod @cr/ITERATIONS 5)) (do
;				(if (< (rand) (wm/get-temperature)) (cl/rand-block)))

			(= 0 (mod @cr/ITERATIONS 4)) (do
																														 		(if (> (rand) (wm/get-temperature)) (cl/rand-block))
																																	(if (> (rand) (wm/get-temperature)) (cl/rand-syntactic-comparison))
																																	(if (> (rand) (wm/get-temperature)) (cl/seek-facsimile)))

			; Pump a random brick every few iterations

			(= 0 (mod @cr/ITERATIONS 3)) (do 
																																	(if (< (rand) (wm/get-temperature)) (cl/dismantler)) ; if it's getting too hot, dismantle something
																																	(if (> (rand) (wm/get-temperature)) ; When it's not so hot, pump a brick target
																																		(let [br (wm/get-random-brick false)]
																																			(if br
																																				(cl/activate-pnet (keyword (str (misc/closest (pn/get-numbers) (:value br)))))))))

		; Pump a target (chosen randomly from the primary and secondary targets) 1 in 10 iterations

			(= 0 (mod @cr/ITERATIONS 10)) (do 
																																		(if (not (nil? @wm/TARGET))
																																			(let [uuids (conj @wm/TARGET2 (:uuid @wm/TARGET))
																																									rand-uuid (rand-nth (seq uuids))
																																									[rand-entry rand-src] (wm/find-anywhere rand-uuid)
																																									pn-node (keyword (str (misc/closest (pn/get-numbers) (:value rand-entry))))]
																																			(log/debug "tick activating pnet for" pn-node rand-src)
																																			(cl/activate-pnet pn-node))))

			)


		

  (wm/decay)
		(pn/decay)
		(wm/flush-target2)

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


	(run-for-iterations 5000)

	(viz/-main)
	(catch Exception e
	 (do
			(log/error "Caught " e)
			(dump)
)))))
