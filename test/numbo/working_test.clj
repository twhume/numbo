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



;      (is (= true (-validate-pnet initial-pnet)))
;      (is (= true (-validate-pnet (initialize-pnet initial-pnet))))
