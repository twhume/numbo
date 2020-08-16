(ns numbo.misc-test
  (:require [clojure.test :refer :all]
  										[numbo.codelet :as cl]
            [numbo.misc :as misc]))

(defn -mk-codelet
 [& t]
 (apply cl/new-codelet :dummytype t))

(deftest range-selection-test
	(let [empty-rack '()
							single-item-rack (list (-mk-codelet))
							two-item-rack (list (-mk-codelet) (-mk-codelet :urgency 5))
							five-item-rack (list (-mk-codelet) (-mk-codelet) (-mk-codelet :urgency 2) (-mk-codelet :urgency 5) (-mk-codelet :urgency 10))
							all-1-rack (list (-mk-codelet :name 1) (-mk-codelet :name 2) (-mk-codelet :name 3) (-mk-codelet :name 4) (-mk-codelet :name 5))]
  (testing "Rack selection given a value"
    (is (= (misc/-sample-val empty-rack :urgency 0) nil))

    (is (= (misc/-sample-val single-item-rack :urgency 0) (-mk-codelet :urgency 1)))
    (is (= (misc/-sample-val single-item-rack :urgency 1) (-mk-codelet :urgency 1)))
    (is (= (misc/-sample-val single-item-rack :urgency 2) nil))


    (is (= (misc/-sample-val two-item-rack :urgency 0) (-mk-codelet)))
    (is (= (misc/-sample-val two-item-rack :urgency 1) (-mk-codelet)))
    (is (= (misc/-sample-val two-item-rack :urgency 2) (-mk-codelet :urgency 5)))
    (is (= (misc/-sample-val two-item-rack :urgency 5) (-mk-codelet :urgency 5)))
    (is (= (misc/-sample-val two-item-rack :urgency 6) (-mk-codelet :urgency 5)))
    (is (= (misc/-sample-val two-item-rack :urgency 7) nil))

    (is (= (misc/-sample-val five-item-rack :urgency 0) (-mk-codelet)))
    (is (= (misc/-sample-val five-item-rack :urgency 1) (-mk-codelet)))
    (is (= (misc/-sample-val five-item-rack :urgency 3) (-mk-codelet :urgency 2)))
    (is (= (misc/-sample-val five-item-rack :urgency 8) (-mk-codelet :urgency 5)))
    (is (= (misc/-sample-val five-item-rack :urgency 18) (-mk-codelet :urgency 10)))
    (is (= (misc/-sample-val five-item-rack :urgency 19) (-mk-codelet :urgency 10)))
    (is (= (misc/-sample-val five-item-rack :urgency 20) nil))

    (is (= (misc/-sample-val all-1-rack :urgency 0) (-mk-codelet :name 1)))
    (is (= (misc/-sample-val all-1-rack :urgency 1) (-mk-codelet :name 1)))
    (is (= (misc/-sample-val all-1-rack :urgency 2) (-mk-codelet :name 2)))
    (is (= (misc/-sample-val all-1-rack :urgency 3) (-mk-codelet :name 3)))
    (is (= (misc/-sample-val all-1-rack :urgency 4) (-mk-codelet :name 4)))
    (is (= (misc/-sample-val all-1-rack :urgency 5) (-mk-codelet :name 5)))
    (is (= (misc/-sample-val all-1-rack :urgency 6) nil))

    )))

