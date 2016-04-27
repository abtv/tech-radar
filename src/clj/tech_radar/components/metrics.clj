(ns tech-radar.components.metrics
  (:require [tech-radar.services.loader :refer [run]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan close!]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [tech-radar.components.counter :refer [Counter]]
            [tech-radar.services.metrics :refer [run-metrics
                                                 run-system-metrics]]
            [tech-radar.utils.parsers :refer [parse-int]]
            [taoensso.timbre.appenders.3rd-party.rotor :refer [rotor-appender]]))

(defn update-fn [counter update-fn init-val]
  (fn [counters]
    (if (counter counters)
      (update-in counters [counter] update-fn)
      (assoc-in counters [counter] init-val))))

(defrecord Metrics [counters stop-metrics-fn stop-system-metrics-fn]
  Counter
  (increment [component counter]
    (swap! (:counters component) (update-fn counter inc 1)))
  (decrement [component counter]
    (swap! (:counters component) (update-fn counter dec -1)))
  component/Lifecycle
  (start [component]
    (if counters
      component
      (let [log-path (:log-path env)
            max-size (-> env
                         (:max-log-size-mb)
                         (parse-int)
                         (* 1024 1024))
            backlog  (-> env
                         (:backlog)
                         (parse-int))]
        (timbre/merge-config!
          {:appenders {:rotor (rotor-appender {:path     log-path
                                               :max-size max-size
                                               :backlog  backlog})}})
        (timbre/info "Initializing metrics")
        (let [counters          (atom {})
              metrics-timeout-s (-> env
                                    (:metrics-timeout-s)
                                    (parse-int))
              swap-fn           (fn [k v]
                                  (swap! counters assoc-in [k] v))]
          (assoc component :counters counters
                           :stop-metrics-fn (run-metrics counters metrics-timeout-s)
                           :stop-system-metrics-fn (run-system-metrics swap-fn 30))))))
  (stop [component]
    (when stop-metrics-fn
      (timbre/info "Stopping metrics")
      (stop-system-metrics-fn)
      (stop-metrics-fn)
      (dissoc component :counters :stop-metrics-fn :stop-system-metrics-fn))))

(defn new-metrics []
  (map->Metrics {}))
