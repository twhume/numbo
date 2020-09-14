(ns numbo.history
	(:require [numbo.misc :as misc]))

; Encapsulates a run of Numbojure
; A sequence of steps, each of which is a map of
; :pnet PNet at the end of this step
; :cyto Cytoplasm at this step
; :coderack Coderack at the end of this step
; :codelet Codelet run in this step
; :iteration number of the step
; :temperature at this step

(def HISTORY (misc/thread-local (atom '[])))

(defn reset 
 "Restart history"
 []
 (reset! @HISTORY '[]))

(defn add-step
 ([h p c r l i te]
 	(conj h (hash-map :pnet p :cyto c :coderack r :codelet l :iteration i :temperature te)))
 ([p c r l i te]
 	(reset! @HISTORY (add-step @@HISTORY p c r l i te))))