(ns numbo.history)

; Encapsulates a run of Numbojure
; A sequence of steps, each of which is a map of
; :pn PNet at the end of this step
; :wm Working Memory at the end of this step
; :cr Coderack at the end of this step
; :cl Codelet run in this step

(def HISTORY (atom '[]))

(defn add-step
 ([h p w r l]
 	(conj h (hash-map :pn p :wm w :cr r :cl l)))
 ([p w r l]
 	(reset! HISTORY (add-step @HISTORY p w r l))))

