(ns tech-radar.ui.table-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.utils.text-formatter :refer [format]]
            [goog.string :as gstring]))

(defn- format-time-number [n]
  (if (< n 10)
    (str "0" n)
    (str n)))

(defn- today? [t now]
  (and (= (.getFullYear t) (.getFullYear now))
       (= (.getMonth t) (.getMonth now))
       (= (.getDate t) (.getDate now))))

(defn- time->str-time [t]
  (let [hours   (-> (.getHours t)
                    (format-time-number))
        minutes (-> (.getMinutes t)
                    (format-time-number))]
    (str hours ":" minutes)))

(defn time->str-date [t]
  (let [month (-> (.getMonth t)
                  (inc)
                  (format-time-number))
        day   (-> (.getDate t)
                  (format-time-number))]
    (str day "." month)))

(defn- time->str [t now]
  (let [time (time->str-time t)
        date (time->str-date t)]
    (if (today? t now)
      time
      (str date (gstring/unescapeEntities "&nbsp;") time))))

(defui TextItem
  Object
  (render [this]
    (let [{:keys [id text]} (om/props this)]
      (html
        [:div.text-item {}
         (mapv identity (format text id))]))))

(def text-item (om/factory TextItem))

(defn time-view [href created-at]
  (let [now (js/Date.)]
    [:div.text-center {:style {:word-wrap "break-word"}}
     [:a.desktop-time {:href   href
                       :target "_blank"}
      (time->str created-at now)]
     (when-not (today? created-at now)
       [:a.mobile-time {:href   href
                        :target "_blank"}
        (time->str-date created-at)])
     [:a.mobile-time {:href   href
                      :target "_blank"}
      (time->str-time created-at)]]))

(defui TopicItem
  Object
  (render [this]
    (let [{:keys [id twitter-id created-at text]} (om/props this)
          href (str "https://twitter.com/statuses/" twitter-id)]
      (html
        [:tr {}
         [:td {}
          [:div.text-center {}
           (time-view href created-at)]]
         [:td {} (text-item {:id   id
                             :text text})]]))))

(def topic-item (om/factory TopicItem {:keyfn :id}))

(defui TableView
  Object
  (render [this]
    (let [{:keys [texts]} (om/get-computed this)]
      (html
        [:div.table-responsive
         [:table.table.table-bordered.table-hover.table-striped
          [:thead
           [:tr
            [:th.text-center "Time"]
            [:th.text-center "Text"]]]
          [:tbody
           (mapv topic-item texts)]]]))))

(def table-view (om/factory TableView))
