(ns tech-radar.routes
  (:require [secretary.core :refer-macros [defroute]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.services.trends :as trends]
            [tech-radar.services.topics :as topics]
            [tech-radar.services.search :as search]))

(declare home-view)
(declare trends-view)
(declare topic-view)
(declare search)

(defn init-routes []
  (defroute home-view "/" []
    (trends/run-trends app-state)
    (swap! app-state (fn [state]
                       (-> state
                           (assoc-in [:current-screen] :home)
                           (assoc :current-topic nil)
                           (assoc-in [:settings :page-number] 1)))))
  (defroute trends-view "/trends" []
    (trends/run-trends app-state)
    (swap! app-state (fn [state]
                       (-> state
                           (assoc-in [:current-screen] :trends)
                           (assoc :current-topic nil)
                           (assoc-in [:settings :page-number] 1)))))
  (defroute topic-view "/topic/:topic" [topic]
    (let [topic* (keyword topic)]
      (topics/show-topic app-state (keyword topic*))
      (swap! app-state (fn [state]
                         (-> state
                             (assoc-in [:current-screen] :topic)
                             (assoc :current-topic topic*)
                             (assoc-in [:settings :page-number] 1))))))
  (defroute search #"/topic/(\w+)/search" [topic {{text :text} :query-params}]
    (search/make-search app-state (keyword topic) text)))
