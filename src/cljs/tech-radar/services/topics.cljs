(ns tech-radar.services.topics
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan close! <! put!]]
            [tech-radar.services.web :refer [web]]))

(defn- set-topic [state topic value]
  (swap! state assoc-in [:topics topic] value))

(defn show-topic [state topic]
  (go
    (let [texts (<! (web :topics/get {:topic (name topic)}))]
      (set-topic state topic texts))))

