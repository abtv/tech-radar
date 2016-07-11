(ns tech-radar.services.statistic
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan close! <! put!]]
            [tech-radar.services.web :refer [web]]))

(defn- set-statistic-results [state statistic]
  (swap! state assoc-in [:statistic] statistic))

(defn statistic-request [state]
  (go
    (let [results (<! (web :statistic/get))]
      (set-statistic-results state results))))
