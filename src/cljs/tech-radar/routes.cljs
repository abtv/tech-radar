(ns tech-radar.routes
  (:require [secretary.core :refer-macros [defroute]]
            [cljs.core.async :refer [put! chan alts!]]
            [tech-radar.history :refer [navigate-to-url!]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.services.trends :refer [run-trends]]
            [tech-radar.services.topics :refer [show-topic]]))

(declare charts-view)
(declare table-view)

(defn init-routes []
  (defroute charts-view "/" []
    (run-trends app-state)
    (swap! app-state (fn [state]
                         (-> state
                             (assoc-in [:current-screen] :trends)
                             (assoc :current-topic nil)))))
  (defroute table-view "/topic/:topic" [topic]
    (let [topic* (keyword topic)]
      (show-topic app-state (keyword topic*))
      (swap! app-state (fn [state]
                         (-> state
                             (assoc-in [:current-screen] :topic)
                             (assoc :current-topic topic*)))))))
