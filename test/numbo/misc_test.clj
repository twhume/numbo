(ns numbo.misc-test
  (:require [clojure.test :refer :all]
  										[numbo.codelet :as cl]
            [numbo.misc :as misc]))

(deftest range-selection-test
	(let [empty-rack ()
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

(deftest range-selection-test
 (testing "Normalizing values"
 	(is (= 0 (misc/normalized 0 -0.01)))
 	(is (= 1 (misc/normalized 1 0.01)))
 	(is (= 0.51 (misc/normalized 0.5 0.01)))))
