(ns numbo.working-test
	(:require [clojure.test :refer :all]
											[numbo.working :as wm]))

(deftest get-largest-brick-test
	(testing "Get largest brick from empty memory"
		(is (= nil (wm/get-largest-brick '()))))

	(testing "Get largest brick from memory containing one value"
	 (let [node-to-add (wm/new-node :brick :value 1)
	 							test-wm (-> '()
	 												(wm/-add-node node-to-add))]
	 	(is (= node-to-add (wm/get-largest-brick test-wm)))
	 ))


	(testing "Get largest brick from a full memory"
	 (let [node1 (wm/new-node :brick :value 1)
	 						node2 (wm/new-node :brick :value 2)
	 						node3 (wm/new-node :brick :value 3)
	 							test-wm (-> '()
	 												(wm/-add-node node1)
	 												(wm/-add-node node3)
	 												(wm/-add-node node2)
	 												)]
	 	(is (= node3 (wm/get-largest-brick test-wm)))
	 )))

	(testing "Get largest free brick from a full memory"
	 (let [node1 (wm/new-node :brick :value 1)
	 						node2 (wm/new-node :brick :value 2)
	 						node3 (wm/new-node :brick :value 3 :status :taken)
	 							test-wm (-> '()
	 												(wm/-add-node node1)
	 												(wm/-add-node node3)
	 												(wm/-add-node node2)
	 												)]
	 	(is (= node2 (wm/get-largest-brick test-wm)))
	 ))

(deftest pump-node-test
	(testing "Test pumping of nodes"
	 (let [node1 (wm/new-node :brick :value 1)
		 					node2 (wm/new-node :brick :value 2)
		 					node4 (wm/new-node :brick :value 4)
		 					test-wm (-> '()
		 												(wm/-add-node node1)
		 												(wm/-add-node node2)
		 												)
		 					pumped-wm (-> '()
		 												(wm/-add-node node1)
		 												(wm/-add-node (assoc node2 :attractiveness (+ wm/DEFAULT_ATTRACTION wm/DEFAULT_ATTRACTION_INC)))
		 												)
		 					]
		 					; pumping a non-existent node does nothing
		 					(is (= test-wm) (wm/pump-node test-wm node4))
		 					; pumping another node ups only its attraction
		 					(is (= pumped-wm) (wm/pump-node test-wm node2))
				)))
