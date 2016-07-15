(ns tech-radar.services.hype-meter
  (:require [tech-radar.analytics.hype-meter :as hype-meter]
            [tech-radar.database.tweets :as tweets]
            [taoensso.timbre :as timbre]))

(defn run-hype-meter [{:keys [database topics hype-tweet-count similarity-threshold]}]
  (reduce (fn [acc topic]
            (let [tweets         (tweets/load-daily-tweets-per-topic database {:topic            topic
                                                                               :max-record-count hype-tweet-count})
                  popular-tweets (hype-meter/popular-tweets tweets {:stop-words           #{}
                                                                    :hype-count           10
                                                                    :similarity-threshold similarity-threshold})]
              (assoc acc topic popular-tweets))) {} topics))

(defn new-hype-meter-fn [{:keys [cache database topics hype-tweet-count similarity-threshold]}]
  (let [busy (atom false)]
    (fn []
      (when-not @busy
        (try
          (reset! busy true)
          (timbre/info "hype-meter-job: start")
          (let [start          (. System (nanoTime))
                popular-tweets (run-hype-meter {:database             database
                                                :topics               topics
                                                :hype-tweet-count     hype-tweet-count
                                                :similarity-threshold similarity-threshold})]
            (swap! cache (fn [cache]
                           (reduce (fn [cache [topic tweets]]
                                     (assoc-in cache [topic :popular-tweets] tweets))
                                   cache popular-tweets)))
            (let [elapsed-time (/ (double (- (. System (nanoTime)) start)) 1000000.0)]
              (timbre/info (str "hype-meter-job: finish (" elapsed-time " msecs)"))))
          (catch Exception ex
            (timbre/error ex "hype-meter-job failed"))
          (finally
            (reset! busy false)))))))
