(ns tech-radar.analytics.utils)

(def word #"[#]?[\p{L}0-9\-]+")

(defn get-words [^String text]
  (->> (.toLowerCase text)
       (re-seq word)
       (set)))

(def hashtag #"\B#[\p{L}^\-]+[\p{L}0-9\-^\-]*[\p{L}0-9]+")

(defn get-hashtags [^String text]
  (->> (re-seq hashtag text)
       (set)))
