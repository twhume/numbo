(ns numbo.working)

; Working Memory (WM)
(def node-types '(:brick :block :operation :target :secondary))
(def statuses '(:taken :free))

(def NODES (atom '()))

; TODO suspiciously similar to cl/new-codelet, rationalize

(defn new-node 
 [t & s]
  (into '{ :type t :status :free :attractiveness 1 } (map vec (partition 2 s))))

(def initial-working
	'{
			:target nil
			:bricks '()
	}
)


; Contributors to temperature:
; # secondary targets
; # nodes which are highly attractive
; # free nodes
;
; High temperature --> less promising; dismantler codelets loaded into coderack, to dismantle probabilistically chosen targets


(defn get-temperature
 "What's the temperature of the working memory m?"
 [m])

(defn add-node
 ([w n v] (conj w (list n v)))
 ([n v] (reset! NODES (add-node @NODES n v))))

(defn print-state
 "(for debug purposes) print the current WM state"
 []
 (println "WM:" @NODES))