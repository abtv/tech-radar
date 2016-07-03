(ns tech-radar.analytics.utils-test
  (:require [clojure.test :refer :all]
            [tech-radar.analytics.utils :refer [get-words
                                                get-hashtags]]))

(deftest get-words-test
  (is (= #{"are" "here" "words"} (get-words "words are here")))
  (is (= #{"project" "tech-radar"} (get-words "tech-radar project")))
  (is (= #{"some-good-news" "are" "here"} (get-words "some-good-news are here")))
  (is (= #{"call-555" "one" "more" "time"} (get-words "call-555 one more time")))
  (is (= #{"1" "20" "300"} (get-words "1 20 300")))
  (is (= #{"0-0"} (get-words "0-0")))
  (is (= #{"this" "is" "000-000"} (get-words "this is 000-000")))
  (is (= #{"#some" "word"} (get-words "#some word"))))

(deftest get-hashtags-test
  (is (= #{} (get-hashtags "")))
  (is (= #{} (get-hashtags "There are no hashtags")))
  (is (= #{"#football" "#match0-0"} (get-hashtags "It was an awesome #football #match0-0")))
  (is (= #{} (get-hashtags "#00")))
  (is (= #{"#d00"} (get-hashtags "#d00")))
  (is (= #{} (get-hashtags "d#dd")))
  (is (= #{"#some-hash-tag"} (get-hashtags "#some-hash-tag word")))
  (is (= #{} (get-hashtags "and this #--")))
  (is (= #{} (get-hashtags "This is not a hash#tag"))))
