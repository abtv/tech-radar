(ns tech-radar.components.metrics
  (:require [tech-radar.services.loader :refer [run]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan close!]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [tech-radar.components.counter :refer [Counter]]
            [tech-radar.services.metrics :refer [run-metrics]]
            [tech-radar.utils.parsers :refer [parse-int]]))

(defn update-fn [counter update-fn init-val]
  (fn [counters]
    (if (counter counters)
      (update-in counters [counter] update-fn)
      (assoc-in counters [counter] init-val))))

(defrecord Metrics [counters stop-fn]
  Counter
  (increment [component counter]
    (swap! (:counters component) (update-fn counter inc 1)))
  (decrement [component counter]
    (swap! (:counters component) (update-fn counter dec -1)))
  component/Lifecycle
  (start [component]
    (if counters
      component
      (do
        (timbre/info "Initializing metrics")
        (let [counters          (atom {})
              metrics-timeout-s (-> env
                                    (:metrics-timeout-s)
                                    (parse-int))]
          (assoc component :counters counters
                           :stop-fn (run-metrics counters metrics-timeout-s))))))
  (stop [component]
    (when stop-fn
      (timbre/info "Stopping metrics")
      (stop-fn)
      (dissoc component :counters :stop-fn))))

(defn new-metrics []
  (map->Metrics {}))
