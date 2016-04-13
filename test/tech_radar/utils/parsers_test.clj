(ns tech-radar.utils.parsers_test
  (:require [clojure.test :refer :all]
            [tech-radar.utils.parsers :refer [parse-twitter-date]]
            [clj-time.core :as time]))

(deftest date-test
  (is (= (time/date-time 2009 11 8 2 3 25) (parse-twitter-date "Sun Nov 08 02:03:25 +0000 2009"))))
