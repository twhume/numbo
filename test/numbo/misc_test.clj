(ns numbo.misc-test
  (:require [clojure.test :refer :all]
  										[numbo.codelet :as cl]
            [numbo.misc :as misc]))

(deftest range-selection-test
	(let [empty-rack ()
							zerod-rack (list (cl/new-codelet :urgency 0 :dummy 1) (cl/new-codelet :urgency 0 :dummy 1) (cl/new-codelet :urgency 0 :dummy 1))
							single-item-rack (list (cl/new-codelet))
							two-item-rack (list (cl/new-codelet) (cl/new-codelet :urgency 5))
							five-item-rack (list (cl/new-codelet) (cl/new-codelet) (cl/new-codelet :urgency 2) (cl/new-codelet :urgency 5) (cl/new-codelet :urgency 10))
							all-1-rack (list (cl/new-codelet :name 1) (cl/new-codelet :name 2) (cl/new-codelet :name 3) (cl/new-codelet :name 4) (cl/new-codelet :name 5))]
  (testing "Rack selection given a value"
    (is (= (misc/select-val-in-range (misc/make-ranges empty-rack :urgency) 0) nil))

    (is (= (misc/select-val-in-range (misc/make-ranges single-item-rack :urgency) 0) (cl/new-codelet)))
    (is (= (misc/select-val-in-range (misc/make-ranges single-item-rack :urgency) 1) nil))

    (is (= (misc/select-val-in-range (misc/make-ranges two-item-rack :urgency) 0) (cl/new-codelet)))
    (is (= (misc/select-val-in-range (misc/make-ranges two-item-rack :urgency) 1) (cl/new-codelet :urgency 5)))
    (is (= (misc/select-val-in-range (misc/make-ranges two-item-rack :urgency) 5) (cl/new-codelet :urgency 5)))
    (is (= (misc/select-val-in-range (misc/make-ranges two-item-rack :urgency) 6) nil))

    (is (= (misc/select-val-in-range (misc/make-ranges five-item-rack :urgency) 0) (cl/new-codelet)))
    (is (= (misc/select-val-in-range (misc/make-ranges five-item-rack :urgency) 1) (cl/new-codelet)))
    (is (= (misc/select-val-in-range (misc/make-ranges five-item-rack :urgency) 3) (cl/new-codelet :urgency 2)))
    (is (= (misc/select-val-in-range (misc/make-ranges five-item-rack :urgency) 8) (cl/new-codelet :urgency 5)))
    (is (= (misc/select-val-in-range (misc/make-ranges five-item-rack :urgency) 18) (cl/new-codelet :urgency 10)))
    (is (= (misc/select-val-in-range (misc/make-ranges five-item-rack :urgency) 19) nil))

    (is (= (misc/select-val-in-range (misc/make-ranges all-1-rack :urgency) 0) (cl/new-codelet :name 1)))
    (is (= (misc/select-val-in-range (misc/make-ranges all-1-rack :urgency) 1) (cl/new-codelet :name 2)))
    (is (= (misc/select-val-in-range (misc/make-ranges all-1-rack :urgency) 2) (cl/new-codelet :name 3)))
    (is (= (misc/select-val-in-range (misc/make-ranges all-1-rack :urgency) 3) (cl/new-codelet :name 4)))
    (is (= (misc/select-val-in-range (misc/make-ranges all-1-rack :urgency) 4) (cl/new-codelet :name 5)))
    (is (= (misc/select-val-in-range (misc/make-ranges all-1-rack :urgency) 5) nil))

    )))

(deftest percent-range-selection-test
	(let [empty-rack ()
							zerod-rack (list (cl/new-codelet :urgency 0 :dummy 1) (cl/new-codelet :urgency 0 :dummy 1) (cl/new-codelet :urgency 0 :dummy 1))
							single-item-rack (list (cl/new-codelet :urgency 0))
							two-item-rack (list (cl/new-codelet :urgency 0) (cl/new-codelet :urgency 0.5))
							five-item-rack (list (cl/new-codelet :urgency 0) (cl/new-codelet :urgency 0) (cl/new-codelet :urgency 0.2) (cl/new-codelet :urgency 0.5) (cl/new-codelet :urgency 1.0))
							all-1-rack (list (cl/new-codelet :name 1 :urgency 0) (cl/new-codelet :name 2 :urgency 0) (cl/new-codelet :name 3 :urgency 0) (cl/new-codelet :name 4 :urgency 0) (cl/new-codelet :name 5 :urgency 0))]
  (testing "Rack selection given a value"
    (is (= (misc/select-val-in-range (misc/make-percent-ranges empty-rack :urgency) 0) nil))

    (is (= (misc/select-val-in-range (misc/make-percent-ranges single-item-rack :urgency) 0) (cl/new-codelet :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges single-item-rack :urgency) 1) nil))

    (is (= (misc/select-val-in-range (misc/make-percent-ranges two-item-rack :urgency) 0) (cl/new-codelet :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges two-item-rack :urgency) 1) (cl/new-codelet :urgency 0.5)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges two-item-rack :urgency) 2) (cl/new-codelet :urgency 0.5)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges two-item-rack :urgency) 50) (cl/new-codelet :urgency 0.5)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges two-item-rack :urgency) 51) nil))

    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 0) (cl/new-codelet :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 1) (cl/new-codelet :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 2) (cl/new-codelet :urgency 0.2)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 3) (cl/new-codelet :urgency 0.2)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 21) (cl/new-codelet :urgency 0.2)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 22) (cl/new-codelet :urgency 0.5)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 71) (cl/new-codelet :urgency 0.5)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 72) (cl/new-codelet :urgency 1.0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 171) (cl/new-codelet :urgency 1.0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges five-item-rack :urgency) 172) nil))

    (is (= (misc/select-val-in-range (misc/make-percent-ranges all-1-rack :urgency) 0) (cl/new-codelet :name 1 :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges all-1-rack :urgency) 1) (cl/new-codelet :name 2 :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges all-1-rack :urgency) 2) (cl/new-codelet :name 3 :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges all-1-rack :urgency) 3) (cl/new-codelet :name 4 :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges all-1-rack :urgency) 4) (cl/new-codelet :name 5 :urgency 0)))
    (is (= (misc/select-val-in-range (misc/make-percent-ranges all-1-rack :urgency) 5) nil))

    )))

(deftest range-selection-test
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
