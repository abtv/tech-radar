(ns tech-radar.analytics.statistic-test
  (:require [clojure.test :refer :all]
            [clj-time.core :refer [now]]
            [tech-radar.analytics.model :refer [new-model]]
            [tech-radar.analytics.protocols :as protocols]))

(deftest statistic-test
  (let [model      (new-model nil {:max-tweet-count         1000
                                   :max-hashtags-per-trend  25
                                   :max-texts-per-request   200
                                   :hashtag-filter-settings {}})
        created-at (now)
        t1         {:id         1
                    :text       "React in Clojure under Linux #react #ubuntu with #Stop-word and #another-word"
                    :created-at created-at
                    :twitter-id 10
                    :hashtags   ["react" "Ubuntu" "ubuntu" "Stop-word" "another-word"]
                    :topics     [:clojure :linux]}
        t2         {:id         2
                    :text       "React in JavaScript #react"
                    :created-at created-at
                    :twitter-id 20
                    :hashtags   ["react"]
                    :topics     [:javascript]}
        t3         {:id         3
                    :text       "React in ClojureScript #react"
                    :created-at created-at
                    :twitter-id 30
                    :hashtags   ["react"]
                    :topics     [:clojure]}]
    (protocols/init model {:topics {:nosql {:texts    []
                                            :hashtags {}}}
                           :tweets []})
    (protocols/add model t1)
    (protocols/add model t2)
    (protocols/add model t3)
    (is (= [{:hashtag :clojure
             :count   2}
            {:hashtag :javascript
             :count   1}
            {:hashtag :linux
             :count   1}] (protocols/statistic model)))))
