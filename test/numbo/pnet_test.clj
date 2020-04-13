(ns numbo.pnet-test
  (:require [clojure.test :refer :all]
            [numbo.pnet :refer :all]))

(deftest pnet-validation-test
	(testing "Validate default pnet is OK, even after initialization"
      (is (= true (validate-pnet initial-pnet)))
      (is (= true (validate-pnet (initialize-pnet initial-pnet))))
))

(defn -set-with-weight
 "Return a set of node names with value v from pnet p"
 [p v]
		(into #{} (map key (-find-with-weight p v))))

(deftest pnet-validation-test
	(testing "Validates that spreading activation works as expected"
		(let [p (initialize-pnet initial-pnet)
								activated (activate-node p :1)]

				; Did it get activated?
				(is (= 2 (get-in activated [:1 :weight])))
				; Did nothing else get activated?
				(is (= #{:1} (-set-with-weight activated 2)))
				

				; Did its neighbors (:plus-1-1, :plus-1-2, :plus-1-3) get activated a bit less?
				(is (= 1.5 (get-in activated [:plus-1-1 :weight])))
				(is (= 1.5 (get-in activated [:plus-1-2 :weight])))
				(is (= 1.5 (get-in activated [:plus-1-3 :weight])))

				; Did no-one else get activated a bit less?
				(is (= #{:plus-1-1 :plus-1-2 :plus-1-3} (-set-with-weight activated 1.5)))

				; Did their neighbors (:2, :3, :4 but not :1) get activated?
				(is (= 1.1 (get-in activated [:2 :weight])))
				(is (= 1.1 (get-in activated [:3 :weight])))
				(is (= 1.1 (get-in activated [:4 :weight])))

				; Did no-one else get activated a bit less?
				(is (= #{:2 :3 :4} (-set-with-weight activated 1.1)))

				; Is everyone else's activation unaltered?
				; we have 7 nodes with altered weights. So (count of nodes)-7 should have weights 1.0

				(is (=
					(- (count (keys activated)) 7)
					(count (-set-with-weight activated 1))
					))
)))
