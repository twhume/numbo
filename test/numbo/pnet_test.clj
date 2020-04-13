(ns numbo.pnet-test
  (:require [clojure.test :refer :all]
            [numbo.pnet :refer :all]))

(deftest pnet-validation-test
	(testing "Validate default pnet is OK, even after initialization"
      (is (= true (validate-pnet initial-pnet)))
      (is (= true (validate-pnet (initialize-pnet initial-pnet))))

    ))