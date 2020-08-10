(ns numbo.config)

; ----- Configuration for core.clj -----

; how often (in iterations) should we dismantle, temperature permitting?
(def FREQ_DISMANTLE 5) 
; how often (in iterations) should we trigger one of our random ops, temperature permitting?
(def FREQ_RAND_OP 6)
; how often (in iterations) should we pump a random brick, temperature permitting?
(def FREQ_PUMP_BRICK 10)
; how often (in iterations) should we pump a random target, temperature permitting?
(def FREQ_PUMP_TARGET 10)

; ----- Configuration for coderack.clj -----

; how large can the coderack get before we start to shrink it down
(def CODERACK_SIZE 30)

; ----- Configuration for cyto.clj -----

(def ATTR_INC 0.7)
; default amount by which to decay attractiveness of a node, each timestep
(def ATTR_DEC 0.01)
; default starting attraction
(def ATTR_DEFAULT 0.4)

; ----- Configuration for pnet.clj -----

; How much to decay each iteration
(def PN_DECAY 0.05)
; Where to start pnet activation
(def PN_DEFAULT 0.0)
; How much to increase by when pumping
(def PN_INC 0.2)

; Multiplier for PN_INC for pumped node
(def PN_SELF 5)
; Multiplier for PN_INC for neighboring nodes
(def PN_NEIGHBOR 3)
; Multiplier for PN_INC for neighbors of neighbors
(def PN_NNEIGHBOR 1)