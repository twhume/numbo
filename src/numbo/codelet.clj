(ns numbo.codelet
	(:require [clojure.string :as str]
											[numbo.coderack :as cr]
											[numbo.pnet :as pn]
											[numbo.working :as wm]))

(def codelet-types :load-target)

(def URGENCY_HIGH 3)
(def URGENCY_MEDIUM 2)
(def URGENCY_LOW 1)

(def urgency-labels '{
	URGENCY_HIGH "High"
	URGENCY_MEDIUM "Medium"
	URGENCY_LOW "Low"
})

; Lets us create codelets with a consistent set of fields.
; Example usage:
;
; (new-codelet)
; (new-codelet :urgency 10)
;
;
; Codelet fields are:
; :urgency
; :fn
; :type (one of codelet-types)
; :desc description

(defn -noop
 [] nil)

(defn new-codelet
 "Create a skeleton of a new codelet, with optional modified fields"
 [& s]
 (into (hash-map :urgency URGENCY_LOW :fn -noop :iteration @cr/ITERATIONS) (map vec (partition 2 s))))



;----- HELPER FUNCTIONS USED BY CODELETS -----
; move these into their files

(defn closest
	"Return the element of sequence s which is closest to the input value n"
[s n]
(nth s (first (first (sort-by second (map-indexed #(list %1 (Math/abs (- n %2))) s))))))

(defn wm-get-random-free-bricks
 "Returns a sequence of randomly chosen free bricks (probabilistically chosen)"
	[]
	)

;----- CODELETS HEREON -----

; activates a specific node in the Pnet

(defn activate-pnet
	[n]
	(cr/add-codelet (new-codelet :type :activate-pnet
																														:desc (str "Activate PNet: " n)
																														:urgency URGENCY_MEDIUM
																														:fn (fn [] (pn/activate-node n)))))

(defn pump-node
 "Pumps the attractiveness of a brick or block with UUID u"
	[u]
	(cr/add-codelet (new-codelet :type :inc-attraction
																														:desc (str "Pump brick/block: " u)
																														:urgency URGENCY_MEDIUM :fn
																														(fn [] (wm/pump-node u)))))

; load-target - (high urgency) when a target is loaded, the pnet landmark closest is activated.
; operands (* - +) are activated. If it’s larger than the largest brick, * is activated more.
; (p143)

(defn load-target
	"Add target node, activate closest number in Pnet, and operands (* if target is larger than largest brick)"
 [v]
 (cr/add-codelet
 	(new-codelet :type :load-target :desc (str "Load target: " v) :urgency URGENCY_HIGH
	 	:fn (fn []
		 	(do
		 		(wm/set-target v)
		 	 (activate-pnet (keyword (str (closest (pn/get-numbers) v))))
		 	 (map activate-pnet (pn/get-operators))
		 	 (if (and 
		 	 	(not (nil? (wm/get-largest-brick)))
		 	 	(> v (:value (wm/get-largest-brick))))
		 	 		(activate-pnet :times))
		 	)))))

; syntactic-comparison (mid urgency) There is a type of codeliet which inspects various nodes and
; notices syntactic similarities, increases attractiveness of them - e.g. if brick 11 shares digits 
; with target 114, increase attractiveness of 11 (p141)

(defn rand-syntactic-comparison
 "Examine a random brick or block, compare to the target, pump it if promising"
 []
 (let [blocks (list (wm/get-random-brick false) (wm/get-random-block))
 						block (rand-nth blocks)
 						val (str (:value block))
 						tval (str (:value @wm/TARGET))]
	 (cond

	  ; either node contains the other, as a string - e.g. 114 contains 11, 15 contains 5, 51 contains 5
	  (or
	  	(str/includes? val tval)
	  	(str/includes? tval val)) (pump-node (:uuid block))
	 )))

(defn load-brick
 "Loads a new brick into memory"
 [v]
	(cr/add-codelet (new-codelet :type :load-brick :desc (str "Load brick: " v) :urgency URGENCY_HIGH
	:fn (fn [] (wm/add-brick v)))))

; rand-op: (low urgency) - select 2 random bricks (biased by attractiveness), and an op
; (biased towards active pnet nodes),  place resulting block in the WM (p145, #4)

(defn rand-block
 "Make a new block out of sampled random bricks and ops"
 []
 (let [b1 (wm/get-random-brick false)
 						v1 (:value b1)
 						b2 (wm/get-random-brick false)
 						v2 (:value b2)
 						op (pn/get-random-op)]
 						(cr/add-codelet (new-codelet :type :new-block
 							:desc (str "Random op: " (:name op) " " v1 "," v2)
 							:urgency URGENCY_HIGH
 							:fn (fn [] (wm/add-block (((:name op) pn/operator-map) v1 v2) (:name op) (vector v1 v2)))))))

;----- END OF CODELETS -----
