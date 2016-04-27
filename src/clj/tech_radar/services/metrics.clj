(ns tech-radar.services.metrics
  (:require [clojure.core.async :refer [chan close! go go-loop timeout >! <! >!! <!! alts!]]
            [taoensso.timbre :as timbre]))

(defn- write-metrics [counters]
  (let [m (reduce (fn [m [k v]]
                    (str m "\n" k ": " v)) "" @counters)]
    (timbre/info m)))

(defn- bytes->megabytes [v]
  (-> v
      (/ (* 1024 1024))
      (long)))

(defn- system-metrics []
  (let [runtime (Runtime/getRuntime)]
    {:total-mb (-> (.totalMemory runtime)
                   (bytes->megabytes))
     :free-mb  (-> (.freeMemory runtime)
                   (bytes->megabytes))
     :max-mb   (-> (.maxMemory runtime)
                   (bytes->megabytes))}))

(defn run-system-metrics [swap-fn timeout-s]
  (when-not timeout-s
    (throw (Exception. "you have to provide timeout-s param")))
  (let [timer-chan    (chan)
        timer-timeout (* 1000 timeout-s)
        process       (go-loop []
                        (when (<! timer-chan)
                          (let [sm (system-metrics)]
                            (doseq [[k v] sm]
                              (swap-fn k v)))))
        stop-fn       (fn []
                        (close! timer-chan)
                        (<!! process))]
    (go
      (while (>! timer-chan true)
        (<! (timeout timer-timeout))))
    stop-fn))

(defn run-metrics [counters metrics-timeout-s]
  (when-not metrics-timeout-s
    (throw (Exception. "you have to provide metrics-timeout-s param")))
  (let [write-timeout (* 1000 metrics-timeout-s)
        write-chan    (chan)
        process       (go-loop []
                        (when (<! write-chan)
                          (write-metrics counters)
                          (recur)))
        stop-fn       (fn []
                        (close! write-chan)
                        (<!! process))]
    (go
      (while (>! write-chan true)
        (<! (timeout write-timeout))))
    stop-fn))
