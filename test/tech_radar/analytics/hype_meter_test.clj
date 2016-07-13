(ns tech-radar.analytics.hype-meter-test
  (:require [clojure.test :refer :all]
            [tech-radar.analytics.utils :as utils]
            [clojure.set :as set]))

(defn calc-similarity [ws1 ws2]
  (let [c1 (count ws1)
        c2 (count ws2)
        ci (-> (set/intersection ws1 ws2)
               (count))
        cm (max c1 c2)]
    (if (= cm 0)
      0
      (/ (double ci) (double cm)))))

(defn- abs [x]
  (if (> x 0)
    x
    (- x)))

(deftest calc-similarity-test
  (let [s1  "Clojure.Spec is awesome"
        s2  "clojure is awesome"
        s3  "javascript is awesome"
        s4  "boom-boom is awesome"
        s5  ""
        ws1 (utils/get-words s1 #{"is"})
        ws2 (utils/get-words s2 #{"is"})
        ws3 (utils/get-words s3 #{"is"})
        ws4 (utils/get-words s4 #{"is"})
        ws5 (utils/get-words s5 #{"is"})]
    (is (< (abs (- (calc-similarity ws1 ws2) 0.666)) 0.001))
    (is (< (abs (- (calc-similarity ws1 ws3) 0.333)) 0.001))
    (is (< (abs (- (calc-similarity ws1 ws5) 0.0)) 0.001))
    (is (< (abs (- (calc-similarity ws5 ws5) 0.0)) 0.001))))

(defn tweets->bags [tweets stop-words-set]
  (mapv (fn [{:keys [id text]}]
          {:id    id
           :words (utils/get-words text stop-words-set)}) tweets))

(deftest tweets->bags-test
  (let [stop-words #{"is" "and" "are" "a" "an"}
        tweets     [{:id   1
                     :text "Unit tests are cool"}
                    {:id   2
                     :text "Clojure is a language"}]]
    (is (= [{:id    1
             :words #{"unit" "tests" "cool"}}
            {:id    2
             :words #{"clojure" "language"}}] (tweets->bags tweets stop-words)))))

(defn calc-total [tweets-bags current]
  (let [{:keys [words]} (nth tweets-bags current)]
    (loop [acc     0
           current (inc current)]
      (if (< current (count tweets-bags))
        (let [sim (calc-similarity words (-> (nth tweets-bags current)
                                             (:words)))]
          (recur (if (>= sim 0.5)
                   (+ acc sim)
                   acc) (inc current)))
        acc))))

(defn hype-meter [tweets {:keys [stop-words hype-count]}]
  (let [tweets  (tweets->bags tweets stop-words)
        weights (loop [weights []
                       i       0]
                  (if (< i (dec (count tweets)))
                    (recur (conj weights [i (calc-total tweets i)]) (inc i))
                    weights))]
    ;TODO while this is ok to show hype-count records for the first time, we still need to analyze resulting set (we can have possible duplicates)
    (->> weights
         (sort-by identity (fn [[id1 weight1]
                                [id2 weight2]]
                             (cond
                               (< weight2 weight1) -1
                               (= weight2 weight1) (>= id2 id1)
                               :else 1)))
         (take hype-count)
         (mapv first))))

(deftest hype-meter-test
  (let [stop-words #{"i" "it" "a" "an" "the" "and" "or" "is" "are" "on"}
        texts      ["Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                    "Zetawar (ClojureScript game based on DataScript) needs just $1K more—please support it! Could make a nice case https://t.co/K3ZBW3YN0Y"
                    "Clojure spec Screencast. Is it possible to build a static analyzer based on specs tests? https://t.co/1F1FzO26o9"
                    "Episode 5 - Hoplon Special with Micha Niskin Now up on SoundCloud https://t.co/7ZmnqH05HJ #Clojure #ClojureScript #Hoplon"
                    "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                    "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                    "RT @richhickey: @stuarthalloway screeencast on the leverage you get with #clojure spec https://t.co/BnBHdjcnYc"
                    "RT @DefnPodcast: Episode 5 - Hoplon Special with Micha Niskin Now up on SoundCloud https://t.co/7ZmnqH05HJ #Clojure #ClojureScript #Hopl…"
                    "Giving a lightning talk on derivatives, a small lib I made for materialised views ins ClojureScript/Rum apps: https://t.co/4pcGFGJlV2"
                    "Очень чётенький курс по clojure. Полистал - весьма последовательно и полезно. https://t.co/nIpetEPROB https://t.co/W375upGeng"
                    "RT @nikitonsky: Zetawar (ClojureScript game based on DataScript) needs just $1K more—please support it! Could make a nice case https://t.co…"]
        tweets     (map-indexed (fn [idx text]
                                  {:id   idx
                                   :text text}) texts)]
    (is (= [] (hype-meter [] {:stop-words #{}
                              :hype-count 10})))
    (is (= [0 4 1] (hype-meter tweets {:stop-words stop-words
                                   :hype-count 3})))))
