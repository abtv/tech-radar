(ns tech-radar.core
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.ui.navbar :refer [nav-bar]]
            [tech-radar.ui.topic-view :refer [topic-view]]
            [tech-radar.ui.trends-view :refer [trends-view]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.history :refer [init-history]]
            [tech-radar.routes :refer [init-routes]]))

(defn- topic-name [topic-items current-screen]
  (->> topic-items
       (filter (fn [{id :id}]
                 (= id current-screen)))
       (first)
       (:name)))

(defui RootComponent
  Object
  (render [this]
    (let [{:keys [current-screen topic-items topics trends] :as props} (om/props this)]
      (html [:div#wrapper {}
             (nav-bar (select-keys props [:topic-items]))
             [:div#page-wrapper {}
              (cond
                (= current-screen :trends) (trends-view {:charts topic-items
                                                         :trends trends})
                (topics current-screen) (topic-view {:language current-screen
                                                     :texts    (current-screen topics)
                                                     :name     (topic-name topic-items current-screen)}))]]))))

(init-routes)
(init-history)

(om/add-root! (om/reconciler {:state app-state})
              RootComponent
              (.getElementById js/document "app"))
