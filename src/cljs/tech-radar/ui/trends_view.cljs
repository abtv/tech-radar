(ns tech-radar.ui.trends-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.ui.loading-view :refer [loading-view]]))

(def max-chart-items 25)

(defn- limit [data]
  (->> data
       (sort-by :count (comp - compare))
       (take max-chart-items)))

(defn- draw-chart [data {:keys [id width height chart]}]
  (let [limit-data   (limit data)
        {:keys [bounds margins plot x-axis y-axis name]} chart
        Chart        (.-chart js/dimple)
        svg          (.newSvg js/dimple (str "#" id) width height)
        dimple-chart (.setMargins (Chart. svg) (:left margins) (:top margins) (:right margins) (:bottom margins))
        x            (.addMeasureAxis dimple-chart "x" x-axis)
        y            (.addCategoryAxis dimple-chart "y" y-axis)
        s            (.addSeries dimple-chart "" plot (clj->js [x y]))]
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

(defn get-div-dimensions [id]
  (let [e (.getElementById js/document id)
        x (.-clientWidth e)
        y (.-clientHeight e)]
    {:width x :height y}))

(defui Chart
  Object
  (componentDidMount [this]
    (let [{:keys [data] :as props} (om/props this)
          params (new-chart-params props)]
      (draw-chart data params)))
  (componentDidUpdate [this _ _]
    (let [{:keys [id data] :as props} (om/props this)
          params (new-chart-params props)]
      (let [n (.getElementById js/document id)]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (when data
        (draw-chart data params))))
  #_(componentWillMount [this]
                        (.addEventListener js/window
                                           "resize" (fn []
                                                      (let [{:keys [width height]} (get-div-dimensions "charts-view")]
                                                        (js/console.log width)
                                                        #_(om/update cursor :div {:width width :height height})))))
  (render [this]
    (let [props (om/props this)]
      (html
        [:div (select-keys props [:id :width :height])]))))

(def chart (om/factory Chart))

(defui ChartsView
  Object
  (render [this]
    (let [{:keys [charts trends]} (om/props this)
          width  400
          height 400]
      (html
        [:div {:id "charts-view"}
         (->> charts
              (partition-all 2)
              (map-indexed (fn [idx items]
                             [:div.row {:key (str "radar_" idx)}
                              (->> items
                                   (map (fn [[id {:keys [name]}]]
                                          [:div.col-lg-6
                                           (when-let [data (id trends)]
                                             (chart {:id     (cljs.core/name id)
                                                     :name   name
                                                     :data   data
                                                     :width  width
                                                     :height height}))])))])))]))))

(def charts-view (om/factory ChartsView))

(defui TrendsView
  static om/IQuery
  (query [this]
    [:topic-items :trends :current-topic])
  Object
  (render [this]
    (html
      (let [{:keys [topic-items trends]} (om/props this)]
        [:div.container-fluid
         [:div.row
          [:div.col-lg-12
           [:h3 {:class "page-header"} "Trends"]]]
         (if (seq trends)
           (charts-view {:charts topic-items
                         :trends trends})
           (loading-view {:text "Loading trends, please wait."}))]))))

(def trends-view (om/factory TrendsView))
