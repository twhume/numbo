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

	(testing "Get random brick"
		(let [test-wm (-> '()
			 												(wm/add-brick 1))]
		(is (= nil (wm/get-random-brick '() false)))
		(is (= 1 (count test-wm)))
		(is (= 1 (:value (wm/get-random-brick test-wm false))))
		(is (= 1 (:value (wm/get-random-brick test-wm false))))
		(is (= 1 (:value (wm/get-random-brick test-wm false))))))

	(testing "Get random brick (free)"
		(let [test-wm (-> '()
			 												(wm/add-brick 1)
			 												(wm/add-brick 9))
								brick9 (assoc (first test-wm) :free false)
								test-wm (wm/update-brick test-wm brick9)]
		(is (= 2 (count test-wm)))
		(is (= 1 (:value (wm/get-random-brick test-wm true))))
		(is (= 1 (:value (wm/get-random-brick test-wm true))))
		(is (= 1 (:value (wm/get-random-brick test-wm true))))))

	(testing "Get random brick (none matches)"
		(let [test-wm (-> '()
			 												(wm/add-brick 1)
			 												(wm/add-brick 9))
								brick9 (assoc (first test-wm) :free false)
								brick1 (assoc (second test-wm) :free false)
								test-wm (wm/update-brick test-wm brick9)
								test-wm (wm/update-brick test-wm brick1)]
		(is (= 2 (count test-wm)))
		(is (= nil (wm/get-random-brick test-wm true)))))

	(testing "Get random block")

			(let [test-wm (-> '()
			 												(wm/add-block 10 :plus [6 4]))]
			(is (= 1 (count test-wm)))
			(is (= nil (wm/get-random-block '())))
			(is (= 10 (:value (wm/get-random-block test-wm))))
))



(deftest find-anywhere-test
		(do 
			(wm/reset)
			(wm/set-target 114)
			(wm/add-brick 15)
			(wm/add-brick 10)
			(wm/add-brick 2)
			(wm/add-brick 7)
			(wm/add-block 9 :plus [2 7])
			(wm/add-block 30 :times [15 2])
			(wm/add-child-block (:uuid (first @wm/BLOCKS)) 1 7 :minus [10 2])

			(testing "Find if target"
				(is (= [@wm/TARGET :target] (wm/find-anywhere (:uuid @wm/TARGET)))))
			(testing "Find if brick")
				(is (= [(first @wm/BRICKS) :bricks] (wm/find-anywhere (:uuid (first @wm/BRICKS)))))
			(testing "Find if block")
				(is (= [(first @wm/BLOCKS) :blocks] (wm/find-anywhere (:uuid (first @wm/BLOCKS)))))
			(testing "Find if child block")
				(is (= [(second (:params (first @wm/BLOCKS))) :blocks] (wm/find-anywhere (:uuid (second (:params (first @wm/BLOCKS)))))))
			(testing "No matches"
					(is (= nil (wm/find-anywhere "dummy-uuid"))))
			))

(deftest pump-node-test
	(testing "Pump if target")
	(testing "Pump if brick")
	(testing "Pump if block")
	(testing "Pump if child block")
	(testing "No matches"))



; pump-node