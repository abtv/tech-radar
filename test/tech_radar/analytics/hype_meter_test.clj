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
