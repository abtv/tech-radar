(ns tech-radar.services.trends
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan close! <! put!]]
            [tech-radar.services.web :refer [web]]))

(defn- convert [hashtags]
  (->> hashtags
       (map (fn [[hashtag-type data]]
              (let [converted-data (mapv (fn [[hashtag count]]
                                           {:hashtag hashtag
                                            :count   count}) data)]
                [hashtag-type converted-data])))
       (into {})))

(defn- set-trends [state trends]
  (let [trends* (->> trends
                     (map (fn [[topic hastags]]
                            [topic (convert hastags)]))
                     (into {}))]
    (swap! state assoc-in [:trends] trends*)))

(defn run-trends [state]
  (go
    (let [trends (<! (web :trends/get {}))]
      (js/console.log "trends")
      (set-trends state trends))))
