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

	(testing "Add first brick"
		(let [test-wm (-> '()
																(wm/add-brick 1))]
		(is (= 1 (count test-wm)))
		(is (= 1 (:value (first test-wm))))))

	(testing "Add second brick"
		(let [test-wm (-> '()
			 												(wm/add-brick 1)
			 												(wm/add-brick 9))]
		(is (= 2 (count test-wm)))
		(is (= 9 (:value (first test-wm))))))

	(testing "Update existing brick")
		(let [test-wm (-> '()
			 												(wm/add-brick 1)
			 												(wm/add-brick 9))
								brick9 (first test-wm)
								brick10 (assoc brick9 :value 10)
								res-wm (wm/update-brick test-wm brick10)]
		(is (= 2 (count res-wm)))
		(is (= 10 (:value (first res-wm))))))

(deftest blocks-test

	(testing "Add first brick"
		(let [test-wm (-> '()
																(wm/add-block 10 :plus [6 4]))]
			(is (= 1 (count test-wm)))
			(is (= 10 (:value (first test-wm))))))

	(testing "Add second block"
		(let [test-wm (-> '()
															(wm/add-block 10 :plus [6 4])
															(wm/add-block 20 :times [5 4])
															)]
		(is (= 2 (count test-wm)))
		(is (= 20 (:value (first test-wm))))))

	(testing "Update existing block"
		(let [test-wm (-> '()
															(wm/add-block 10 :plus [6 4])
															(wm/add-block 20 :times [5 4]))
								block20 (first test-wm)
								block21 (assoc block20 :value 21)
								res-wm (wm/update-blocks test-wm block21)]
		(is (= 2 (count res-wm)))
		(is (= 21 (:value (first res-wm))))))

	(testing "Add child block"
		(let [test-wm (-> '()
															 (wm/add-block 10 :plus [6 4])
															 (wm/add-block 20 :times [5 4]))
								block20 (first test-wm)
								res-wm  (wm/add-child-block test-wm (:uuid block20) 1 4 :plus [2 2])
								res-bl  (first res-wm)  
								]
		(is (= 2 (count res-wm)))
		(is (= 20 (:value res-bl)))
		(is (= 4 (:value (second (:params res-bl)))))))

	(testing "Update child block"
		(let [test-wm (-> '()
															 (wm/add-block 10 :plus [6 4])
															 (wm/add-block 20 :times [5 4]))
								block20 (first test-wm)
								test-wm (wm/add-child-block test-wm (:uuid block20) 1 4 :plus [2 2])
								child-bl (second (:params (first test-wm)))
								new-ch   (assoc child-bl :value 6 :params [3 3])
								res-wm	 (wm/update-blocks test-wm new-ch)
								]
		(is (= 2 (count res-wm)))
		(is (= 6 (:value (second (:params (first res-wm))))))
		(is (= [3 3] (:params (second (:params (first res-wm)))))))))

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