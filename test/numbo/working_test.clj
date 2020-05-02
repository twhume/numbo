(ns numbo.working-test
	(:require [clojure.test :refer :all]
											[numbo.working :as wm]))

(deftest get-largest-brick-test
	(testing "Get largest brick from empty memory"
		(is (= nil (wm/get-largest-brick '()))))

	(testing "Get largest brick from memory containing one value"
	 (let [test-wm (wm/add-brick '() 1)]
	 	(is (= 1 (:value (wm/get-largest-brick test-wm))))
	 ))


	(testing "Get largest brick from a full memory"
	 (let [test-wm (-> '()
	 												(wm/add-brick 1)
	 												(wm/add-brick 3)
	 												(wm/add-brick 2))]
	 	(is (= 3 (:value (wm/get-largest-brick test-wm))))
	 )))

	(testing "Get largest free brick from a full memory"
	 (let [test-wm (-> '()
	 												(wm/add-brick 1)
	 												(wm/add-brick 3 false)
	 												(wm/add-brick 2))]
	 	(is (= 2 (:value (wm/get-largest-brick test-wm))))
	 ))
