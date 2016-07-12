(ns tech-radar.analytics.search-test
  (:require [clojure.test :refer :all]
            [clj-time.core :refer [now]]
            [tech-radar.analytics.search :refer [new-search
                                                 add-text
                                                 search-texts
                                                 remove-oldest-item]]))

(deftest search-test
  (let [search     (new-search)
        created-at (now)
        t1         {:id         1
                    :text       "some cool text #rust-lang"
                    :twitter-id 100
                    :created-at created-at}
        t2         {:id         2
                    :text       "another cool text"
                    :twitter-id 100
                    :created-at created-at}
        t3         {:id         3
                    :text       "very bad text"
                    :twitter-id 300
                    :created-at created-at}
        t4         {:id         4
                    :text       "very good text"
                    :twitter-id 400
                    :created-at created-at}
        statistic  (atom {})]
    (add-text search (assoc t1 :topics #{:cool}) statistic)
    (add-text search (assoc t2 :topics #{:cool}) statistic)
    (add-text search (assoc t3 :topics #{:very :bad}) statistic)
    (add-text search (assoc t4 :topics #{:very :good}) statistic)
    (is (= (into (sorted-map) {1 (dissoc t1 :id)
                               2 (dissoc t2 :id)
                               3 (dissoc t3 :id)
                               4 (dissoc t4 :id)})
           (:texts @search)))
    (is (= {:cool {"some"       [1]
                   "cool"       [1 2]
                   "text"       [1 2]
                   "another"    [2]
                   "#rust-lang" [1]}
            :very {"very" [3 4]
                   "bad"  [3]
                   "text" [3 4]
                   "good" [4]}
            :bad  {"very" [3]
                   "bad"  [3]
                   "text" [3]}
            :good {"very" [4]
                   "good" [4]
                   "text" [4]}}
           (:index @search)))
    (is (= {:total 1
            :texts [t1]} (search-texts search :cool "#rust-lang")))
    (is (= {:total 2
            :texts [t1 t2]} (search-texts search :cool "cool")))
    (is (= {:total 1
            :texts [t3]} (search-texts search :bad "bad")))
    (is (= {:total 2
            :texts [t1 t2]} (search-texts search :cool "cool text")))
    (is (= {:total 1
            :texts [t4]} (search-texts search :very "text good")))
    (is (= {:total 1
            :texts [t4]} (search-texts search :very "good text")))
    (is (= {:total 1
            :texts [t4]} (search-texts search :very "very good text")))
    (is (= {:total 0
            :texts []} (search-texts search :cool "good text")))))

(deftest remove-test
  (let [search     (new-search)
        created-at (now)
        t1         {:id         1
                    :text       "some cool text #rust-lang"
                    :twitter-id 100
                    :created-at created-at}
        t2         {:id         2
                    :text       "another cool text"
                    :twitter-id 100
                    :created-at created-at}
        t3         {:id         3
                    :text       "very bad text"
                    :twitter-id 300
                    :created-at created-at}
        t4         {:id         4
                    :text       "very good text"
                    :twitter-id 400
                    :created-at created-at}
        statistic  (atom {})]
    (add-text search (assoc t1 :topics #{:cool}) statistic)
    (add-text search (assoc t2 :topics #{:cool}) statistic)
    (add-text search (assoc t3 :topics #{:very :bad}) statistic)
    (add-text search (assoc t4 :topics #{:very :good}) statistic)
    (remove-oldest-item search statistic)
    (is (= (into (sorted-map) {2 (dissoc t2 :id)
                               3 (dissoc t3 :id)
                               4 (dissoc t4 :id)})
           (:texts @search)))
    (is (= {:cool {"cool"    [2]
                   "text"    [2]
                   "another" [2]}
            :very {"very" [3 4]
                   "bad"  [3]
                   "text" [3 4]
                   "good" [4]}
            :bad  {"very" [3]
                   "bad"  [3]
                   "text" [3]}
            :good {"very" [4]
                   "good" [4]
                   "text" [4]}}
           (:index @search)))))

(deftest search-without-duplicates-test
  (let [search     (new-search)
        created-at (now)
        t1         {:id         1
                    :text       "a word can occur more than one time, but search results should contain only one record per relevant text"
                    :twitter-id 100
                    :created-at created-at}
        statistic  (atom {})]
    (add-text search (assoc t1 :topics #{:cool}) statistic)
    (is (= {:total 1
            :texts [t1]} (search-texts search :cool "one")))))
