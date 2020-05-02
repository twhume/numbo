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
 ([h p t br bl r l i]
 	(conj h (hash-map :pnet p :target t :bricks br :blocks bl :coderack r :codelet l :iteration i)))
 ([p t br bl r l i]
 	(reset! HISTORY (add-step @HISTORY p t br bl r l i))))

