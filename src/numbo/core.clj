(ns numbo.core
	(:require [clojure.string :as str]
											[clojure.tools.logging :as log]
											[clojure.tools.cli :refer [parse-opts]]
											[numbo.coderack :as cr]
											[numbo.codelet :as cl]
											[numbo.config :as cfg :refer [CONFIG]]
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
		(log/debug "ITERATION=" @@cr/ITERATIONS)
		(log/debug "CYTO=" @@cy/CYTO)
		(log/debug "PNET=" @@pn/PNET)
		(log/debug "CODERACK=" @@cr/CODERACK)
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
		(log/debug "tick, CR=" (count @@cr/CODERACK) ",temp=" (cy/get-temperature))

;			(= 0 (mod @cr/ITERATIONS 5)) (do
;				(if (< (rand) (cy/get-temperature)) (cl/rand-block)))

		
			(if	(= 0 (mod @@cr/ITERATIONS (:FREQ_DISMANTLE @@CONFIG))) (do
																																	(if (< (rand) (cy/get-temperature)) (cl/dismantler)))) ; if it's getting too hot, dismantle something

		 (if	(= 0 (mod @@cr/ITERATIONS (:FREQ_RAND_BLOCK @@CONFIG))) (do
																														 		(if (> (rand) (cy/get-temperature)) (cl/rand-block))))

  	(if (= 0 (mod @@cr/ITERATIONS (:FREQ_SEEK_FACSIMILE @@CONFIG))) (do
																																	(if (> (rand) (cy/get-temperature))	(cl/seek-facsimile))))
 
		 (if	(= 0 (mod @@cr/ITERATIONS (:FREQ_RAND_TARGET_MATCH @@CONFIG))) (do
																														 		(if (> (rand) (cy/get-temperature)) (cl/rand-target-match))))

		 (if	(= 0 (mod @@cr/ITERATIONS (:FREQ_RAND_SYNTACTIC_COMPARISON @@CONFIG))) (do
																														 		(if (> (rand) (cy/get-temperature)) (cl/rand-syntactic-comparison))))


;		 (if	(= 0 (mod @cr/ITERATIONS 2)) (do
;																														 		(if (> (rand) (cy/get-temperature)) ((rand-nth (list cl/rand-block cl/seek-facsimile))))))

			; Pump a random brick every few iterations

		 (if	(= 0 (mod @@cr/ITERATIONS (:FREQ_PUMP_BRICK @@CONFIG))) (do 
																																	(if (> (rand) (cy/get-temperature)) ; When it's not so hot, pump a brick target
																																		(let [br (cy/random-brick)]
																																			(if br
																																				(cl/activate-pnet (pn/closest-keyword br)))))))

		; Pump a target (chosen randomly from the primary and secondary targets) 1 in 10 iterations

			(if	(= 0 (mod @@cr/ITERATIONS (:FREQ_PUMP_TARGET @@CONFIG))) (let [t (cy/random-target)]
																																		 (if (not (nil? t))
																																		 	(do
																																		 		(log/debug "tick activating target" t)
																																		 		(cl/pump-node t)
																																			 	(cl/activate-pnet (pn/closest-keyword t))
																																			 	(cl/seek-facsimile)
																																			 	))))

  (cy/decay)
		(pn/decay)
		(cr/decay)
))

(defn run-until
 [pred]
 (try
	 (loop []
	  (do
		 	(cr/process-next-codelet)

		 	(tick)
		 	(if (or (pred) @@cy/COMPLETE)
		 			[@@cr/ITERATIONS (if (empty? (cy/get-solutions)) nil (cy/format-block (:val (first (cy/get-solutions)))))]
		 		 (recur))))
			(catch Exception e
				 (do
						(log/error "Caught " e)
						(println @cr/CODERACK)
						(println e)
						(dump)
						[@@cr/ITERATIONS nil]
						))))
	
(defn run-until-empty-cr
 []
 (run-until (fn[] (empty? @@cr/CODERACK))))

