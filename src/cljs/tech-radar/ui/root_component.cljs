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

#_(defmulti screen (fn [props]
                   :trends
                   #_(:current-screen props)))

#_(defmethod screen :trends
  (fn [obj]
    []
    #_(html
        [:div
         (trends-view {:charts topic-items
                       :trends trends})])))
;
;(defmethod screen :topic (fn [{:keys [current-screen topics topic-items]}]
;                           (topic-view {:language current-screen
;                                        :texts    (current-screen topics)
;                                        :name     (topic-name topic-items current-screen)})))
;
;(defmethod screen :default (fn [props]
;
;                             ))

(defui RootComponent
  Object
  (render [this]
    (let [{:keys [current-screen topics trends topic-items] :as props} (om/props this)]
      (html [:div#wrapper {}
             (nav-bar (select-keys props [:topic-items]))
             [:div#page-wrapper {}
              #_(screen props)
              (cond
                (= current-screen :trends) (trends-view {:charts topic-items
                                                         :trends trends})
                (topics current-screen) (topic-view {:language current-screen
                                                     :texts    (current-screen topics)
                                                     :name     (topic-name topic-items current-screen)}))]]))))
