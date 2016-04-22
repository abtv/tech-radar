(ns tech-radar.services.analysis
  (:require [clojure.core.async :refer [thread chan <!! >!! close!]]
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

(defn run [{:keys [model analysis-chan metrics]}]
  (thread
    (timbre/info "analysis started")
    (loop []
      (when-let [tweet (<!! analysis-chan)]
        (decrement metrics :analysis-chan)
        (add model tweet)
        (increment metrics :total-texts)
        (recur)))
    (timbre/info "analysis finished")))
