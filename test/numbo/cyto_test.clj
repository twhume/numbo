(ns numbo.cyto-test
  (:require [clojure.test :refer :all]
  			[numbo.cyto :refer :all]))

(deftest add-brick-test)

(deftest add-block-test)

(deftest block-exists?-test
	(testing "block-exists? for an entry which is in the cyto"
		(is (= 5 (count @CODERACK)))

	(testing "block-exists? for an entry which is not in the cyto"

	(testing "block-exists? for an empty cyto"
