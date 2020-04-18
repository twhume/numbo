(ns numbo.working)

; Working Memory (WM)
(def node-types '(:brick :block :operation :target :secondary))
(def statuses '(:taken :free))

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
  (into (hash-map :type t :status :free :attractiveness 1 ) (map vec (partition 2 s))))

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

(defn print-state
 "(for debug purposes) print the current WM state"
 []
 (println "WM:" @NODES))