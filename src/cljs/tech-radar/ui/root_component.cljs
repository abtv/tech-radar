(ns tech-radar.ui.root-component
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [om.next :as om]
            [tech-radar.ui.navbar :refer [nav-bar]]
            [tech-radar.ui.topic-view :refer [topic-view]]
            [tech-radar.ui.trends-view :refer [trends-view]]))

(defn- topic-name [topic-items current-screen]
  (->> topic-items
       (current-screen)
       (:name)))

(defmulti screen (fn [props]
                   (:current-screen props)))

(defmethod screen :trends [{:keys [topic-items trends]}]
  (trends-view {:charts topic-items
                :trends trends}))

(defmethod screen :topic [{:keys [current-topic topics topic-items records-per-page]}]
  (topic-view {:texts            (current-topic topics)
               :name             (topic-name topic-items current-topic)
               :records-per-page records-per-page}))

(defui RootComponent
  Object
  (render [this]
    (let [props (om/props this)]
      (html [:div#wrapper {}
             (nav-bar props)
             [:div#page-wrapper {}
              (screen props)]]))))
