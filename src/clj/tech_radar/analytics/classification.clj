(ns tech-radar.analytics.classification
  (:require [clojure.set :refer [intersection]]
            [tech-radar.analytics.utils :refer [get-words]]))

(defn- index-phrases [phrases]
  (map get-words phrases))

(defn index-topics [topics]
  (->> topics
       (map (fn [[name phrases]]
              [name (index-phrases phrases)]))
       (into {})))

(defn- remove-hashtag-signs [words-set]
  (->> words-set
       (map (fn [^String word]
              (when word
                (if (.startsWith word "#")
                  (subs word 1)
                  word))))
       (into #{})))

(defn classify [^String text indexed-topics]
  (let [words (-> (get-words text)
                  (remove-hashtag-signs))]
    (->> indexed-topics
         (filter (fn [[_ phrases]]
                   (some (fn [phrase]
                           (= phrase (intersection words phrase))) phrases)))
         (map first)
         (set))))
