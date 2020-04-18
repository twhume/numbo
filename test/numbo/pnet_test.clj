(ns numbo.pnet-test
  (:require [clojure.test :refer :all]
            [numbo.pnet :refer :all]))

(deftest pnet-validation-test
	(testing "Validate default pnet is OK after initialization"
      (is (= true (-validate-pnet (initialize-pnet initial-pnet))))
))

(deftest pnet-activation-test
	(testing "Validates that spreading activation works as expected"
		(let [p (initialize-pnet initial-pnet)
								activated (activate-node p :1)]

				; Did it get activated?
				(is (= 2 (get-in activated [:1 :activation])))
				; Did nothing else get activated?
				(is (= #{:1} (-set-with-activation activated 2)))
				

				; Did its neighbors (:plus-1-1, :plus-1-2, :plus-1-3) get activated a bit less?
				(is (= 1.5 (get-in activated [:plus-1-1 :activation])))
				(is (= 1.5 (get-in activated [:plus-1-2 :activation])))
				(is (= 1.5 (get-in activated [:plus-1-3 :activation])))

				; Did no-one else get activated a bit less?
				(is (= #{:plus-1-1 :plus-1-2 :plus-1-3} (-set-with-activation activated 1.5)))

				; Did their neighbors (:2, :3, :4 but not :1) get activated?
				(is (= 1.1 (get-in activated [:2 :activation])))
				(is (= 1.1 (get-in activated [:3 :activation])))
				(is (= 1.1 (get-in activated [:4 :activation])))

				; Did no-one else get activated a bit less?
				(is (= #{:2 :3 :4} (-set-with-activation activated 1.1)))

				; Is everyone else's activation unaltered?
				; we have 7 nodes with altered activations. So (count of nodes)-7 should have activations 1.0

				(is (=
					(- (count (keys activated)) 7)
					(count (-set-with-activation activated 1))
					))
)))
