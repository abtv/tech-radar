(ns tech-radar.ui.root-component
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [om.next :as om]
            [tech-radar.ui.navbar :refer [nav-bar
                                          NavBar]]
            [tech-radar.ui.topic-view :refer [topic-view
                                              TopicView]]
            [tech-radar.ui.trends-view :refer [trends-view
                                               TrendsView]]
            [tech-radar.ui.home :refer [home]]))

(defui RootComponent
  static om/IQuery
  (query [this]
    `[{:settings ~(om/get-query NavBar)}                    ; Construct cursor for NavBar
      :current-screen                                       ; Required for dispatching main view
      :current-topic                                        ; For mixing
      :state                                                ; Because TopicView & TrendsView required full state
      ])
  Object
  (set-page-number [this cnt]
    (om/transact! this `[(page-number/set {:page-number ~cnt}) [:settings]]))

  (render [this]
    (let [{:keys [settings current-screen current-topic state]} (om/props this)]
      (html
        [:div#wrapper {}
         (nav-bar (merge settings
                         {:current-topic current-topic}))
         [:div#page-wrapper {}
          (condp = current-screen
            :home (home state)
            :trends (trends-view state)
            (topic-view (om/computed state
                                     {:set-page-number
                                      (fn [page-number]
                                        #(.set-page-number this page-number))})))]]))))
