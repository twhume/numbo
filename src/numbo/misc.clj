(ns numbo.misc
	(:require [clojure.zip :as zip]))

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
 (select-val-in-range r (rand-int (first (last r)))))

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

 (if (= (k m) 0) 1
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