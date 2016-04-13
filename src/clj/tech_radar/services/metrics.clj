(ns tech-radar.services.metrics
  (:require [clojure.core.async :refer [chan close! go go-loop timeout >! <! >!! <!! alts!]]
            [taoensso.timbre :as timbre]))
;TODO add logger to file from timbre

(defn- write-metrics [counters]
  (let [m (reduce (fn [m [k v]]
                    (str m "\n" k ": " v)) "" @counters)]
    (timbre/info m)))

(defn run-metrics [counters]
  (let [write-timeout 10000
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
