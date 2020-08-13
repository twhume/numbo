(ns numbo.config)

(def config (atom '{

; ----- Configuration for core.clj -----

; how often (in iterations) should we dismantle, temperature permitting?
	:FREQ_DISMANTLE 5
; how often (in iterations) should we trigger one of our random ops, temperature permitting?
 :FREQ_RAND_OP 6
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
 :PN_DEFAULT 0.0
; How much to increase by when pumping
 :PN_INC 0.2

; Multiplier for PN_INC for pumped node
 :PN_SELF 5
; Multiplier for PN_INC for neighboring nodes
 :PN_NEIGHBOR 3
; Multiplier for PN_INC for neighbors of neighbors
:PN_NNEIGHBOR 1}))