(ns tech-radar.analytics.utils)

(def word #"[#]?[\p{L}0-9\-]+")

#_(defn get-words [^String text]
    (->> (.toLowerCase text)
         (re-seq word)
         (set)))

(defn get-words
  ([^String text]
   (->> (.toLowerCase text)
        (re-seq word)
        (set)))
  ([^String text stop-words-set]
   (->> (.toLowerCase text)
        (re-seq word)
        (filter (comp not stop-words-set))
        (set))))

(def hashtag #"\B#[\p{L}^\-]+[\p{L}0-9\-^\-]*[\p{L}0-9]+")

(defn get-hashtags [^String text]
  (->> (re-seq hashtag text)
       (set)))
