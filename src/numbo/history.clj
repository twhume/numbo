(ns numbo.history)

; Encapsulates a run of Numbojure
; A sequence of steps, each of which is a map of
; :pnet PNet at the end of this step
; :target Target at the end of this step
; :bricks Bricks at the end of this step
; :blocks Blocks at the end of this step
; :coderack Coderack at the end of this step
; :codelet Codelet run in this step
; :iteration number of the step

(def HISTORY (atom '[]))

(defn reset 
 "Restart history"
 []
 (reset! HISTORY '[]))

(defn add-step
 ([h p c r l i te]
 	(conj h (hash-map :pnet p :cyto c :coderack r :codelet l :iteration i :temperature te)))
 ([p c r l i te]
 	(reset! HISTORY (add-step @HISTORY p c r l i te))))

