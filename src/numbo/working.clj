(ns numbo.working
	(:require [numbo.misc :as misc]))

; Working Memory (WM)
(def node-types '(:brick :block :operation :target :secondary))
(def statuses '(:taken :free))

; default amount by which to increase attractiveness of a node, when it's pumped
(def DEFAULT_ATTRACTION_INC 5)
; default starting attraction
(def DEFAULT_ATTRACTION 1)

(def NODES (atom '()))

; TODO suspiciously similar to cl/new-codelet, rationalize
;
;
; Nodes are a map of:
; :type - one of :brick :target :target2 (secondary target) :block
; :status - one of :free :taken
; :attractiveness - how attractive it is right now
; :value - value of any bricks

(defn new-node 
 "create a new node of type t with optional values s"
 [t & s]
  (into (hash-map :type t :status :free :attractiveness DEFAULT_ATTRACTION) (map vec (partition 2 s))))

; Pulls a random brick from the WM, selected probailistically according to :attractiveness.
; If f is true, only select blocks with a status of :free

(defn get-random-brick
 "Choose a random brick from the WM w probabilistically by attractiveness. If f is true, just free ones"
 ([w f]
 	(let [ranges (misc/make-ranges (if f (filter #(= :free (:status %)) w) w) :attractiveness)]
 	 (if (not-empty ranges)
 	  (misc/random-val-in-range ranges))))
 ([f] (get-random-brick @NODES f)))


(def initial-working
	'{
			:target nil
			:bricks '()
	}
)

(defn get-largest-brick
	"Return the value of the largest free brick in memory, nil if there's none"
	([n]
	 (let [free-bricks (filter #(and (= :brick (:type %)) (= :free (:status %))) n)]
	 	(if (empty? free-bricks) nil (apply max-key :value free-bricks))))
	([] (get-largest-brick @NODES)))

; Contributors to temperature:
; # secondary targets
; # nodes which are highly attractive
; # free nodes
;
; High temperature --> less promising; dismantler codelets loaded into coderack, to dismantle probabilistically chosen targets


(defn get-temperature
 "What's the temperature of the working memory m?"
 [m])

(defn -add-node
 "Private function which adds the node n to the working memory w, returns w"
 [w n]
 (conj w n)
 )

(defn add-node
 "Adds a new node of type t and value v to the working memory"
 ([w t v] (-add-node w (new-node t :value v)))
 ([n v] (reset! NODES (add-node @NODES n v))))

(defn pump-node
 "Pump a node n in memory w, by increasing its attractiveness"
 ([w n]
	 (let [pumped-n (assoc n :attractiveness (+ (:attractiveness n) DEFAULT_ATTRACTION_INC))]
 		(conj (remove #{n} w) pumped-n))))

(defn print-state
 "(for debug purposes) print the current WM state"
 []
 (println "WM:" @NODES))