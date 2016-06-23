(ns tech-radar.analytics.model-test
  (:require [clojure.test :refer :all]
            [clj-time.core :refer [now]]
            [tech-radar.analytics.model :refer [new-model]]
            [tech-radar.analytics.protocols :refer [init
                                                    add
                                                    trends
                                                    texts
                                                    search]]))

(deftest empty-test
  (let [model (new-model nil {:max-tweet-count         1000
                              :max-hashtags-per-trend  25
                              :max-texts-per-request   200
                              :hashtag-filter-settings {}})
        t1    {:id         1
               :text       "just a plain text"
               :created-at (now)
               :hashtags   []
               :topics     []}]
    (init model {:topics {:nosql {:texts    []
                                  :hashtags {}}}
                 :tweets []})
    (add model t1)
    (let [trends*       (trends model)
          unknown-topic (texts model :unknown)
          nosql         (texts model :nosql)]
      (is (= {:nosql {}}
             trends*))
      (is (= [] unknown-topic))
      (is (= [] nosql)))))

(deftest add-test
  (let [model      (new-model nil {:max-tweet-count         1000
                                   :max-hashtags-per-trend  25
                                   :max-texts-per-request   200
                                   :hashtag-filter-settings {:clojure #{"stop-word" "Another-word"}
                                                             :linux   #{"stop-word" "Another-Word"}}})
        _ (prn model)
        created-at (now)
        topic-item (fn [tweet]
                     (select-keys tweet [:id :text :created-at :twitter-id]))
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
                    :topics     [:clojure]}
        e1         (topic-item t1)
        e2         (topic-item t2)
        e3         (topic-item t3)]
    (init model {:topics {:nosql {:texts    []
                                  :hashtags {}}}
                 :tweets []})
    (add model t1)
    (add model t2)
    (add model t3)
    (let [trends*          (trends model)
          clojure-topic    (texts model :clojure)
          javascript-topic (texts model :javascript)
          linux-topic      (texts model :linux)
          nosql            (texts model :nosql)]
      (is (= {:clojure    {:daily {"react"  2
                                   "ubuntu" 2}}
              :linux      {:daily {"react"  1
                                   "ubuntu" 2}}
              :javascript {:daily {"react" 1}}
              :nosql      {}}
             trends*))
      (is (= [e1 e3] clojure-topic))
      (is (= [e2] javascript-topic))
      (is (= [e1] linux-topic))
      (is (= [] nosql)))
    (is (= {:total 1
            :texts [{:id         2
                     :text       "React in JavaScript #react"
                     :created-at created-at
                     :twitter-id 20}]} (search model :javascript "#react")))
    (is (= {:total 0
            :texts []} (search model :no-topic "#react")))
    (is (= {:total 2
            :texts [{:id         1
                     :text       "React in Clojure under Linux #react #ubuntu with #Stop-word and #another-word"
                     :created-at created-at
                     :twitter-id 10}
                    {:id         3
                     :text       "React in ClojureScript #react"
                     :created-at created-at
                     :twitter-id 30}]} (search model :clojure "#react")))))
