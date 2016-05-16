(ns tech-radar.ui.topic-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.utils.text-formatter :refer [format]]
            [tech-radar.ui.loading-view :refer [loading-view]]))

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
    (let [{:keys [id twitter-id created-at text]} (om/props this)]

      (html
        [:tr {}
         [:td {} [:a {:href   (str "https://twitter.com/statuses/" twitter-id)
                      :target "_blank"}
                  (time->str created-at)]]
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
            [:th {} "Time"]
            [:th {} "Text"]]]
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

(defn- pagintation [{:keys [texts records-per-page page-number set-page-number]}]
  (when (seq texts)
    (let [texts-count (count texts)
          pages-count (/ texts-count records-per-page)
          pages-count (if (= 0 (mod texts-count records-per-page))
                        pages-count
                        (inc pages-count))]
      [:div.text-center {}
       [:div {}
        [:ul.pagination.no-borders {}
         (->> (range 1 pages-count)
              (mapv (page-fn set-page-number page-number)))]]])))

(defui TopicView
  static om/IQuery
  (query [this]
    [:topics
     :current-topic
     {:settings [:topic-items
                 :records-per-page
                 :page-number]}])
  Object
  (render [this]
    (let [{topics                          :topics
           current-topic                   :current-topic
           {topic-items      :topic-items
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
           (if (seq texts)
             (table-view (om/computed {} {:texts            texts
                                          :records-per-page records-per-page
                                          :page-number      page-number}))
             (loading-view {:text "Loading texts, please wait."}))]]]))))

(def topic-view (om/factory TopicView))

