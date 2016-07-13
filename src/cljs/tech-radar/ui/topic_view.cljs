(ns tech-radar.ui.topic-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.ui.table-view :as table-view]
            [tech-radar.ui.message-view :refer [message-view]]))

(defn- page-fn [set-page-number current-page-number]
  (fn [page-number]
    [:li {:key   (str "page-" page-number)
          :class (if (= page-number current-page-number)
                   "active cursor"
                   "cursor")}
     [:a {:on-click (set-page-number page-number)}
      page-number]]))

(defn- mobile? []
  (let [max-mobile-width 768]
    (<= (.-innerWidth js/window) max-mobile-width)))

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
        [:div.text-center
         [:div
          [:ul.pagination.no-borders
           (->> (range 1 (inc pages-count))
                (mapv (page-fn set-page-number page-number)))]]]))))

(defui TopicView
  static om/IQuery
  (query [this]
    [:topics
     :current-topic
     {:settings [:records-per-page
                 :page-number]}])
  Object
  (render [this]
    (let [{topics                          :topics
           current-topic                   :current-topic
           {records-per-page :records-per-page
            page-number      :page-number} :settings} (om/props this)
          texts (current-topic topics)
          {:keys [set-page-number]} (om/get-computed this)]
      (html
        [:div.container-fluid
         [:div.row
          [:div.col-lg-12
           (pagintation {:texts            texts
                         :records-per-page records-per-page
                         :page-number      page-number
                         :set-page-number  set-page-number})
           (if texts
             (if (seq texts)
               (let [page-texts (->> texts
                                     (drop (* (dec page-number) records-per-page))
                                     (take records-per-page)
                                     (into []))]
                 (table-view/table-view (om/computed {} {:texts page-texts})))
               (message-view {:text "No records found for your request."}))
             (message-view {:text "Loading texts, please wait."}))]]]))))

(def topic-view (om/factory TopicView))

