(ns tech-radar.services.hype-meter
  (:require [tech-radar.analytics.hype-meter :as hype-meter]
            [tech-radar.database.tweets :as tweets]))

(defn run-hype-meter [{:keys [database topics]}]
  (prn "hype-meter: begin")
  (let [top (reduce (fn [acc topic]
                      (let [tweets         (tweets/load-daily-tweets-per-topic database {:topic            topic
                                                                                         :max-record-count 2000})
                            _              (prn topic)
                            _              (prn (count tweets))
                            popular-tweets (hype-meter/popular-tweets tweets {:stop-words #{}
                                                                              :hype-count 10})]
                        (prn topic)
                        (prn (map :text popular-tweets))
                        (assoc acc topic popular-tweets))) {} topics)]
    (prn "hype-meter: end")
    top))
