(ns numbo.config)

(def config (atom '{

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


}))

; Map of type --> urgency for codelets




(def urgencies (atom {
	:activate-pnet (:URGENCY_MEDIUM @config)
	:inc-attraction (:URGENCY_MEDIUM @config)
	:rand-syntactic-comparison (:URGENCY_LOW @config)
	:check-done (:URGENCY_HIGH @config)
	:fulfil-target2 (:URGENCY_HIGH @config)
	:rand-target-match (:URGENCY_MEDIUM @config)
	:load-target (:URGENCY_HIGH @config)
	:create-target2 (:URGENCY_HIGH @config)
	:probe-target2 (:URGENCY_HIGH @config)
	:load-brick (:URGENCY_HIGH @config)
	:test-block (:URGENCY_HIGH @config)
	:seek-facsimile (:URGENCY_MEDIUM @config)
	:rand-block (:URGENCY_LOW @config)
	:dismantler (:URGENCY_LOW @config)
	}))