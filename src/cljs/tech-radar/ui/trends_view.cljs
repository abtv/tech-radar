(ns tech-radar.ui.trends-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.ui.message-view :refer [message-view]]
            [tech-radar.ui.table-view :as table-view]
            [tech-radar.utils.convert :as convert]
            [tech-radar.ui.chart-view :as chart-view]))

(defn trend-type-list-item [trend-type current-trend-type set-trend-type]
  (let [name (convert/trend-type->trend-type-name trend-type)]
    [:li {:key      (str "page-" name)
          :class    (if (= trend-type current-trend-type)
                      "active cursor"
                      "cursor")
          :on-click #(set-trend-type trend-type)}
     [:a name]]))

(defn trend-type-select-item [trend-type current-trend-type]
  (let [name (convert/trend-type->trend-type-name trend-type)]
    [:option {:key   (str "page-" name)
              :value name}
     name]))

(defn trend-list-item [trend current-trend set-current-trend]
  (let [name (convert/trend-item->trend-name trend)]
    [:li {:key      (str "trend-list-item-" name)
          :class    (if (= trend current-trend)
                      "active cursor"
                      "cursor")
          :on-click #(set-current-trend trend)}
     [:a name]]))

(defn trend-select-item [trend current-trend]
  (let [name (convert/trend-item->trend-name trend)]
    [:option {:key   (str "trend-select-item-" name)
              :value name}
     name]))

(defn trends-controls-desktop [{:keys [current-trend set-current-trend
                                       trend-type set-trend-type]}]
  [:div.row
   [:div.col-lg-6
    [:div.text-center {}
     [:ul.pagination.no-borders {}
      (->> [:jobs :clojure :jvm :javascript :golang :linux :nosql]
           (mapv #(trend-list-item % current-trend set-current-trend)))]]]
   [:div.col-lg-6
    [:div.text-center {}
     [:ul.pagination.no-borders {}
      (->> [:daily :weekly :monthly]
           (mapv #(trend-type-list-item % trend-type set-trend-type)))]]]])

(defn trends-controls-mobile [{:keys [current-trend set-current-trend
                                      trend-type set-trend-type]}]
  [:div.row
   [:div.col-xs-6
    [:select.combobox.input-large.form-control
     {:on-change (fn [e]
                   (set-current-trend (-> e
                                          .-target
                                          .-value
                                          convert/trend-name->trend-item)))}
     (->> [:jobs :clojure :jvm :javascript :golang :linux :nosql]
          (mapv #(trend-select-item % current-trend)))]]

   [:div.col-xs-6
    [:select.combobox.input-large.form-control
     {:on-change (fn [e]
                   (set-trend-type (-> e
                                       .-target
                                       .-value
                                       convert/trend-type-name->trend-type)))}
     (->> [:daily :weekly :monthly]
          (mapv #(trend-type-select-item % trend-type)))]]])

(defui TrendsView
  static om/IQuery
  (query [this]
    (conj (om/get-query chart-view/ChartView) :trend-type :current-trend))
  Object
  (render [this]
    (html
      (let [{:keys [trends trend-type current-trend] :as props} (om/props this)
            {:keys [set-trend-type set-current-trend]} (om/get-computed this)]
        [:div.container-fluid
         (if (seq trends)
           [:div
            [:div.trends-controls-desktop
             (trends-controls-desktop {:current-trend     current-trend
                                       :set-current-trend set-current-trend
                                       :trend-type        trend-type
                                       :set-trend-type    set-trend-type})]
            [:div.trends-controls-mobile
             (trends-controls-mobile {:current-trend     current-trend
                                      :set-current-trend set-current-trend
                                      :trend-type        trend-type
                                      :set-trend-type    set-trend-type})]
            [:div.row
             [:div.col-lg-6
              (let [popular-tweets (-> trends
                                       (current-trend)
                                       (:popular))]
                (if (seq popular-tweets)
                  (table-view/table-view (om/computed {} {:texts popular-tweets}))
                  [:span "There is no data yet..."]))]
             [:div.col-lg-6
              (chart-view/chart-view (om/computed props {:trend-type    trend-type
                                                         :current-trend current-trend}))]]]
           (message-view {:text "Loading trends, please wait."}))]))))

(def trends-view (om/factory TrendsView))
