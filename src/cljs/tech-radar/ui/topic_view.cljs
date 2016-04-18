(ns tech-radar.ui.topic-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.utils.text-formatter :refer [format]]))

(defn- format-time-number [n]
  (if (< n 10)
    (str "0" n)
    (str n)))

(defn- time->str [t]
  (let [hours   (-> (.getHours t)
                    (format-time-number))
        minutes (-> (.getMinutes t)
                    (format-time-number))]
    (str hours ":" minutes)))

(defui TextItem
  Object
  (render [this]
    (let [{:keys [id text]} (om/props this)]
      (html
        [:div {}
         (mapv identity (format text id))]))))

(def text-item (om/factory TextItem))

(defui TopicItem
  Object
  (render [this]
    (let [{:keys [id created-at text]} (om/props this)]
      (html
        [:tr {}
         [:td {} (time->str created-at)]
         [:td {} (text-item {:id   id
                             :text text})]
         [:td {} ""]]))))

(def topic-item (om/factory TopicItem {:keyfn :id}))

(defui TopicView
  Object
  (render [this]
    (let [{:keys [name texts]} (om/props this)]
      (html
        [:div.container-fluid {}
         [:div {:class "row"}
          [:div {:class "col-lg-12"}
           [:h1 {:class "page-header"} name]]]
         [:div {:class "row"}
          [:div {:class "col-lg-12"}
           [:div {:class "table-responsive"}
            [:table {:class "table table-bordered table-hover table-striped"}
             [:thead {}
              [:tr {}
               [:th {} "Time"]
               [:th {} "Text"]
               [:th {} "Status"]]]
             [:tbody
              (->> texts
                   (take 25)
                   (mapv topic-item))]]]]]]))))

(def topic-view (om/factory TopicView))

