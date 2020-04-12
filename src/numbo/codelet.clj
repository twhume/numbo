(ns numbo.codelet)

; Lets us create codelets with a consistent set of fields.
; Example usage:
;
; (new-codelet)
; (new-codelet :urgency 10)

(defn new-codelet
 "Create a skeleton of a new codelet, with optional modified fields"
 [& s]
 (into '{:urgency 1} (map vec (partition 2 s))))