(ns tech-radar.analytics.hype-meter
  (:require [clojure.set :as set]
            [tech-radar.analytics.utils :as utils]))

(defn calc-similarity [ws1 ws2]
  (let [c1 (count ws1)
        c2 (count ws2)
        ci (-> (set/intersection ws1 ws2)
               (count))
        cm (max c1 c2)]
    (if (= cm 0)
      0
      (/ (double ci) (double cm)))))

(defn tweets->bags [tweets stop-words-set]
  (mapv (fn [{:keys [id text]}]
          {:id    id
           :words (utils/get-words text stop-words-set)}) tweets))

(defn calc-total [tweets-bags current]
  (let [similarity-threshold 0.5
        {:keys [words]} (nth tweets-bags current)]
    (loop [acc     0
           current (inc current)]
      (if (< current (count tweets-bags))
        (let [sim (calc-similarity words (-> (nth tweets-bags current)
                                             (:words)))]
          (recur (if (>= sim similarity-threshold)
                   (+ acc sim)
                   acc) (inc current)))
        acc))))

(defn reorder-tweets-by-similarity [tweets-bags]
  (let [weights (loop [weights (transient [])
                       i       0]
                  (if (< i (dec (count tweets-bags)))
                    (recur (conj! weights [i (calc-total tweets-bags i)]) (inc i))
                    (persistent! weights)))]
    (->> weights
         (filter (fn [[id weight]]
                   (> weight 0)))
         (sort-by identity (fn [[id1 weight1]
                                [id2 weight2]]
                             (cond
                               (< weight2 weight1) -1
                               (= weight2 weight1) (>= id2 id1)
                               :else 1)))
         (mapv first))))

(defn popular-tweets [tweets {:keys [stop-words hype-count]}]
  (let [tweets-bags          (tweets->bags tweets stop-words)
        ordered-indices      (reorder-tweets-by-similarity tweets-bags)
        get-tweet            (fn [ordered-index]
                               (->> (nth ordered-indices ordered-index)
                                    (nth tweets)))
        get-words            (fn [ordered-index]
                               (->> (nth ordered-indices ordered-index)
                                    (nth tweets-bags)
                                    (:words)))
        similarity-threshold 0.5]
    (if (seq ordered-indices)
      (loop [popular-tweets (transient [(get-tweet 0)])
             words          (get-words 0)
             index          1]
        (if (< index (count ordered-indices))
          (let [words2  (get-words index)
                sim     (calc-similarity words words2)
                similar (> sim similarity-threshold)]
            (recur (if similar
                     popular-tweets
                     (conj! popular-tweets (get-tweet index)))
                   (if similar
                     words
                     (get-words index))
                   (inc index)))
          (->> (persistent! popular-tweets)
               (take hype-count))))
      [])))