(defn run-for-iterations
 [n]
 (run-until (fn[] (>= @@cr/ITERATIONS n))))

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
 "Run max i iterations of a calculation of target t, bricks b, c times, vizualize if v is set, debug if d = true"
 [c i t b v d]
 (loop [n 0 iterations 0 solutions '[]]

 	(if (= n c)
 		[
 			(int (/ iterations c)) ; average number of iterations
 			(* 100 (misc/round-to 2 (/ (- c (count (filter nil? solutions))) c))) ; %age of puzzles with a solution found
 			(count (distinct (filter #(not (nil? %1)) solutions))) ; # distinct solutions
				t ; target we are going for (just to make debugging easier)
 		]

 	(do
				; re-init everything every time so we can run from the REPL
				(pn/initialize-pnet)
				(cy/reset)
				(hist/reset)
				(cr/reset)

				(cl/load-target t)
				(doall (map cl/load-brick b))

				(let [result (run-for-iterations i)]
					(if d
						(if @@cy/COMPLETE
							(println n "," t "," b "," @@cr/ITERATIONS "," (cl/-format-block (:val (first (cy/get-solutions)))) "," (count (filter empty? (map :coderack @@hist/HISTORY))))
							(println n "," t "," b "," @@cr/ITERATIONS ", none, " (count (filter empty? (map :coderack @@hist/HISTORY))))))

				 (if v (viz/-main))
				 (recur (inc n) (+ iterations (first result)) (conj solutions (second result)))
)))))

(def problems
'((114 (11,20,7,1,6))
 (87 (8,3,9,10,7))
 (31 (3,5,24,3,14))
 (25 (8,5,5,11,2))
 (81 (9,7,2,25,18))
 (6 (3,3,17,11,22))
 (11 (2,5,1,25,23))
 (116 (20,2,16,14,6))
 (127 (7,6,4,22,25))
 (41 (5,16,22,25,1))))

(defn -average [coll] 
  (misc/round-to 2 (/ (reduce + coll) (count coll))))

(defn -transpose [coll]
   (apply map vector coll))

(defn run-for-config
 [c]
 (do
 	(reset! @cfg/CONFIG c)
 	(let [results (pmap #(run-calcs 100 1000 (first %1) (second %1) false false) problems) ; run against each problem 100 times and collect results
 							averages (map -average (-transpose results))]
 							(take 3 averages))))

(defn run-for-urgencies
 [u]
 (do
 	(reset! @cfg/URGENCIES u)
 	(let [results (pmap #(run-calcs 100 1000 (first %1) (second %1) false false) problems) ; run against each problem 100 times and collect results
 							averages (map -average (-transpose results))]
 							(take 3 averages))))


(defn -unchunk [s]
  (when (seq s)
    (lazy-seq
      (cons (first s)
            (-unchunk (next s))))))

(defn run-epoch
 ""
[start-cfg start-percent max-it]

; take the first 10 configurations which deliver a greater accuracy than the current

	(loop [cfg start-cfg percent start-percent it 0]
		(if (= max-it it)
			(do (println "End of iterations") cfg)
			(do
				(println "Iteration=" it "percent=" percent "cfg=" cfg)
				(let [results (first
						 (reverse
						 	(sort-by #(second (second %1))
						 	 (take 5
								 	(filter #(> (second (second %1)) percent)
								 		(pmap #(let [result (run-for-config %1)] (do (println (second result)) (list %1 result))) (-unchunk (repeatedly 500 (partial cfg/evolve-config cfg)))))))))]
				(if (nil? results)
					(do (println "Couldn't find a better child") cfg)
					(recur
					 (first results)
					 (second (second results))
					 (inc it)
					 )))))))

(defn run-epoch-urgencies
 ""
[start-urg start-percent max-it]

; take the first 10 configurations which deliver a greater accuracy than the current

	(loop [urg start-urg percent start-percent it 0]
		(if (= max-it it)
			(do (println "End of iterations") urg)
			(do
				(println "Iteration=" it "percent=" percent "urgencies=" urg)
				(let [results (first
						 (reverse
						 	(sort-by #(second (second %1))
						 	 (take 5
								 	(filter #(> (second (second %1)) percent)
								 		(pmap #(let [result (run-for-urgencies %1)] (do (println (second result)) (list %1 result))) (-unchunk (repeatedly 500 (partial cfg/evolve-urgencies urg)))))))))]
				(if (nil? results)
					(do (println "Couldn't find a better child") urg)
					(recur
					 (first results)
					 (second (second results))
					 (inc it)
					 )))))))

(defn -do-task
 ""
 [n]
 (do
 	(println "start n=" n)
 	(Thread/sleep (+ 3000 (rand 5000)))
 	(println "end n=" n)
))


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

  		:else (run-calcs (:count opts) (:iterations opts) (:target opts) (:bricks opts) (= 1 (:visualize opts)) true))))