(deftest percent-range-selection-test
	(let [empty-rack ()
							zerod-rack (list (-mk-codelet :urgency 1 :dummy 1) (-mk-codelet :urgency 1 :dummy 1) (-mk-codelet :urgency 1 :dummy 1))
							single-item-rack (list (-mk-codelet :urgency 1))
							two-item-rack (list (-mk-codelet :urgency 1) (-mk-codelet :urgency 0.5))
							five-item-rack (list (-mk-codelet :urgency 1.0) (-mk-codelet :urgency 1.0) (-mk-codelet :urgency 0.2) (-mk-codelet :urgency 0.5) (-mk-codelet :urgency 1.0))
							all-1-rack (list (-mk-codelet :name 1 :urgency 1) (-mk-codelet :name 2 :urgency 1) (-mk-codelet :name 3 :urgency 1) (-mk-codelet :name 4 :urgency 1) (-mk-codelet :name 5 :urgency 1))]
  (testing "Rack selection given a value"
    (is (= (misc/-sample-val empty-rack :urgency 0) nil))

    (is (= (misc/-sample-val single-item-rack :urgency 0) (-mk-codelet :urgency 1)))
    (is (= (misc/-sample-val single-item-rack :urgency 1) (-mk-codelet :urgency 1)))
    (is (= (misc/-sample-val single-item-rack :urgency 2) nil))

    (is (= (misc/-sample-val two-item-rack :urgency 0) (-mk-codelet :urgency 1)))
    (is (= (misc/-sample-val two-item-rack :urgency 1.0) (-mk-codelet :urgency 1)))
    (is (= (misc/-sample-val two-item-rack :urgency 1.1) (-mk-codelet :urgency 0.5)))
    (is (= (misc/-sample-val two-item-rack :urgency 1.5) (-mk-codelet :urgency 0.5)))
    (is (= (misc/-sample-val two-item-rack :urgency 1.6) nil))

    (is (= (misc/-sample-val five-item-rack :urgency 0) (-mk-codelet :urgency 1.0)))
    (is (= (misc/-sample-val five-item-rack :urgency 1) (-mk-codelet :urgency 1.0)))
    (is (= (misc/-sample-val five-item-rack :urgency 2) (-mk-codelet :urgency 1.0)))
    (is (= (misc/-sample-val five-item-rack :urgency 2.2) (-mk-codelet :urgency 0.2)))
    (is (= (misc/-sample-val five-item-rack :urgency 2.7) (-mk-codelet :urgency 0.5)))
    (is (= (misc/-sample-val five-item-rack :urgency 3.7) (-mk-codelet :urgency 1.0)))
    (is (= (misc/-sample-val five-item-rack :urgency 3.8) nil))

    (is (= (misc/-sample-val all-1-rack :urgency 0) (-mk-codelet :name 1 :urgency 1)))
    (is (= (misc/-sample-val all-1-rack :urgency 1) (-mk-codelet :name 1 :urgency 1)))
    (is (= (misc/-sample-val all-1-rack :urgency 2) (-mk-codelet :name 2 :urgency 1)))
    (is (= (misc/-sample-val all-1-rack :urgency 3) (-mk-codelet :name 3 :urgency 1)))
    (is (= (misc/-sample-val all-1-rack :urgency 4) (-mk-codelet :name 4 :urgency 1)))
    (is (= (misc/-sample-val all-1-rack :urgency 5) (-mk-codelet :name 5 :urgency 1)))
    (is (= (misc/-sample-val all-1-rack :urgency 6) nil))

    )))

(deftest normalize-test
 (testing "Normalizing values"
 	(is (= 0 (misc/normalized 0 -0.01)))
 	(is (= 1 (misc/normalized 1 0.01)))
 	(is (= 0.51 (misc/normalized 0.5 0.01)))))


(deftest within-test
	(testing "Test within range detection"
		; outside the ranges
		(is (= false (misc/within 0.4 1 0.5)))
		(is (= false (misc/within 1.6 1 0.5)))

		(is (= false (misc/within 4 10 0.5)))
		(is (= false (misc/within 16 10 0.5)))

		; within the ranges
		(is (= true (misc/within 0.6 1 0.5)))
		(is (= true (misc/within 1.4 1 0.5)))

		(is (= true (misc/within 6 10 0.5)))
		(is (= true (misc/within 14 10 0.5)))

		; on the border
		(is (= true (misc/within 0.5 1 0.5)))
		(is (= true (misc/within 1.5 1 0.5)))

		(is (= true (misc/within 5 10 0.5)))
		(is (= true (misc/within 15 10 0.5)))

		; nil inputs
		(is (= false (misc/within nil 1 0.5)))
		(is (= false (misc/within 1.5 nil 0.5)))


	))
