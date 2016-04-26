(ns tech-radar.ui.root-component
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [om.next :as om]
            [tech-radar.ui.navbar :refer [nav-bar
                                          NavBar]]
            [tech-radar.ui.topic-view :refer [topic-view
                                              TopicView]]
            [tech-radar.ui.trends-view :refer [trends-view]]))

(defmulti screen (fn [props]
                   (:current-screen props)))

(defmethod screen :trends [props]
  (trends-view props))

(defmethod screen :topic [props]
  (topic-view props))

(defui RootComponent
  static om/IQuery
  (query [this]
    `[{:settings ~(reduce conj (om/get-query NavBar) (om/get-query TopicView))}
      :trends
      :topics
      :current-screen
      :current-topic])
  Object
  (render [this]
    (let [{:keys [settings] :as props} (om/props this)]
      (html [:div#wrapper {}
             (nav-bar settings)
             [:div#page-wrapper {}
              (screen props)]]))))
