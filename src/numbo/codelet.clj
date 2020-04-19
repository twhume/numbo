(ns numbo.codelet
	(:require [clojure.string :as str]
											[numbo.coderack :as cr]
											[numbo.pnet :as pn]
											[numbo.working :as wm]))

(def codelet-types :load-target)

(def URGENCY_HIGH 3)
(def URGENCY_MEDIUM 2)
(def URGENCY_LOW 1)

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

(defn -noop
 [] nil)

(defn new-codelet
 "Create a skeleton of a new codelet, with optional modified fields"
 [& s]
 (into (hash-map :urgency URGENCY_LOW :fn -noop) (map vec (partition 2 s))))

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

(defn pn-get-operands
	"Get all the operand nodes from the pnet"
	[] ())

(defn wm-get-random-free-bricks
 "Returns a sequence of randomly chosen free bricks (probabilistically chosen)"
	[]
	)

(defn pn-get-rand-op
 ""
 [])



;----- CODELETS HEREON -----

; -create-node - adds a new node to WM for a brick or target, used to initialize
; TODO set initial attractiveness, function of numerical value (e.g. mod 5, 10 == 0)
;

; activates a specific node in the Pnet

(defn activate-pnet
	[n]
	(cr/add-codelet (new-codelet :type :activate-pnet :urgency URGENCY_MEDIUM :fn (fn [] (pn/activate-node n)))))

(defn inc-attraction
	[n]
	(cr/add-codelet (new-codelet :type :inc-attraction :urgency URGENCY_MEDIUM :fn (fn [] (wm/pump-node n)))))


; load-target - (high urgency) when a target is loaded, the pnet landmark closest is activated.
; operands (* - +) are activated. If it’s larger than the largest brick, * is activated more.
; (p143)

(defn load-target
	"Add target node, activate closest number in Pnet, and operands (* if target is larger than largest brick)"
 [v]
 (cr/add-codelet
 	(new-codelet :type :load-target :urgency URGENCY_HIGH
	 	:fn (fn []
		 	(do
		 		(wm/add-node :target v))
		 	 (activate-pnet (keyword (str (closest (pn/get-numbers) v))))
		 	 (map activate-pnet (pn/get-operators))
		 	 (if (and 
		 	 	(not (nil? (wm/get-largest-brick)))
		 	 	(> v (:value (wm/get-largest-brick))))
		 	 		(activate-pnet :times))
		 	))))

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
	(cr/add-codelet (new-codelet :type :new-node :urgency URGENCY_HIGH :fn (fn [] (wm/add-node :brick v)))))

(defn rand-op
 ""
 [])

(defn create-block
""
[n1 n2 op])

; read-brick
; rand-op




; Next: starting from p142, document the types of codelets
; THEN
; write one which looks for syntactic similarities between bricks/blocks + targets, increases attractiveness of bricks
; write one which looks at random nodes (by attractiveness) and evals to a target
; write one which makes a secondary target
; write one which makes a block

;----- END OF CODELETS -----


; From the book:

; Types of codelets

; Actual codelets
;
; 

; Examples fr

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