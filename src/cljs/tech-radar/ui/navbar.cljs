(ns tech-radar.ui.navbar
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.state :refer [app-state]]))

(defui BrandToggle
  Object
  (render [this]
    (html
      [:div.navbar-header
       [:button.navbar-toggle {:type        "button"
                               :data-toggle "collapse"
                               :data-target ".navbar-ex1-collapse"}
        [:span.sr-only "Toggle navigation"]
        [:span.icon-bar]
        [:span.icon-bar]
        [:span.icon-bar]]
       [:a.navbar-brand {:href "#/"} "Tech Radar"]
       [:img {:src "images/radar.svg"}]])))

(defui MenuItem
  Object
  (render [this]
    (let [{:keys [id href name]} (om/props this)]
      (html
        [:li
         [:a {:href href
              :alt  name}
          [:img {:src (str "images/" (cljs.core/name id) ".svg")
                 :alt name}]]]))))

(def menu-item (om/factory MenuItem {:keyfn :name}))

(defui SidebarMenuItems
  Object
  (render [this]
    (let [{:keys [menu-items]} (om/props this)]
      (html
        [:div.collapse.navbar-collapse.navbar-ex1-collapse
         [:ul.nav.navbar-nav.side-nav
          (map menu-item menu-items)]]))))

(def brand-toggle (om/factory BrandToggle))
(def sidebar-menu-items (om/factory SidebarMenuItems))

(defui NavBar
  Object
  (render [this]
    (let [props (om/props this)]
      (html [:nav.navbar.navbar-inverse.navbar-fixed-top {:role "navigation"}
             (brand-toggle)
             (sidebar-menu-items props)]))))

(def nav-bar (om/factory NavBar))
