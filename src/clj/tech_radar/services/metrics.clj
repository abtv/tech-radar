(ns tech-radar.services.metrics
  (:require [clojure.core.async :refer [chan close! go go-loop timeout >! <! >!! <!! alts!]]
            [taoensso.timbre :as timbre]))

(defn- write-metrics [counters]
  (let [m (reduce (fn [m [k v]]
                    (str m "\n" k ": " v)) "" @counters)]
    (timbre/info m)))

(defn run-metrics [counters metrics-timeout-s]
  (when-not metrics-timeout-s
    (throw (Exception. "you have to provide metrics-timeout-s param")))
  (let [write-timeout (* 1000 metrics-timeout-s)
        write-chan    (chan)
        process       (go-loop []
                        (let [value (<! write-chan)]
                          (when value
                            (write-metrics counters)
                            (recur))))
        stop-fn       (fn []
                        (close! write-chan)
                        (<!! process))]
    (go
      (while (>! write-chan true)
        (<! (timeout write-timeout))))
    stop-fn))
