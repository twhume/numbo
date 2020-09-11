(ns numbo.misc
	(:require [clojure.tools.logging :as log]
											[clojure.set :as set]
											[clojure.zip :as zip]
											[random-seed.core :refer :all])
	(:refer-clojure :exclude [rand rand-int rand-nth]))

; Useful functions I want to avoid duplicating across files

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
 ([v] (normalized v 0.01 1.0))
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

(defn third
	"Return the third item in the sequence s"
	[s]
	(nth s 2))

(defn invert-val
 "Invert the value of key k in map m"
 [k m]
 (if (nil? m) nil
 (assoc m k (round-to 2 (- 1 (k m))))))

(defn int-k
 "Turn keyword into an integer"
 [k]
 (Integer/parseInt (name k)))


(defn seq-remove
 "Return sequence s without the first instance of value v"
 [s v] ((if (vector? s) vec identity) ; preserve vectorhood!
 	(let [[n m] (split-with (partial not= v) s)] (concat n (rest m)))))

(defn closest
	"Return the element of sequence s which is closest to the input value n"
	[s n]
	(do (log/debug "closest s=" s " n=" n)
	(nth s (first (first (sort-by second (map-indexed #(list %1 (Math/abs (- n %2))) s)))))))

(defn fuzzy-closest
	"Return an element of sequence s close to the input value n: closest 70% of the time, second-closest 20%, third 10%"
	[s n]
	(do
		(log/debug "fuzzy-closest s=" s " n=" n)
		(let [which (condp >= (rand)
																0.7 first
																0.9 (if (> (count s) 1) second first)
																(condp >= (count s)
																	2 first
																	1 second
																	third
																))]
		 (log/debug "which=" which "s=" (count s))
			(nth s (first (which (sort-by second (map-indexed #(list %1 (Math/abs (- n %2))) s))))))))

(defn fuzzy-closest-seq
 "Return a list of integer elements from sequence c which are closest to each item in s, w/o reusing elements"
 [c s]
 (loop [nodes c
 							ins s
 							ret '[]]
 							(if (or (empty? ins) (empty? nodes)) ret
	 							(let [best-match (fuzzy-closest nodes (first ins))]
	 								(recur
	 									(seq-remove nodes best-match)
	 									(rest ins)
	 									(conj ret best-match)
	 								)))))

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

(defn xor [a b] (or (and a (not b)) (and (not a) b)))

(defn sample
 "Sample n random values from sequence s where the weight of each value is f(), total of all weights is w"
 ([s f n w]
  (if (empty? s) '()
   (loop [rem s ret '[] tot w]
				(if (or (empty? rem) (= n (count ret))) ret
	    (let [cur (-sample-val rem f (rand tot))]
						(recur (seq-remove rem cur) (conj ret cur) (- tot (f cur))))))))
 ([s f n] (sample s f n (reduce + (map f s))))
 ([s f] (first (sample s f 1))))

; a = an atom containing the input sequence
; f = a function run on each element in the input sequence to determine their value
; p = a function run on the whole input sequence before processing
; o = a function run on each value before it is returned

(defn mk-sampler
 "Create a sampler for the sequence in atom a using function f to identify value of an element in it, p to filter out values"
 ([a f p o]
 	(fn
 		([] (o (sample (map o (p @a)) f)))
   ([n] (map o (sample (map o (p @a)) f n)))))
 ([a f p] (mk-sampler a f p identity))
 ([a f] (mk-sampler a f identity identity)))

(defn invert-key
 "Returns the inverse value of key k in input c"
 [k min max c]
 (- (+ max min) (k c)))
