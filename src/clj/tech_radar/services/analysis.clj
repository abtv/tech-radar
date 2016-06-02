(ns tech-radar.services.analysis
  (:require [clojure.core.async :refer [thread go-loop timeout chan <! >! <!! >!! close! alts!!]]
            [taoensso.timbre :as timbre]
            [tech-radar.components.counter :refer [increment
                                                   decrement]]
            [tech-radar.database.tweets :refer [load-tweets-per-topic
                                                load-tweets]]
            [tech-radar.database.hashtags :refer [load-daily-hashtags
                                                  load-weekly-hashtags
                                                  load-monthly-hashtags]]
            [clj-time.coerce :refer [to-sql-time]]
            [tech-radar.analytics.protocols :refer [init
                                                    add
                                                    trends
                                                    texts
                                                    reset-trends]]
            [tech-radar.analytics.cache :refer [set-cached-trends]]
            [clj-time.local :as local]
            [clj-time.core :as time]))

(defn- load-topic [database {:keys [topic max-record-count]}]
  (let [tweets  (load-tweets-per-topic database {:topic            topic
                                                 :max-record-count max-record-count})
        daily   (load-daily-hashtags database topic)
        weekly  (load-weekly-hashtags database topic)
        monthly (load-monthly-hashtags database topic)]
    {:texts    tweets
     :hashtags {:daily   daily
                :weekly  weekly
                :monthly monthly}}))

(defn load-data [database {:keys [topics max-texts-per-request max-tweet-count]}]
  (when-not max-tweet-count
    (throw (Exception. "you have to provide max-tweet-count param")))
  (when-not max-texts-per-request
    (throw (Exception. "you have to provide max-texts-per-request param")))
  {:topics (reduce (fn [data topic]
                     (->> (load-topic database {:topic            topic
                                                :max-record-count max-texts-per-request})
                          (assoc data topic))) {} topics)
   :tweets (load-tweets database max-tweet-count)})

(defmulti load-hashtags (fn [type database topic]
                          type))

(defmethod load-hashtags :daily [_ database topic]
  (load-daily-hashtags database topic))

(defmethod load-hashtags :weekly [_ database topic]
  (load-weekly-hashtags database topic))

(defmethod load-hashtags :monthly [_ database topic]
  (load-monthly-hashtags database topic))

(defn- reload-hashtags [type database topics]
  (map (fn [topic]
         [topic (load-hashtags type database topic)])
       topics))

(defn run-hashtags-update [{:keys [database topics analysis-chan metrics]}]
  (let [stop-chan   (chan)
        last-update (atom {:daily   (local/local-now)
                           :weekly  (local/local-now)
                           :monthly (local/local-now)})
        exceed?     (fn [type offset]
                      (let [now              (local/local-now)
                            last-update-time (type @last-update)]
                        (time/before? (time/plus last-update-time offset) now)))
        reload-fn   (fn [hashtag-type]
                      (increment metrics :analysis-chan)
                      (>!! analysis-chan {:type         :hashtags-update
                                          :hashtag-type hashtag-type
                                          :hashtags     (reload-hashtags hashtag-type database topics)})
                      (swap! last-update assoc-in [hashtag-type] (local/local-now)))
        process     (thread
                      (loop []
                        (let [daily-offset   (time/hours 1)
                              weekly-offset  (time/hours 12)
                              monthly-offset (time/hours 24)
                              reload         (timeout 1000)
                              [_ c] (alts!! [reload stop-chan])]
                          (when (= reload c)
                            (when (exceed? :daily daily-offset)
                              (reload-fn :daily))
                            (when (exceed? :weekly weekly-offset)
                              (reload-fn :weekly))
                            (when (exceed? :monthly monthly-offset)
                              (reload-fn :monthly))
                            (recur)))))
        stop-fn     (fn []
                      (close! stop-chan)
                      (<!! process))]
    stop-fn))

(defn run-model-update [{:keys [model analysis-chan metrics]}]
  (thread
    (timbre/info "analysis started")
    (loop []
      (when-let [{:keys [type tweet hashtag-type hashtags]} (<!! analysis-chan)]
        (decrement metrics :analysis-chan)
        (case type
          :tweet (if (seq (:topics tweet))
                   (do
                     (add model tweet)
                     (increment metrics :total-texts))
                   (timbre/warn (str "Can't classify text: \"" (:text tweet) "\"")))
          :hashtags-update (reset-trends model hashtag-type hashtags))
        (recur)))
    (timbre/info "analysis finished")))

(defn cache-update-fn [model cache]
  (timbre/info "cache update")
  (->> (trends model)
       (set-cached-trends cache)))

(defn run-cache-update [{:keys [model cache cache-update-timeout-s]}]
  (let [stop-chan (chan)
        process   (thread
                    (timbre/info "cache update started")
                    (loop []
                      (let [tmt (timeout (* 1000 cache-update-timeout-s))
                            [_ c] (alts!! [tmt stop-chan])]
                        (when (= tmt c)
                          (cache-update-fn model cache)
                          (recur))))
                    (timbre/info "cache update finished"))]
    (fn []
      (close! stop-chan)
      (<!! process))))
