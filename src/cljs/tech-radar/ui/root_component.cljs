(ns tech-radar.ui.root-component
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [om.next :as om]
            [tech-radar.ui.navbar :refer [nav-bar
                                          NavBar]]
            [tech-radar.ui.topic-view :refer [topic-view
                                              TopicView]]
            [tech-radar.ui.trends-view :refer [trends-view
                                               TrendsView]]))

(defmulti screen (fn [this props]
                   (:current-screen props)))

(defmethod screen :trends [this props]
  (trends-view props))

(defmethod screen :topic [this props]
  (topic-view (om/computed props {:set-page-number (fn [page-number]
                                                     #(om/transact! this `[(page-number/set {:page-number ~page-number})
                                                                          [:settings]]))})))

(defui RootComponent
  static om/IQuery
  (query [this]
    `[{:navbar-settings ~(om/get-query NavBar)}
      {:settings [:topic-items]}
      :trends
      :topics
      :current-screen
      :current-topic])
  Object
  (render [this]
    (let [{:keys [navbar-settings] :as props} (om/props this)]
      (html [:div#wrapper {}
             (nav-bar navbar-settings)
             [:div#page-wrapper {}
              (screen this props)]]))))
