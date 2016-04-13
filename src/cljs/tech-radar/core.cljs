(ns tech-radar.core
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.ui.navbar :refer [nav-bar]]
            [tech-radar.ui.topic-view :refer [topic-view]]
            [tech-radar.ui.trends-view :refer [trends-view]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.history :refer [init-history]]
            [tech-radar.routes :refer [init-routes]]))

(defui RootComponent
  Object
  (render [this]
    (let [{:keys [current-screen menu-items topics trends]} (om/props this)]
      (html [:div#wrapper {}
             (nav-bar {:menu-items menu-items})
             [:div#page-wrapper {}
              (cond
                (= current-screen :trends) (trends-view {:charts (->> menu-items
                                                                      (map (fn [item]
                                                                            (select-keys item [:id :name]))))
                                                        :trends  trends})
                :else (topic-view {:language current-screen
                                   :texts    (current-screen topics)
                                   :name     (->> menu-items
                                                  (filter (fn [{id :id}]
                                                            (= id current-screen)))
                                                  (first)
                                                  (:name))}))]]))))

(init-routes)
(init-history)

(om/add-root! (om/reconciler {:state app-state})
              RootComponent
              (.getElementById js/document "app"))
