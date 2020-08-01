(ns numbo.cyto-test
  (:require [clojure.test :refer :all]
  			[numbo.cyto :refer :all]))

(deftest add-brick-test)

(deftest add-block-test)

(deftest block-exists?-test
	(testing "block-exists? for an entry which is in the cyto")

	(testing "block-exists? for an entry which is not in the cyto")

	(testing "block-exists? for an empty cyto"))


(deftest plug-target2-test
	(testing "plug-target2 where there's an exist brick == target2 too"
		(let [start-c '{ :targets ({:val 114 :attr 0}, {:val 6 :attr 0}) :bricks ({:val 11 :attr 0}) :blocks ({:val (- 7 1) :attr 0}, {:val (- (* 6 20) 6) :attr 0}) }
								end-c '{ :targets ({:val 114 :attr 0}, {:val 6 :attr 0}) :bricks ({:val 11 :attr 0}) :blocks ({:val (- (* 6 20) (- 7 1)) :attr 0}) }]
								(is (= end-c (plug-target2 start-c '(- 7 1))))))

(testing "plug-target2 where there's no exist brick == target2 too"
		(let [start-c '{ :targets ({:val 114 :attr 0}, {:val 6 :attr 0}) :bricks ({:val 11 :attr 0}) :blocks ({:val (- 7 1) :attr 0}, {:val (- (* 8 20) 6) :attr 0}) }
								end-c '{ :targets ({:val 114 :attr 0}, {:val 6 :attr 0}) :bricks ({:val 11 :attr 0}) :blocks ({:val (- (* 8 20) (- 7 1)) :attr 0}) }]
								(is (= end-c (plug-target2 start-c '(- 7 1))))))

(testing "plug-target2 where there's no match for the block, so it's a no-op"
		(let [start-c '{ :targets ({:val 114 :attr 0}, {:val 6 :attr 0}) :bricks ({:val 11 :attr 0}) :blocks ({:val (- 7 1) :attr 0}, {:val (- (* 8 20) 12) :attr 0}) }]
								(is (= start-c (plug-target2 start-c '(- 7 1))))))
	)