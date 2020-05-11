(ns numbo.pnet-test
  (:require [clojure.test :refer :all]
            [numbo.pnet :refer :all]))

(deftest pnet-validation-test
	(testing "Validate default pnet is OK after initialization"
		(let [p (initialize-pnet initial-pnet)]
      (is (= true (-validate-pnet p)))
      (is (= :param (get-link-type p :plus-2-2 :2)))
      (is (= :result (get-link-type p :plus-1-2 :3)))
      (is (= :operator (get-link-type p :plus-1-2 :plus)))
      (is (= :similar (get-link-type p :4 :5)))
)))

(deftest pnet-activation-test
	(testing "Validates that spreading activation works as expected"
		(let [p (initialize-pnet initial-pnet)
								activated (activate-node p :1)]

				; Did it get activated?
				(is (= 0.3 (get-in activated [:1 :activation])))
				; Did nothing else get activated?
				(is (= #{:1} (-set-with-activation activated 0.3)))
				

				; Did its neighbors (:plus-1-1, :plus-1-2, :plus-1-3) get activated a bit less?
				(is (= 0.2 (get-in activated [:plus-1-1 :activation])))
				(is (= 0.2 (get-in activated [:plus-1-2 :activation])))
				(is (= 0.2 (get-in activated [:plus-1-3 :activation])))

				; Did no-one else get activated a bit less?
				(is (= #{:plus-1-1 :plus-1-2 :plus-1-3} (-set-with-activation activated 0.2)))

				; Did their neighbors (:2, :3, :4 but not :1) get activated?
				(is (= 0.15 (get-in activated [:2 :activation])))
				(is (= 0.15 (get-in activated [:3 :activation])))
				(is (= 0.15 (get-in activated [:4 :activation])))

				; Did no-one else get activated a bit less?
				(is (= #{:2 :3 :4 :plus} (-set-with-activation activated 0.15)))

				; Is everyone else's activation unaltered?
				; we have 8 nodes with altered activations. So (count of nodes)-8 should have activations 1.0

				(is (=
					(- (count (keys activated)) 8)
					(count (-set-with-activation activated 0.1))
					))
)))

(deftest pnet-activation-test
	(testing "Validates that spreading activation works as expected"
		(let [original (initialize-pnet initial-pnet)
								decayed (decay original)]
								(is (every? #(> (first %1) (second %1))
									(map list
										(map :activation (vals original))
										(map :activation (vals decayed))))))))

