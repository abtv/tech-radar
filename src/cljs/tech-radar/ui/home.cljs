(ns tech-radar.ui.home
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.ui.message-view :refer [message-view]]))

(defui Pie-diagram
  Object
  (componentDidMount [this]
    (let [{:keys [id data width height series measure-axis]} (om/props this)
          Chart        (.-chart js/dimple)
          svg          (.newSvg js/dimple (str "#" id) width height)
          dimple-chart (Chart. svg (clj->js data))]
      (.addMeasureAxis dimple-chart "p" measure-axis)
      (.addSeries dimple-chart series (-> js/dimple .-plot .-pie))
      (.addLegend dimple-chart 0 0 width 100)
      (.draw dimple-chart)
      ;(let [on-hover (fn [] (
      ;                              ))]
      ;  (.addEventListener (.getElementById js/document (str "#" id)) "onHover" on-hover))
      ))

  (render [this]
    (let [{:keys [id width height]} (om/props this)]
      (html
        [:div {:id     id
               :width  width
               :height height
               :style  {:float "right"}}]))))


(def pie-diagram (om/factory Pie-diagram))


(def data [{:hashtag "Clojure" :count 10}
           {:hashtag "JVM" :count 20}
           {:hashtag "JavaScript" :count 30}
           {:hashtag "Golang" :count 40}
           {:hashtag "Linux" :count 50}
           {:hashtag "NoSQL" :count 10}])

(defui Home
  Object
  (render [this]
    (html
      (let [props (om/props this)]
        [:div.container-fluid
         [:div.row {}
          [:div.col-lg-12 {}
           [:h3 {} "Tech Radar helps you to be aware of modern trends in programming"]
           [:hr {}]
           (pie-diagram {
                         :id           "pie"
                         :width        300
                         :height       300
                         :data         data
                         :measure-axis "count"
                         :series       "hashtag"
                         })
           [:h4 {} [:i.fa.fa-rocket {}] " Features"]
           [:p {} "This is a resource created specially for programmers."]
           [:span {} "With Tech Radar you can:"]
           [:ul {}
            [:li {} "analyze job trends and see what's in high demand now"]
            [:li {} "find an interesting job"]
            [:li {} "analyze programming tendencies and decide what technology can solve your problems and improve your productivity"]
            [:li {} "discover new things which you are unaware"]
            [:li {} "read all the interesting tweets in your field of interest - it doesn't matter how many people you follow"]]
           [:p {} "Tech Radar uses Twitter as data source. It collects and analyzes tweets about programming and provides
                   a convenient way for you to broad your knowledge horizons."]
           [:hr {}]
           [:h4 {} [:i.fa.fa-info-circle {}] " How to use Tech Radar"]
           [:p {} "Use " [:a {:href "#/trends"} "Trends"] " menu item to see hashtag analytics. You can click any hashtag on a diagram
                   and you will see tweets with the hashtag. Use " [:a {:href "#/topic/jobs"} "Jobs"]
            " menu item to see latest job tweets or one of the languge menu items to see latest language trends. You can
             also find interesting tweets with \"Search\" window."]
           [:hr {}]
           [:div {}
            [:h4 {} [:i.fa.fa-code-fork {}] " How to contribute"]
            [:span {} "Tech radar is an open source project written in Clojure/ClojureScirpt. It was created to provide an
                       example of a full stack Clojure application written in an idiomatic way. This project is under active
                       development - you can contribute to the project "]
            [:a {:href   "https://github.com/abtv/tech-radar"
                 :target "_blank"} "here"]
            [:span {} ". Pull requests and any ideas are welcome."]]]]]))))

(def home (om/factory Home))
