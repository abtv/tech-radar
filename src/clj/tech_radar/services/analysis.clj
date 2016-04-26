(ns tech-radar.services.analysis
  (:require [clojure.core.async :refer [thread go-loop timeout chan <! >! <!! >!! close!]]
            [taoensso.timbre :as timbre]
            [tech-radar.components.counter :refer [increment
                                                   decrement]]
            [tech-radar.database.database :refer [load-topic]]
            [clj-time.local :as local]
            [clj-time.core :as time]
            [clj-time.coerce :refer [to-sql-time]]
            [tech-radar.analytics.protocols :refer [init
                                                    add]]))

(defn load-data [database topics load-data-hours]
  (when-not load-data-hours
    (throw (Exception. "you have to provide load-data-hours param")))
  (let [from (-> (local/local-now)
                 (time/minus (time/hours load-data-hours))
                 (to-sql-time))
        to   (-> (local/local-now)
                 (to-sql-time))]
    (->> topics
         (reduce (fn [data topic]
                   (->> (load-topic database {:topic topic
                                              :from  from
                                              :to    to})
                        (assoc data topic))) {}))))

(defn- reload-model [{:keys [model database topics load-data-hours]}]
  (timbre/info "reloading model")
  (let [data (load-data database topics load-data-hours)]
    (init model data)))

(defn run [{:keys [model analysis-chan metrics load-data-hours] :as params}]
  (go-loop []
    (<! (timeout (* 3600 1000 load-data-hours)))
    (when (>! analysis-chan {:reload true})
      (recur)))
  (thread
    (timbre/info "analysis started")
    (loop []
      (when-let [{:keys [reload] :as tweet} (<!! analysis-chan)]
        (if reload
          (reload-model params)
          (do
            (decrement metrics :analysis-chan)
            (when-not (seq (:topics tweet))
              (timbre/warn (str "Can't classify text: \"" (:text tweet) "\"")))
            (add model tweet)
            (increment metrics :total-texts)))
        (recur)))
    (timbre/info "analysis finished")))
