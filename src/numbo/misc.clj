(ns numbo.misc
	(:require [clojure.tools.logging :as log]
											[clojure.set :as set]
											[clojure.zip :as zip]
											[random-seed.core :refer :all])
	(:refer-clojure :exclude [rand rand-int rand-nth]))



; Useful functions I want to avoid duplicating across files

; Used to implement probabilistic sampling. Find a specific value within the range
;
; e.g. Urgency value ranges for selecting a codelet. What are urgency value ranges?
; Imagine we have a set of codelet in the rack with urgencies 1 1 2 5 10
; The urgency value ranges are 1 2 4 9 19
; (minimum urgency must be 1 rather than 0, so that 0-urgency nodes have a chance of running)
; A random value from 0..19 would be generated to choose one weighted by its urgency

(defn select-val-in-range
 "Given a sequence of (value range, value) r, and a value v within the ranges, return a value"
	[r v]
 (second (first (filter #(< v (first %)) r))))

; Find a random value within the ranges (this tends to be what we want)

(defn random-val-in-range
 "Given a sequence of (value range, value) r, return a random value"
 [r]
 (if (empty? r) nil
	 (select-val-in-range r (rand-int (first (last r))))))

; Take a sequence of maps s and return a list of (range, node) tuples where range is based on key k.
; Used to power probabilistic sampling of elements from a sequence
;
; With codelets the sequence is the coderack and the key is :urgency
; With bricks in WM the sequence is the NODES and the key is :attractiveness

(defn make-ranges
 [s k]
 	 (map #(list %1 %2) (reductions + (map k s)) s))

(defn -make-percent
 [k m]

 (if (zero? (k m)) 1
 	(int (* (k m) 100))))

; A version of make-ranges which assumes the value pointed at by k is 0..1
; This value is commuted into 1..100

(defn make-percent-ranges
 [s k]
 	 (map #(list %1 %2) (reductions + (map (partial -make-percent k) s)) s))

(defn uuid
 "Generate a new Java UUID"
 []
	(.toString (java.util.UUID/randomUUID)))

(defn round-to
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

(defn normalized
 "Give an input value v and optional modified m return a pumped value capped at 1"
 ([v] (cond
 	(< v 0) 0
 	(> v 1) 1
 	:else (round-to 2 v)
 ))
 ([v m] (normalized (+ (if v v 0) (if m m 0)))))

(defn within
	"is the value v1 within p% of v2?"
	[v1 v2 p]
	(do
		(log/debug "within" v1 v2 p)
	(cond
		(nil? v1) false
		(nil? v2) false
		(and
			(>= v1 (* v2 (- 1 p)))
			(<= v1 (* v2 (+ 1 p)))) true
		:else false))
)

(defn invert-val
 "Invert the value of key k in map m"
 [k m]
 (if (nil? m) nil
 (assoc m k (round-to 2 (- 1 (k m))))))

;; zip-walk takes a transformation function f and a zipper z.
;; f takes a location and returns location. Applies f
;; to the nodes in the zipper maintaining the original nesting.
;; From https://clojuredocs.org/clojure.zip/next

(defn zip-walk [f z]
  (if (zip/end? z)
    (zip/root z)
    (recur f (zip/next (f z)))))

(defn int-k
 "Turn keyword into an integer"
 [k]
 (Integer/parseInt (name k)))

(defn closest
	"Return the element of sequence s which is closest to the input value n"
[s n]
(do (log/debug "closest s=" s " n=" n)
(nth s (first (first (sort-by second (map-indexed #(list %1 (Math/abs (- n %2))) s))))))
)

(defn seq-remove
 "Return sequence s without the first instance of value v"
 [s v] ((if (vector? s) vec identity) ; preserve vectorhood!
 	(let [[n m] (split-with (partial not= v) s)] (concat n (rest m)))))

(defn third
	"Return the third item in the sequence s"
	[s]
	(nth s 2))

(defn common-elements [& colls]
  (let [freqs (map frequencies colls)]
    (mapcat (fn [e] (repeat (apply min (map #(% e) freqs)) e))
            (apply set/intersection (map (comp set keys) freqs)))))

(defn remove-first
 "Return sequence s without the first instance of v"
 [s v]
 ((if (vector? s) vec identity) ; preserve vectorhood in inputs, as we rely on it for ordering purposes
 	(let [[n m] (split-with #(not= v %1) s)] (concat n (rest m)))))

(defn remove-each
 "Returns sequence s1 removing each instance of s2"
 [s1 s2]
 (reduce remove-first s1 s2))

(defn replace-first
 "Return sequence s with the first instance of a replaced by b"
 [a b s]
 (if (some #{a} s)
	 ((if (vector? s) vec identity) ; preserve vectorhood in inputs, as we rely on it for ordering purposes
	 	(let [[n m] (split-with #(not= a %1) s)] (concat n (cons b (rest m)))))
	 s))


























