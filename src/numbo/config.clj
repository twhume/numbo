(ns numbo.config
	(:require
											[numbo.misc :as misc]))

(def CONFIG (misc/thread-local (atom '{

; ----- Configuration for core.clj -----

; how often (in iterations) should we dismantle, temperature permitting?
	:FREQ_DISMANTLE 5
; how often (in iterations) should we trigger one of our random ops, temperature permitting?
 :FREQ_RAND_BLOCK 2
 :FREQ_SEEK_FACSIMILE 10
 :FREQ_RAND_TARGET_MATCH 10
 :FREQ_RAND_SYNTACTIC_COMPARISON 10

; how often (in iterations) should we pump a random brick, temperature permitting?
 :FREQ_PUMP_BRICK 10
; how often (in iterations) should we pump a random target, temperature permitting?
 :FREQ_PUMP_TARGET 10

; ----- Configuration for coderack.clj -----

; how large can the coderack get before we start to shrink it down
 :CODERACK_SIZE 30

; ----- Configuration for cyto.clj -----

 :ATTR_INC 0.7
; default amount by which to decay attractiveness of a node, each timestep
 :ATTR_DEC 0.01
; default starting attraction
 :ATTR_DEFAULT 0.4

; ----- Configuration for pnet.clj -----

; How much to decay each iteration
 :PN_DECAY 0.05
; Where to start pnet activation
 :PN_DEFAULT 0.01
; How much to increase by when pumping
 :PN_INC 0.2

; Multiplier for PN_INC for pumped node
 :PN_SELF 5
; Multiplier for PN_INC for neighboring nodes
 :PN_NEIGHBOR 3
; Multiplier for PN_INC for neighbors of neighbors
:PN_NNEIGHBOR 1

; ----- Configuration for codelet.clj -----

:URGENCY_HIGH 5
:URGENCY_MEDIUM 3
:URGENCY_LOW 1

:FUZZY_CLOSEST_TOP 0.7
:FUZZY_CLOSEST_MID 0.9


})))

; Map of type --> urgency for codelets




(def urgencies (atom {
	:activate-pnet (:URGENCY_MEDIUM @@CONFIG)
	:inc-attraction (:URGENCY_MEDIUM @@CONFIG)
	:rand-syntactic-comparison (:URGENCY_LOW @@CONFIG)
	:check-done (:URGENCY_HIGH @@CONFIG)
	:fulfil-target2 (:URGENCY_HIGH @@CONFIG)
	:rand-target-match (:URGENCY_MEDIUM @@CONFIG)
	:load-target (:URGENCY_HIGH @@CONFIG)
	:create-target2 (:URGENCY_HIGH @@CONFIG)
	:probe-target2 (:URGENCY_HIGH @@CONFIG)
	:load-brick (:URGENCY_HIGH @@CONFIG)
	:test-block (:URGENCY_HIGH @@CONFIG)
	:seek-facsimile (:URGENCY_MEDIUM @@CONFIG)
	:rand-block (:URGENCY_LOW @@CONFIG)
	:dismantler (:URGENCY_LOW @@CONFIG)
	}))

(defn -mod-int
 [val floor ceiling min-add max-add]
 (let [modifier (+ min-add (rand-int (- max-add min-add)))
 						new-val (+ modifier val)]
 	(cond
 	 (< new-val floor) floor
 	 (> new-val ceiling) ceiling
 	 :else new-val)))

(defn -mod-float
 [val floor ceiling min-add max-add]
 (let [modifier (+ min-add (rand (- max-add min-add)))
 						new-val (+ modifier val)]
 	(cond
 	 (< new-val floor) floor
 	 (> new-val ceiling) ceiling
 	 :else (/ (Math/round (* new-val 100)) 100))))

(def config-modmap
{

	:FREQ_DISMANTLE {
		:floor 1
		:ceiling 20
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:FREQ_RAND_BLOCK {
		:floor 1
		:ceiling 20
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:FREQ_SEEK_FACSIMILE {
		:floor 1
		:ceiling 20
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:FREQ_RAND_TARGET_MATCH {
		:floor 1
		:ceiling 20
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:FREQ_RAND_SYNTACTIC_COMPARISON {
		:floor 1
		:ceiling 20
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:FREQ_PUMP_BRICK {
		:floor 1
		:ceiling 20
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:FREQ_PUMP_TARGET {
		:floor 1
		:ceiling 20
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:CODERACK_SIZE {
		:floor 5
		:ceiling 50
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:ATTR_INC {
		:floor 0.01
		:ceiling 1.0
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.1
	}

	:ATTR_DEC {
		:floor 0.01
		:ceiling 1.0
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.1
	}

	:ATTR_DEFAULT {
		:floor 0.01
		:ceiling 1.0
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.1
	}

	:PN_DECAY {
		:floor 0.01
		:ceiling 1.0
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.1
	}

	:PN_DEFAULT {
		:floor 0.01
		:ceiling 1.0
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.1
	}

	:PN_INC {
		:floor 0.01
		:ceiling 1.0
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.1
	}

	:PN_SELF {
		:floor 0.01
		:ceiling 10
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.2
	}

	:PN_NEIGHBOR {
		:floor 0.01
		:ceiling 10
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.2
	}

	:PN_NNEIGHBOR {
		:floor 0.01
		:ceiling 10
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.2
	}

	:URGENCY_HIGH {
		:floor 1
		:ceiling 50
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

		:URGENCY_MEDIUM {
		:floor 1
		:ceiling 50
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

		:URGENCY_LOW {
		:floor 1
		:ceiling 50
		:mod-fn -mod-int
		:mod-min 0
		:mod-max 3
	}

	:FUZZY_CLOSEST_TOP {
		:floor 0.1
		:ceiling 0.9
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.1
	}

	:FUZZY_CLOSEST_MID {
		:floor 0.1
		:ceiling 0.9
		:mod-fn -mod-float
		:mod-min 0
		:mod-max 0.1
	}


})

(defn evolve-config
 "Return a sliightly evolved version of the config c"
 [c]
 (loop [ks (keys c) cfg c]
 	(if (empty? ks) cfg
	 	(let [cur-key (first ks)
	 							cur-cfg (cur-key config-modmap)
	 							f (:mod-fn cur-cfg)]
	 			 (if (< (rand) 0.2)
	 					(recur (rest ks) (assoc cfg cur-key (f (cur-key cfg) (:floor cur-cfg) (:ceiling cur-cfg) (:mod-min cur-cfg) (:mod-max cur-cfg))))
	  					(recur (rest ks) cfg))))))



