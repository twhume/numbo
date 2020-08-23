(ns numbo.core
	(:require [clojure.string :as str]
											[clojure.tools.logging :as log]
											[clojure.tools.cli :refer [parse-opts]]
											[numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.config :as cfg :refer [config]]
											[numbo.cyto :as cy]
											[numbo.history :as hist]
											[numbo.misc :as misc]
											[numbo.pnet :as pn]
											[numbo.viz :as viz]
											[random-seed.core :refer :all])
	(:refer-clojure :exclude [rand rand-int rand-nth]))

(def seed (rand-int Integer/MAX_VALUE))
;(def seed 1572254847)

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

;			(= 0 (mod @cr/ITERATIONS 5)) (do
;				(if (< (rand) (cy/get-temperature)) (cl/rand-block)))

		
			(if	(= 0 (mod @cr/ITERATIONS (:FREQ_DISMANTLE @config))) (do
																																	(if (< (rand) (cy/get-temperature)) (cl/dismantler)))) ; if it's getting too hot, dismantle something

		 (if	(= 0 (mod @cr/ITERATIONS (:FREQ_RAND_BLOCK @config))) (do
																														 		(if (> (rand) (cy/get-temperature)) (cl/rand-block))))

		 (if	(= 0 (mod @cr/ITERATIONS (:FREQ_SEEK_FACSIMILE @config))) (do
																														 		(if (> (rand) (cy/get-temperature)) (cl/seek-facsimile))))

		 (if	(= 0 (mod @cr/ITERATIONS (:FREQ_RAND_TARGET_MATCH @config))) (do
																														 		(if (> (rand) (cy/get-temperature)) (cl/rand-target-match))))

		 (if	(= 0 (mod @cr/ITERATIONS (:FREQ_RAND_SYNTACTIC_COMPARISON @config))) (do
																														 		(if (> (rand) (cy/get-temperature)) (cl/rand-syntactic-comparison))))


;		 (if	(= 0 (mod @cr/ITERATIONS 2)) (do
;																														 		(if (> (rand) (cy/get-temperature)) ((rand-nth (list cl/rand-block cl/seek-facsimile))))))

			; Pump a random brick every few iterations

		 (if	(= 0 (mod @cr/ITERATIONS (:FREQ_PUMP_BRICK @config))) (do 
																																	(if (> (rand) (cy/get-temperature)) ; When it's not so hot, pump a brick target
																																		(let [br (cy/random-brick)]
																																			(if br
																																				(cl/activate-pnet (pn/closest-keyword br)))))))

		; Pump a target (chosen randomly from the primary and secondary targets) 1 in 10 iterations

			(if	(= 0 (mod @cr/ITERATIONS (:FREQ_PUMP_TARGET @config))) (let [t (cy/random-target)]
																																		 (if (not (nil? t))
																																		 	(do
																																		 		(log/debug "tick activating target" t)
																																		 		(cy/pump-node t)
																																			 	(cl/activate-pnet (pn/closest-keyword t))))))

  (cy/decay)
		(pn/decay)
		(cr/decay)
))

(defn run-until
 [pred]
 (loop []
  (do
	 	(cr/process-next-codelet)

	 	(tick)
	 	(if (not (or (pred) @cy/COMPLETE))
	 		 (recur)))))

(defn run-until-empty-cr
 []
 (run-until (fn[] (empty? @cr/CODERACK))))

(defn run-for-iterations
 [n]
 (run-until (fn[] (>= @cr/ITERATIONS n))))

(defn parse-int [s] (Integer/parseInt s))

(def cli-options
[["-i" "--iterations NUM" 
			"Maximum number of iterations to run before declaring failure"
			:default 10000
			:parse-fn #(Integer/parseInt %)
			:validate [#(< 0 %) "Must be a positive number"]]
 ["-c" "--count NUM" 
			"How many times to run each calculation"
			:default 1
			:parse-fn #(Integer/parseInt %)
			:validate [#(< 0 %) "Must be a positive number"]]			
["-t" "--target VALUE"
			"Target value for calculation"
			:parse-fn #(Integer/parseInt %)
			:validate [#(< 0 %) "Must be a positive number"]]
		["-b" "--bricks 1,2,3"
			"Comma-separate list of brick values"
			:parse-fn #(map parse-int (str/split % #","))
			:validate [#(< 0 (count %)) "Must be 1+ bricks"]]
		["-s" "--seed VAL"
			"Seed value for randomness"
			:parse-fn #(Long/parseLong %)]
		["-v" nil "Visualize"
    :id :visualize
    :default 0
    :update-fn inc]
		])


(defn user-error
	[s]
	(do
		(println s)
		(log/error s)))

(defn run-calcs
 "Run max i iterations of a calculation of target t, bricks b, c times, vizualize if v is set"
 [c i t b v]
 (dotimes [n c]

		; re-init everything every time so we can run from the REPL
	(try
		(pn/initialize-pnet)
		(cy/reset)
		(hist/reset)
		(cr/reset)

		(cl/load-target t)
		(doall (map cl/load-brick b))

		(run-for-iterations i)
		(if @cy/COMPLETE
			(println n "," t "," b "," @cr/ITERATIONS "," (cy/format-block (:val (first (cy/get-solutions)))) "," (count (filter empty? (map :coderack @hist/HISTORY))))
			(println n "," t "," b "," @cr/ITERATIONS ", none, " (count (filter empty? (map :coderack @hist/HISTORY)))))

	 (if v (viz/-main))
		(catch Exception e
		 (do
				(log/error "Caught " e)
				(println e)
				(dump)
)))))

(defn -main
  [& args]
  (let [argv (parse-opts *command-line-args* cli-options)
  						opts (:options argv)]
  	(cond
  		(nil? (:target opts)) (user-error "No target specified")
  		(nil? (:bricks opts)) (user-error "No bricks specified")
  		(and
  			(= 1 (:visualize opts))
  			(< 1 (:count opts))) (user-error "Visualizing over >1 rounds")

  		:else (run-calcs (:count opts) (:iterations opts) (:target opts) (:bricks opts) (= 1 (:visualize opts))))))