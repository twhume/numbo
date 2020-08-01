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
				(is (= 1.0 (get-in activated [:1 :activation])))
				; Did nothing else get activated?
				(is (= #{:1} (-set-with-activation activated 1.0)))
				
				; Did its neighbors (:plus-1-1, :plus-1-2, :plus-1-3) get activated a bit less?
				(is (= 0.6 (get-in activated [:plus-1-1 :activation])))
				(is (= 0.6 (get-in activated [:plus-1-2 :activation])))
				(is (= 0.6 (get-in activated [:plus-1-3 :activation])))

				; Did no-one else get activated a bit less?
				(is (= #{:plus-1-4 :minus-4-1 :minus-50-1 :minus-5-1 :plus-1-3 :minus-7-1 :plus-1-2 :plus-1-9 :plus-1-5 :plus-1-6 :minus-10-1 :plus-1-8 :plus-1-1 :minus-3-1 :minus-6-1 :minus-8-1 :minus-9-1 :plus-1-7} (-set-with-activation activated 0.6)))

				; Did their neighbors (:2, :3, :4 but not :1) get activated?
				(is (= 0.2 (get-in activated [:2 :activation])))
				(is (= 0.2 (get-in activated [:3 :activation])))
				(is (= 0.2 (get-in activated [:4 :activation])))

				; Did no-one else get activated a bit less?
				(is (= #{:10 :4 :minus :7 :50 :8 :9 :49 :plus :2 :5 :3 :6} (-set-with-activation activated 0.2)))

				; Is everyone else's activation unaltered?
				; we have 8 nodes with altered activations. So (count of nodes)-32 should have activations

				(is (=
					(- (count (keys activated)) 32)
					(count (-set-with-activation activated 0.0))
					))
)))

(deftest pnet-decay-test
	(testing "Validates that decaying activation works as expected"
		(let [original (activate-node (initialize-pnet initial-pnet) :times-5-20)
								decayed (decay original)]
								(is
								 (and
										(every? #(>= (first %1) (second %1)) ; No entry is in the original is lower than the entry in the decayed
											(map list
												(map :activation (vals original))
												(map :activation (vals decayed))))
										(some #(> (first %1) (second %1)) ; Some entries in the original are higher than in the decayed
											(map list
												(map :activation (vals original))
												(map :activation (vals decayed)))))))))

