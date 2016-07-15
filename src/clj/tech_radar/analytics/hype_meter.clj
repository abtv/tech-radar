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

(defn calc-total [tweets-bags current similarity-threshold]
  (let [{:keys [words]} (nth tweets-bags current)]
    (loop [acc     0
           current (inc current)]
      (if (< current (count tweets-bags))
        (let [sim (calc-similarity words (-> (nth tweets-bags current)
                                             (:words)))]
          (recur (if (>= sim similarity-threshold)
                   (+ acc sim)
                   acc) (inc current)))
        acc))))

(defn reorder-tweets-by-similarity [tweets-bags similarity-threshold]
  (let [weights (loop [weights (transient [])
                       i       0]
                  (if (< i (dec (count tweets-bags)))
                    (recur (conj! weights [i (calc-total tweets-bags i similarity-threshold)]) (inc i))
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

(defn popular-tweets [tweets {:keys [stop-words hype-count similarity-threshold]}]
  (let [tweets-bags     (tweets->bags tweets stop-words)
        ordered-indices (reorder-tweets-by-similarity tweets-bags similarity-threshold)
        get-tweet       (fn [tweet-index]
                          (nth tweets tweet-index))
        get-words       (fn [ordered-index]
                          (->> (nth ordered-indices ordered-index)
                               (nth tweets-bags)
                               (:words)))
        unique-indices  (mapv (fn [_]
                                true) ordered-indices)]
    (if (seq ordered-indices)
      (loop [i              0
             unique-indices unique-indices]
        (if (< i (count ordered-indices))
          (let [words          (get-words i)
                unique-indices (if (nth unique-indices i)
                                 (loop [j              (inc i)
                                        unique-indices unique-indices]
                                   (if (< j (count ordered-indices))
                                     (let [words2  (get-words j)
                                           sim     (calc-similarity words words2)
                                           similar (>= sim similarity-threshold)]
                                       (recur (inc j) (if similar
                                                        (assoc unique-indices j false)
                                                        unique-indices)))
                                     unique-indices))
                                 unique-indices)]
            (recur (inc i) unique-indices))
          (->> ordered-indices
               (map-indexed (fn [idx tweet-idx]
                              [idx tweet-idx]))
               (filter (fn [[idx tweet-idx]]
                         (nth unique-indices idx)))
               (map (comp get-tweet second))
               (take hype-count))))
      [])))
