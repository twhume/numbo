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

(defn -initial-attractiveness
	"Calculates an initial attractiveness for the number, based on its value"
 [n]
 (cond-> 0
 	(= 0 (mod n 5)) (+ 5)
 	(= 0 (mod n 10)) (+ 5)
 	(= 0 (mod n 100)) (+ 5)
 ))

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
	(cr/add-codelet (new-codelet :type :activate-pnet :desc (str "Activate PNet: " n) :urgency URGENCY_MEDIUM :fn (fn [] (pn/activate-node n)))))

(defn inc-attraction
	[n]
	(cr/add-codelet (new-codelet :type :inc-attraction :desc (str "Pump PNet: " n) :urgency URGENCY_MEDIUM :fn (fn [] (wm/pump-node n)))))


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
		 		(wm/add-node :type :target :value v))
		 	 (activate-pnet (keyword (str (closest (pn/get-numbers) v))))
		 	 (map activate-pnet (pn/get-operators))
		 	 (if (and 
		 	 	(not (nil? (wm/get-largest-brick)))
		 	 	(> v (:value (wm/get-largest-brick))))
		 	 		(activate-pnet :times))
		 	))))

; syntactic-comparison (mid urgency) There is a type of codeliet which inspects various nodes and
; notices syntactic similarities, increases attractiveness of them - e.g. if brick 11 shares digits 
; with target 114, increase attractiveness of 11 (p141)


(defn syntactic-comparison
 "Looks for syntactic similarities between nodes and increases their attractiveness accordingly"
 [n1 n2]
 (let [v1 (str (:value n1))
 						v2 (str (:value n2))]
	 (cond

	  ; either node contains the other, as a string - e.g. 114 contains 11, 15 contains 5, 51 contains 5

	  (or (str/includes? v1 v2) (str/includes? v2 v1)) (do (inc-attraction n1) (inc-attraction n2))
	 )))

(defn load-brick
 "Loads a new brick into memory"
 [v]
	(cr/add-codelet (new-codelet :type :load-brick :desc (str "Load brick: " v) :urgency URGENCY_HIGH
	:fn (fn [] (wm/add-node :type :brick :value v :attractiveness (-initial-attractiveness v))))))

; rand-op: (low urgency) - select 2 random bricks (biased by attractiveness), and an op
; (biased towards active pnet nodes),  place resulting block in the WM (p145, #4)

(defn rand-op
 "Make a new block out of sampled random bricks and ops"
 []
 (let [b1 (wm/get-random-brick false)
 						b2 (wm/get-random-brick false)
 						op (pn/get-random-op)]
 						(cr/add-codelet (new-codelet :type :new-block :desc (str "Random op: " op b1 b2) :urgency URGENCY_HIGH
 							:fn (fn [] (wm/add-node :type :block :value (hash-map :arg1 b1 :arg3 b2 :op op)))))))

;----- END OF CODELETS -----
