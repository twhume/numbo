(ns numbo.history)

; Encapsulates a run of Numbojure
; A sequence of steps, each of which is a map of
; :pnet PNet at the end of this step
; :working Working Memory at the end of this step
; :coderack Coderack at the end of this step
; :codelet Codelet run in this step
; :iteration number of the step

(def HISTORY (atom '[]))

(defn reset 
 "Restart history"
 []
 (reset! HISTORY '[]))

(defn add-step
 ([h p w r l i]
 	(conj h (hash-map :pnet p :working w :coderack r :codelet l :iteration i)))
 ([p w r l i]
 	(reset! HISTORY (add-step @HISTORY p w r l i))))

