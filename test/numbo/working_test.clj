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


(deftest set-target-test
	(testing "Set target when memory is empty"
		(do
			(wm/reset)
			(wm/set-target 999)
			(is (= 999 (:value @wm/TARGET)))))
	(testing "Set target when memory is not empty")
		(do
			(wm/reset)
			(wm/set-target 999)
			(wm/set-target 998)
			(is (= 998 (:value @wm/TARGET)))))

(deftest bricks-test
	(testing "Add first brick")
	(testing "Add second brick")
	(testing "Update existing brick")
)

(deftest blocks-test
	(testing "Add first block")
	(testing "Add second block")
	(testing "Update existing block")
	(testing "Add child block")
	(testing "Update child block")
)

(deftest random-test
	(testing "Get random brick")
	(testing "Get random brick (free)")
	(testing "Get random brick (none matches)")
	(testing "Get random block"))


(deftest find-anywhere-test
	(testing "Find if target")
	(testing "Find if brick")
	(testing "Find if block")
	(testing "Find if child block")
	(testing "No matches"))

(deftest pump-node-test
	(testing "Pump if target")
	(testing "Pump if brick")
	(testing "Pump if block")
	(testing "Pump if child block")
	(testing "No matches"))



; pump-node