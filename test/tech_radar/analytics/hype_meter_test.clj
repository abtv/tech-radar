(ns tech-radar.analytics.hype-meter-test
  (:require [clojure.test :refer :all]
            [tech-radar.analytics.hype-meter :as hype-meter]
            [tech-radar.analytics.utils :as utils]))

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
    (is (< (abs (- (hype-meter/calc-similarity ws1 ws2) 0.666)) 0.001))
    (is (< (abs (- (hype-meter/calc-similarity ws1 ws3) 0.333)) 0.001))
    (is (< (abs (- (hype-meter/calc-similarity ws1 ws5) 0.0)) 0.001))
    (is (< (abs (- (hype-meter/calc-similarity ws1 ws4) 0.333)) 0.001))
    (is (< (abs (- (hype-meter/calc-similarity ws5 ws5) 0.0)) 0.001))))

(deftest tweets->bags-test
  (let [stop-words #{"is" "and" "are" "a" "an"}
        tweets     [{:id   1
                     :text "Unit tests are cool"}
                    {:id   2
                     :text "Clojure is a language"}]]
    (is (= [{:id    1
             :words #{"unit" "tests" "cool"}}
            {:id    2
             :words #{"clojure" "language"}}] (hype-meter/tweets->bags tweets stop-words)))))

(deftest reorder-tweets-by-similarity-test
  (let [stop-words           #{"i" "it" "a" "an" "the" "and" "or" "is" "are" "on"}
        texts                ["Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "Zetawar (ClojureScript game based on DataScript) needs just $1K more—please support it! Could make a nice case https://t.co/K3ZBW3YN0Y"
                              "Clojure spec Screencast. Is it possible to build a static analyzer based on specs tests? https://t.co/1F1FzO26o9"
                              "Episode 5 - Hoplon Special with Micha Niskin Now up on SoundCloud https://t.co/7ZmnqH05HJ #Clojure #ClojureScript #Hoplon"
                              "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "RT @richhickey: @stuarthalloway screeencast on the leverage you get with #clojure spec https://t.co/BnBHdjcnYc"
                              "RT @DefnPodcast: Episode 5 - Hoplon Special with Micha Niskin Now up on SoundCloud https://t.co/7ZmnqH05HJ #Clojure #ClojureScript #Hopl…"
                              "Giving a lightning talk on derivatives, a small lib I made for materialised views ins ClojureScript/Rum apps: https://t.co/4pcGFGJlV2"
                              "Очень чётенький курс по clojure. Полистал - весьма последовательно и полезно. https://t.co/nIpetEPROB https://t.co/W375upGeng"
                              "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "RT @nikitonsky: Zetawar (ClojureScript game based on DataScript) needs just $1K more—please support it! Could make a nice case https://t.co…"]
        tweets               (map-indexed (fn [idx text]
                                            {:id   idx
                                             :text text}) texts)
        tweets-bags          (hype-meter/tweets->bags tweets stop-words)
        similarity-threshold 0.5]
    (is (= [] (hype-meter/reorder-tweets-by-similarity [] similarity-threshold)))
    (is (= [0 1 5 6 2 4] (hype-meter/reorder-tweets-by-similarity tweets-bags similarity-threshold)))))

(deftest popular-tweets-test
  (let [stop-words           #{"i" "it" "a" "an" "the" "and" "or" "is" "are" "on"}
        texts                ["Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "Очень чётенький курс по clojure. Полистал - весьма последовательно и полезно. https://t.co/nIpetEPROB https://t.co/W375upGeng"
                              "Zetawar (ClojureScript game based on DataScript) needs just $1K more—please support it! Could make a nice case https://t.co/K3ZBW3YN0Y"
                              "Clojure spec Screencast. Is it possible to build a static analyzer based on specs tests? https://t.co/1F1FzO26o9"
                              "Episode 5 - Hoplon Special with Micha Niskin Now up on SoundCloud https://t.co/7ZmnqH05HJ #Clojure #ClojureScript #Hoplon"
                              "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "RT @richhickey: @stuarthalloway screeencast on the leverage you get with #clojure spec https://t.co/BnBHdjcnYc"
                              "RT @DefnPodcast: Episode 5 - Hoplon Special with Micha Niskin Now up on SoundCloud https://t.co/7ZmnqH05HJ #Clojure #ClojureScript #Hopl…"
                              "Giving a lightning talk on derivatives, a small lib I made for materialised views ins ClojureScript/Rum apps: https://t.co/4pcGFGJlV2"
                              "RT @planetclojure: Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"
                              "RT @nikitonsky: Zetawar (ClojureScript game based on DataScript) needs just $1K more—please support it! Could make a nice case https://t.co…"]
        tweets               (map-indexed (fn [idx text]
                                            {:id   idx
                                             :text text}) texts)
        similarity-threshold 0.5]
    (is (= [] (hype-meter/popular-tweets [] {:stop-words #{}
                                             :hype-count 10})))
    (is (= [{:id   0
             :text "Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"}]
           (hype-meter/popular-tweets tweets {:stop-words           stop-words
                                              :hype-count           1
                                              :similarity-threshold similarity-threshold})))
    (is (= [{:id   0
             :text "Clojure spec Screencast: Leverage https://t.co/GSRJ5pH4rK"}
            {:id   3
             :text "Zetawar (ClojureScript game based on DataScript) needs just $1K more—please support it! Could make a nice case https://t.co/K3ZBW3YN0Y"}
            {:id   5
             :text "Episode 5 - Hoplon Special with Micha Niskin Now up on SoundCloud https://t.co/7ZmnqH05HJ #Clojure #ClojureScript #Hoplon"}]
           (hype-meter/popular-tweets tweets {:stop-words           stop-words
                                              :hype-count           10
                                              :similarity-threshold similarity-threshold})))))
