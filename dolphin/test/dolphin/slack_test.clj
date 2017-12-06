(ns dolphin.slack-test
  (:require [dolphin.slack :as test]
						[clojure.test :refer :all]
            [dolphin.core :refer :all]))

(deftest register-one-command
  (testing "Register-one-command"
    (do
			(is (= 0 (count @test/list-of-commands)))
			(test/add-to-list-of-commands ["A"])	
			(is (= 1 (count @test/list-of-commands)))
		)
  )
)

