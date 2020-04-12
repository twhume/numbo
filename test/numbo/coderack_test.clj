(ns numbo.coderack-test
  (:require [clojure.test :refer :all]
  										[numbo.codelet :refer :all]
            [numbo.coderack :refer :all]))

(deftest rack-selection-test
	(let [empty-rack ()
							single-item-rack (list (new-codelet))
							two-item-rack (list (new-codelet) (new-codelet :urgency 5))
							five-item-rack (list (new-codelet) (new-codelet) (new-codelet :urgency 2) (new-codelet :urgency 5) (new-codelet :urgency 10))
							all-1-rack (list (new-codelet :name 1) (new-codelet :name 2) (new-codelet :name 3) (new-codelet :name 4) (new-codelet :name 5))]
  (testing "Rack selection given a value"
    (is (= (-select-node-for-val (-make-urgencies empty-rack) 0) nil))

    (is (= (-select-node-for-val (-make-urgencies single-item-rack) 0) (new-codelet)))
    (is (= (-select-node-for-val (-make-urgencies single-item-rack) 1) nil))

    (is (= (-select-node-for-val (-make-urgencies two-item-rack) 0) (new-codelet)))
    (is (= (-select-node-for-val (-make-urgencies two-item-rack) 1) (new-codelet :urgency 5)))
    (is (= (-select-node-for-val (-make-urgencies two-item-rack) 5) (new-codelet :urgency 5)))
    (is (= (-select-node-for-val (-make-urgencies two-item-rack) 6) nil))

    (is (= (-select-node-for-val (-make-urgencies five-item-rack) 0) (new-codelet)))
    (is (= (-select-node-for-val (-make-urgencies five-item-rack) 1) (new-codelet)))
    (is (= (-select-node-for-val (-make-urgencies five-item-rack) 3) (new-codelet :urgency 2)))
    (is (= (-select-node-for-val (-make-urgencies five-item-rack) 8) (new-codelet :urgency 5)))
    (is (= (-select-node-for-val (-make-urgencies five-item-rack) 18) (new-codelet :urgency 10)))
    (is (= (-select-node-for-val (-make-urgencies five-item-rack) 19) nil))

    (is (= (-select-node-for-val (-make-urgencies all-1-rack) 0) (new-codelet :name 1)))
    (is (= (-select-node-for-val (-make-urgencies all-1-rack) 1) (new-codelet :name 2)))
    (is (= (-select-node-for-val (-make-urgencies all-1-rack) 2) (new-codelet :name 3)))
    (is (= (-select-node-for-val (-make-urgencies all-1-rack) 3) (new-codelet :name 4)))
    (is (= (-select-node-for-val (-make-urgencies all-1-rack) 4) (new-codelet :name 5)))
    (is (= (-select-node-for-val (-make-urgencies all-1-rack) 5) nil))

    )))

(deftest rack-emptying-test
	(let [five-item-rack (list (new-codelet) (new-codelet) (new-codelet :urgency 2) (new-codelet :urgency 5) (new-codelet :urgency 10))]
	 (do
	 	(reset! CODERACK five-item-rack)
  	(testing "Rack is processed one item at a time, and doesn't fall over when we hit zero entries"
  		(is (= 5 (count @CODERACK)))
  		(process-next-node)
  		(is (= 4 (count @CODERACK)))
  		(process-next-node)
  		(is (= 3 (count @CODERACK)))
  		(process-next-node)
  		(is (= 2 (count @CODERACK)))
  		(process-next-node)
  		(is (= 1 (count @CODERACK)))
  		(process-next-node)
  		(is (= 0 (count @CODERACK)))
  		(process-next-node)
  		(is (= 0 (count @CODERACK)))
  		))))
