(ns tech-radar.services.classifier
  (:require [clojure.set :refer [intersection]]))

(def word #"[\w\-]+")

(def hashtag #"\B#\w*[\w-]+\w*")

(defn- index-phrases [phrases]
  (map (fn [phrase]
         (->> (re-seq word phrase)
              (map (fn [^String s]
                     (.toLowerCase s)))
              (set)))
       phrases))

(defn index-topics [topics]
  (->> topics
       (map (fn [[name phrases]]
              [name (index-phrases phrases)]))
       (into {})))

(defn classify [^String text indexed-topics]
  (let [words (->> (.toLowerCase text)
                   (re-seq word)
                   (set))]
    (->> indexed-topics
         (filter (fn [[_ phrases]]
                   (some (fn [phrase]
                           (= phrase (intersection words phrase))) phrases)))
         (map first)
         (set))))

(defn get-hashtags [text]
  (->> (re-seq hashtag text)
       (set)))
