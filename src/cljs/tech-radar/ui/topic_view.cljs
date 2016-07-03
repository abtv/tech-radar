(ns tech-radar.ui.topic-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.utils.text-formatter :refer [format]]
            [tech-radar.ui.message-view :refer [message-view]]
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
    (let [{:keys [texts records-per-page page-number]} (om/get-computed this)]
      (html
        [:div {:class "table-responsive"}
         [:table {:class "table table-bordered table-hover table-striped"}
          [:thead {}
           [:tr {}
            [:th {:class "text-center"} "Time"]
            [:th {:class "text-center"} "Text"]]]
          [:tbody
           (->> texts
                (drop (* (dec page-number) records-per-page))
                (take records-per-page)
                (mapv topic-item))]]]))))

(def table-view (om/factory TableView))

(defn- topic-name [topic-items current-topic]
  (->> topic-items
       (current-topic)
       (:name)))

(defn- page-fn [set-page-number current-page-number]
  (fn [page-number]
    [:li {:key   (str "page-" page-number)
          :class (if (= page-number current-page-number)
                   "active cursor"
                   "cursor")}
     [:a {:on-click (set-page-number page-number)}
      page-number]]))

(defn- mobile? []
  (<= (.-innerWidth js/window) 768))

(defn- pagintation [{:keys [texts records-per-page page-number set-page-number]}]
  (when (seq texts)
    (let [texts-count (count texts)
          pages-count (-> texts-count
                          (/ records-per-page)
                          (int))
          pages-count (if (= 0 (mod texts-count records-per-page))
                        pages-count
                        (inc pages-count))
          pages-count (if (mobile?)
                        (min pages-count 8)
                        pages-count)]
      (when (> pages-count 1)
        [:div.text-center {}
         [:div {}
          [:ul.pagination.no-borders {}
           (->> (range 1 (inc pages-count))
                (mapv (page-fn set-page-number page-number)))]]]))))

(defui TopicView
  static om/IQuery
  (query [this]
    [:topics
     :current-topic
     {:settings [:menu-items
                 :records-per-page
                 :page-number]}])
  Object
  (render [this]
    (let [{topics                          :topics
           current-topic                   :current-topic
           {topic-items      :menu-items
            records-per-page :records-per-page
            page-number      :page-number} :settings} (om/props this)
          name  (topic-name topic-items current-topic)
          texts (current-topic topics)
          {:keys [set-page-number]} (om/get-computed this)]
      (html
        [:div.container-fluid {}
         [:div {:class "row"}
          [:div {:class "col-lg-12"}
           (pagintation {:texts            texts
                         :records-per-page records-per-page
                         :page-number      page-number
                         :set-page-number  set-page-number})
           (if texts
             (if (seq texts)
               (table-view (om/computed {} {:texts            texts
                                            :records-per-page records-per-page
                                            :page-number      page-number}))
               (message-view {:text "No records found for your request."}))
             (message-view {:text "Loading texts, please wait."}))]]]))))

(def topic-view (om/factory TopicView))

