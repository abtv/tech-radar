(ns tech-radar.ui.root-component
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [om.next :as om]
            [tech-radar.ui.navbar :refer [nav-bar]]
            [tech-radar.ui.topic-view :refer [topic-view]]
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
    [:current-screen :records-per-page :topic-items :trends :current-topic :topics])
  Object
  (render [this]
    (let [props (om/props this)]
      (html [:div#wrapper {}
             (nav-bar props)
             [:div#page-wrapper {}
              (screen props)]]))))
