(ns tech-radar.ui.trends-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.ui.message-view :refer [message-view]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.history :refer [navigate-to-url!]]
            [tech-radar.services.search :refer [make-search]]))

(def max-chart-items 25)

(defn- limit [data]
  (->> data
       (sort-by :count (comp - compare))
       (take max-chart-items)))

(defn- on-hashtag-click [topic]
  (let [on-click (fn [e]
                   (let [text (or (.-y e) e)]
                     ;(navigate-to-url! (str "#" (name topic) "/search"))
                     ;(navigate-to-url! (str "#" (name topic) "?text=" text))
                     (make-search app-state topic (str "#" text))))]
    (.on (.selectAll js/d3 (str "#" (name topic) " rect")) "click" on-click)
    (.on (.selectAll js/d3 (str "#" (name topic) " text")) "click" on-click)))

(defn- set-hashtag-click []
  (on-hashtag-click :jobs)
  (on-hashtag-click :clojure)
  (on-hashtag-click :jvm)
  (on-hashtag-click :javascript)
  (on-hashtag-click :golang)
  (on-hashtag-click :linux)
  (on-hashtag-click :nosql))

(defn- draw-chart [data {:keys [id width height chart]}]
  (let [limit-data   (limit data)
        {:keys [bounds margins plot x-axis y-axis name]} chart
        Chart        (.-chart js/dimple)
        svg          (.newSvg js/dimple (str "#" id) width height)
        dimple-chart (.setMargins (Chart. svg) (:left margins) (:top margins) (:right margins) (:bottom margins))
        x            (.addMeasureAxis dimple-chart "x" x-axis)
        y            (.addCategoryAxis dimple-chart "y" y-axis)
        s            (.addSeries dimple-chart "" plot (clj->js [x y]))]
    (set-hashtag-click)
    ;(.assignColor dimple-chart "lang-category" "yellow")
    (aset s "data" (clj->js limit-data))
    (.draw dimple-chart)
    (.text (.-titleShape x) name)
    (.text (.-titleShape y) "")))

(defn- new-chart-params [{:keys [id name width height]}]
  {:id     id
   :width  width
   :height height
   :chart  {:margins {:left   "100px"
                      :top    "0px"
                      :right  "0px"
                      :bottom "50px"}
            :plot    js/dimple.plot.bar
            :x-axis  "count"
            :y-axis  "hashtag"
            :name    name}})

(defn get-chart-size [id]
  (let [e            (.getElementById js/document id)
        x            (.-clientWidth e)
        width-offset 30
        height       400]
    {:width (- x width-offset) :height height}))

(defui Chart
  Object
  (componentDidMount [this]
    (let [{:keys [data parent-id] :as props} (om/props this)
          params (new-chart-params props)
          size   (get-chart-size parent-id)]
      (om/set-state! this size)
      (draw-chart data (merge params size))))
  (componentDidUpdate [this _ _]
    (let [{:keys [id parent-id data] :as props} (om/props this)
          params (new-chart-params props)
          size   (get-chart-size parent-id)]
      (let [n (.getElementById js/document id)]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (when data
        (draw-chart data (merge params size)))))
  (componentWillMount [this]
    (let [{:keys [parent-id]} (om/props this)]
      (.addEventListener js/window
                         "resize" (fn []
                                    (om/set-state! this (get-chart-size parent-id))))))
  (render [this]
    (let [{:keys [id]} (om/props this)
          {:keys [width height]} (om/get-state this)]
      (html
        [:div {:id     id
               :width  width
               :height height}]))))

(def chart (om/factory Chart))

(defui ChartsView
  static om/IQuery
  (query [this]
    [{:settings [:menu-items :topic-items]} :trends :current-topic])
  Object
  (render [this]
    (let [{{menu-items  :menu-items
            topic-items :topic-items} :settings
           trends                     :trends} (om/props this)
          {:keys [trend-type]} (om/get-computed this)]
      (html
        [:div {:id "charts-view"}
         (->> menu-items
              (filter (fn [[k _]]
                        (topic-items k)))
              (partition-all 2)
              (map-indexed (fn [idx items]
                             [:div.row {:key (str "radar_" idx)}
                              (->> items
                                   (map (fn [[id {:keys [name]}]]
                                          (let [parent-id (str "chart-div-" (cljs.core/name name))]
                                            [:div.col-lg-6 {:id parent-id}
                                             (when-let [data (-> trends
                                                                 (id)
                                                                 (trend-type))]
                                               (chart {:id        (cljs.core/name id)
                                                       :name      name
                                                       :data      data
                                                       :parent-id parent-id}))]))))])))]))))

(def charts-view (om/factory ChartsView))

(defn- trend-type->name [type]
  (case type
    :daily "Daily"
    :weekly "Weekly"
    :monthly "Monthly"))

(defn trend-item [trend-type current-trend-type set-trend-type]
  (let [name (trend-type->name trend-type)]
    [:li {:key      (str "page-" name)
          :class    (if (= trend-type current-trend-type)
                      "active cursor"
                      "cursor")
          :on-click #(set-trend-type trend-type)}
     [:a
      name]]))

(defui TrendsView
  static om/IQuery
  (query [this]
    (conj (om/get-query ChartsView) :trend-type))
  Object
  (render [this]
    (html
      (let [{:keys [trends trend-type] :as props} (om/props this)
            {:keys [set-trend-type]} (om/get-computed this)]
        [:div.container-fluid
         (if (seq trends)
           [:div
            [:div.row
             [:div.col-lg-12
              [:div.text-center {}
               [:div {}
                [:ul.pagination.no-borders {}
                 (trend-item :daily trend-type set-trend-type)
                 (trend-item :weekly trend-type set-trend-type)
                 (trend-item :monthly trend-type set-trend-type)]]]]]
            (charts-view (om/computed props {:trend-type trend-type}))]
           (message-view {:text "Loading trends, please wait."}))]))))

(def trends-view (om/factory TrendsView))
