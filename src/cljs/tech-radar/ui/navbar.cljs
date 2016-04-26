(ns tech-radar.ui.navbar
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.state :refer [app-state]]))

(defn brand-toggle []
  (html
    [:div.navbar-header {}
     [:button.navbar-toggle {:type        "button"
                             :data-toggle "collapse"
                             :data-target ".navbar-ex1-collapse"}
      [:span.sr-only {} "Toggle navigation"]
      [:span.icon-bar {}]
      [:span.icon-bar {}]
      [:span.icon-bar {}]]
     [:a.navbar-brand {:href "#/"} "Tech Radar"]
     [:img {:src "images/radar.svg"}]]))

(defn navbar-right [component records-per-page]
  [:ul {:class "nav navbar-right top-nav"}
   [:li {:class "dropdown"}
    [:a {:href "#", :class "dropdown-toggle", :data-toggle "dropdown", :aria-expanded "false"}
     [:i {:class "fa fa-gear"} " records per page "]
     [:b {:class "caret"}]]
    [:ul {:class "dropdown-menu "}
     (mapv (fn [records-count]
             [:li {:class (if (= records-per-page records-count)
                            "active"
                            nil)
                   :key   (str "records_count_" records-count)}
              [:a {:on-click #(om/transact! component `[(records-per-page/set {:records-per-page ~records-count})
                                                        {:settings [:records-per-page]}])}
               records-count]]) [10 20 30])]]])

(defui MenuItem
  static om/IQuery
  (query [this]
    [:href :name])
  Object
  (render [this]
    (let [{:keys [href name]} (om/props this)]
      (html
        [:li {}
         [:a {:href href
              :alt  name}
          [:span {} name]]]))))

(def menu-item (om/factory MenuItem {:keyfn :name}))

(defn sidebar-menu-items [topic-items]
  [:div.collapse.navbar-collapse.navbar-ex1-collapse {}
   [:ul.nav.navbar-nav.side-nav {}
    (mapv (comp menu-item second) topic-items)]])

(defui NavBar
  static om/IQuery
  (query [this]
    [:records-per-page :topic-items :page-number])
  Object
  (render [this]
    (let [{:keys [records-per-page topic-items]} (om/props this)]
      (html [:nav.navbar.navbar-inverse.navbar-fixed-top {:role "navigation"}
             (brand-toggle)
             (navbar-right this records-per-page)
             (sidebar-menu-items topic-items)]))))

(def nav-bar (om/factory NavBar))
