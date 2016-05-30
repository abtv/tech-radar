(ns tech-radar.analytics.model-test
  (:require [clojure.test :refer :all]
            [clj-time.core :refer [now]]
            [tech-radar.analytics.model :refer [new-model]]
            [tech-radar.analytics.protocols :refer [init
                                                    add
                                                    trends
                                                    texts]]))

(deftest empty-test
  (let [model (new-model nil {:max-tweet-count        1000
                              :max-hashtags-per-trend 25
                              :max-texts-per-request  200})
        t1    {:id         1
               :text       "just a plain text"
               :created-at (now)
               :hashtags   []
               :topics     []}]
    (init model {:nosql {:texts    []
                         :hashtags {}}})
    (add model t1)
    (let [trends*       (trends model)
          unknown-topic (texts model :unknown)
          nosql         (texts model :nosql)]
      (is (= {:nosql {}}
             trends*))
      (is (= [] unknown-topic))
      (is (= [] nosql)))))

(deftest add-test
  (let [model      (new-model nil {:max-tweet-count        1000
                                   :max-hashtags-per-trend 25
                                   :max-texts-per-request  200})
        topic-item (fn [tweet]
                     (select-keys tweet [:id :text :created-at]))
        t1         {:id         1
                    :text       "React in Clojure under Linux #react #ubuntu"
                    :created-at (now)
                    :hashtags   ["react" "ubuntu"]
                    :topics     [:clojure :linux]}
        t2         {:id         2
                    :text       "React in JavaScript #react"
                    :created-at (now)
                    :hashtags   ["react"]
                    :topics     [:javascript]}
        t3         {:id         3
                    :text       "React in ClojureScript #react"
                    :created-at (now)
                    :hashtags   ["react"]
                    :topics     [:clojure]}
        e1         (topic-item t1)
        e2         (topic-item t2)
        e3         (topic-item t3)]
    (init model {:nosql {:texts    []
                         :hashtags {}}})
    (add model t1)
    (add model t2)
    (add model t3)
    (let [trends*          (trends model)
          clojure-topic    (texts model :clojure)
          javascript-topic (texts model :javascript)
          linux-topic      (texts model :linux)
          nosql            (texts model :nosql)]
      (is (= {:clojure    {:daily {"react"  2
                                   "ubuntu" 1}}
              :linux      {:daily {"react"  1
                                   "ubuntu" 1}}
              :javascript {:daily {"react" 1}}
              :nosql      {}}
             trends*))
      (is (= [e1 e3] clojure-topic))
      (is (= [e2] javascript-topic))
      (is (= [e1] linux-topic))
      (is (= [] nosql)))))
