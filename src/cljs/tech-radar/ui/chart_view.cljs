(ns tech-radar.ui.chart-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.services.search :refer [make-search]]))

(defn- on-hashtag-click [topic]
  (let [all-rects    (.selectAll js/d3 (str "#" (name topic) " rect"))
        all-texts    (.selectAll js/d3 (str "#" (name topic) " text"))
        on-click     (fn [e]
                       (let [text (or (.-y e) e)]
                         (make-search app-state topic (str "#" text))))
        set-style-fn (fn [cursor color]
                       (fn [_]
                         (this-as this
                           (let [obj (.select js/d3 this)]
                             (.style obj "cursor" cursor)
                             (.style obj "fill" color)))))]
    (.on all-rects "click" on-click)
    (.on all-texts "click" on-click)

    (.on all-rects "mouseover" (set-style-fn "pointer" "red"))
    (.on all-rects "mouseout" (set-style-fn "default" "80B1D3"))

    (.on all-texts "mouseover" (set-style-fn "pointer" "red"))
    (.on all-texts "mouseout" (set-style-fn "default" "black"))))

(defn- set-hashtag-click []
  (on-hashtag-click :jobs)
  (on-hashtag-click :clojure)
  (on-hashtag-click :jvm)
  (on-hashtag-click :javascript)
  (on-hashtag-click :golang)
  (on-hashtag-click :linux)
  (on-hashtag-click :nosql))

(defn max-count [hashtag-count]
  (apply max (mapv #(:count %) hashtag-count)))

(defn validate-count [count]
  "Calculate count ticks"
  (let [max-tick 20]
    (min max-tick count)))

(defn- draw-chart [data {:keys [id width height chart]}]
  (let [{:keys [bounds margins plot x-axis y-axis name]} chart
        Chart          (.-chart js/dimple)
        svg            (.newSvg js/dimple (str "#" id) width height)
        dimple-chart   (.setMargins (Chart. svg (clj->js data)) (:left margins) (:top margins) (:right margins) (:bottom margins))
        count-axis     (.addMeasureAxis dimple-chart "x" x-axis)
        hash-tags-axis (.addCategoryAxis dimple-chart "y" y-axis)]
    (.addSeries dimple-chart "" plot (clj->js [count-axis hash-tags-axis]))

    (when (not= (count data) 0)
      (let [count-tick (validate-count (max-count data))]
        (set! (.-ticks count-axis) count-tick)))

    (.addOrderRule hash-tags-axis x-axis false)
    (.draw dimple-chart)
    (.text (.-titleShape count-axis) name)
    (.text (.-titleShape hash-tags-axis) "")

    (set-hashtag-click)))

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
        height       (.-clientHeight (.-documentElement js/document))]
    {:width (- x width-offset) :height (* height 0.87)}))

(defui Chart
  Object
  (componentDidMount [this]
    (let [{:keys [data parent-id] :as props} (om/props this)
          params (new-chart-params props)
          size   (get-chart-size parent-id)
          state  (om/get-state this)]
      (om/set-state! this (assoc state :size size))
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
  (componentWillUnmount [this]
    (let [{:keys [resize] :as state} (om/get-state this)]
      (.removeEventListener js/window "resize" resize)
      (om/set-state! this (dissoc state :resize))))
  (componentWillMount [this]
    (let [{:keys [parent-id]} (om/props this)]
      (let [resize (fn []
                     (when-let [size (get-chart-size parent-id)]
                       (let [state (om/get-state this)]
                         (om/set-state! this (assoc state :size size)))))
            state  (om/get-state this)]
        (om/set-state! this (assoc state :resize resize))
        (.addEventListener js/window "resize" resize))))
  (render [this]
    (let [{:keys [id]} (om/props this)
          {{width  :width
            height :height} :size} (om/get-state this)]
      (html
        [:div {:id     id
               :width  width
               :height height}]))))

(def chart (om/factory Chart))

(defui ChartView
  static om/IQuery
  (query [this]
    [{:settings [:menu-items :topic-items]} :trends :current-topic])
  Object
  (render [this]
    (let [{{menu-items :menu-items} :settings
           trends                   :trends} (om/props this)
          {:keys [trend-type current-trend]} (om/get-computed this)]
      (html
        [:div
         (let [parent-id "chart-container"
               {:keys [name]} (current-trend menu-items)]
           [:div.row {}
            [:div.col-lg-12 {:id parent-id}
             (when-let [data (-> trends
                                 (current-trend)
                                 (trend-type))]
               (chart {:id        (cljs.core/name current-trend)
                       :name      name
                       :data      data
                       :parent-id parent-id}))]])]))))

(def chart-view (om/factory ChartView))
