(ns tech-radar.services.classifier-test
  (:require [clojure.test :refer :all]
            [tech-radar.services.classifier :refer [index-topics
                                                   classify
                                                   get-hashtags]]))

(deftest index-topics-test
  (let [topics         {:jvm     ["JVM" "scala lang" "java"]
                        :clojure ["clojure" "ClojureScript"]}
        indexed-topics (index-topics topics)]
    (is (= {:jvm     [#{"jvm"} #{"scala" "lang"} #{"java"}]
            :clojure [#{"clojure"} #{"clojurescript"}]} indexed-topics))))

(deftest classify-test
  (let [topics (index-topics {:jvm     ["JVM" "scala lang" "java"]
                              :clojure ["clojure" "ClojureScript"]})]
    (is (= #{} (classify "" topics)))
    (is (= #{} (classify "" {})))
    (is (= #{} (classify "Some text" topics)))
    (is (= #{} (classify "let's talk about wheather" topics)))
    (is (= #{:clojure} (classify "I like Clojure lang" topics)))
    (is (= #{:jvm} (classify "JVM is virtual machine" topics)))
    (is (= #{:clojure :jvm} (classify "Clojure runs under JVM" topics)))))

(deftest get-hashtags-test
  (is (= #{} (get-hashtags "")))
  (is (= #{} (get-hashtags "There are no hashtags")))
  (is (= #{"#football" "#match0-0"} (get-hashtags "It was an awesome #football #match0-0")))
  (is (= #{} (get-hashtags "This is not a hash#tag"))))
