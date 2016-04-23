(ns tech-radar.ui.root-component
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [om.next :as om]
            [tech-radar.ui.navbar :refer [nav-bar]]
            [tech-radar.ui.topic-view :refer [topic-view]]
            [tech-radar.ui.trends-view :refer [trends-view]]))

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
