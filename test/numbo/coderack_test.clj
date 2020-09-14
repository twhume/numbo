(ns numbo.coderack-test
  (:require [clojure.test :refer :all]
  										[numbo.codelet :refer :all]
            [numbo.coderack :refer :all]
            [numbo.misc :as misc]))

(deftest rack-emptying-test
	(let [five-item-rack (list (new-codelet :dummytype) (new-codelet :dummytype) (new-codelet  :dummytype :urgency 2) (new-codelet :dummytype :urgency 5) (new-codelet :dummytype :urgency 10))]
	 (do
	 	(reset! @CODERACK five-item-rack)
  	(testing "Rack is processed one item at a time, and doesn't fall over when we hit zero entries"
  		(is (= 5 (count @@CODERACK)))
  		(process-next-codelet)
  		(is (= 4 (count @@CODERACK)))
  		(process-next-codelet)
  		(is (= 3 (count @@CODERACK)))
  		(process-next-codelet)
  		(is (= 2 (count @@CODERACK)))
  		(process-next-codelet)
  		(is (= 1 (count @@CODERACK)))
  		(process-next-codelet)
  		(is (= 0 (count @@CODERACK)))
  		(process-next-codelet)
  		(is (= 0 (count @@CODERACK)))
  		))))
