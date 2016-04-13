(ns tech-radar.utils.parsers
  (:require [clj-time.core :as time]))

(def months {"Jan" 1
             "Feb" 2
             "Mar" 3
             "Apr" 4
             "May" 5
             "Jun" 6
             "Jul" 7
             "Aug" 8
             "Sep" 9
             "Oct" 10
             "Nov" 11
             "Dec" 12})

(defn parse-twitter-date [^String text]
  (try
    (let [year   (-> (subs text 26 30)
                     (Integer/parseInt))
          month  (-> (subs text 4 7)
                     (months))
          day    (-> (subs text 8 10)
                     (Integer/parseInt))
          hour   (-> (subs text 11 13)
                     (Integer/parseInt))
          minute (-> (subs text 14 16)
                     (Integer/parseInt))
          second (-> (subs text 17 19)
                     (Integer/parseInt))]
      (time/date-time year month day hour minute second))
    (catch Exception _
      nil)))
