(ns numbo.codelet
	(:require [numbo.working :as wm]
											[numbo.coderack :as cr]))

(def codelet-types :new-node)

; Lets us create codelets with a consistent set of fields.
; Example usage:
;
; (new-codelet)
; (new-codelet :urgency 10)

(defn new-codelet
 "Create a skeleton of a new codelet, with optional modified fields"
 [& s]
 (into '{:urgency 1} (map vec (partition 2 s))))

; create-node - adds a new node to WM for a brick or target

(defn create-node
	[t v]
	(cr/add-codelet (new-codelet :type :new-node :fn (fn [] (wm/add-node t v)))))

; Examples from the text
; maybe we don't need all these...
; See p142
;
; kill-secondary-nodes: destroys previously built blocks or target, frees up constituent nodes
; share-digits?: notice that a target and brick share digits
; activate the pnet landmark closest to the target
; activate operations suitable for target (e.g. weight towards mult for targets much larger than bricks)
; TODO: more on p143 onwards
; TODO: also look at source code

; work out: do we need a visualizer?