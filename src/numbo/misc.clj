(ns numbo.misc
	(:require [clojure.tools.logging :as log]
											[clojure.set :as set]
											[clojure.zip :as zip]
											[random-seed.core :refer :all])
	(:refer-clojure :exclude [rand rand-int rand-nth]))

; Useful functions I want to avoid duplicating across files

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
 ([v l u] (cond
 	(< v l) l
 	(> v u) u
 	:else (round-to 2 v)
 ))
 ([v] (normalized v 0 1))
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

; Used to implement probabilistic sampling. Find a specific value within the range

(defn -sample-val
 [s f r]
 (if (empty? s) nil
	 (loop [rem s acc 0.0]
		 	(let [cur (first rem) curv (f cur) accv (+ curv acc)]
			  (cond
			  	(nil? cur) (log/error "-sample-val fallen off the edge")
			  	(>= accv r) cur
			  	(= 1 (count rem)) nil
			  	:else (recur (rest rem) accv))))))

(defn sample
 "Sample n random values from sequence s where the weight of each value is f(), total of all weights is w"
 ([s f n w]
  (if (empty? s) '()
   (loop [rem s ret '[] tot w]
				(if (or (empty? rem) (= n (count ret))) ret
	    (let [cur (-sample-val rem f (rand tot))]
						(recur (seq-remove rem cur) (conj ret cur) (- tot (f cur))))))))
 ([s f n] (sample s f n (reduce + (map f s))))
 ([s f] (sample s f 1)))

(defn xor [a b] (or (and a (not b)) (and (not a) b)))
