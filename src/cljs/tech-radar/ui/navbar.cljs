(ns tech-radar.ui.navbar
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.state :refer [app-state]]))

(defui BrandToggle
  Object
  (render [this]
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
       [:img {:src "images/radar.svg"}]])))

(defui NavbarRight
  Object
  (render [this]
    (html
      [:ul {:class "nav navbar-right top-nav"}
       #_[:li {:class "dropdown"}
        [:a {:href "#", :class "dropdown-toggle", :data-toggle "dropdown"}
         [:i {:class "fa fa-envelope"}]
         [:b {:class "caret"}]]
        [:ul {:class "dropdown-menu message-dropdown"}
         [:li {:class "message-preview"}
          [:a {:href "#"}
           [:div {:class "media"}
            [:span {:class "pull-left"}
             [:img {:class "media-object", :src "http://placehold.it/50x50"}]]
            [:div {:class "media-body"}
             [:h5 {:class "media-heading"}
              [:strong "John Smith"]]
             [:p {:class "small text-muted"}
              [:i {:class "fa fa-clock-o"}] " Yesterday at 4:32 PM"]
             [:p "Lorem ipsum dolor sit amet, consectetur..."]]]]]
         [:li {:class "message-preview"}
          [:a {:href "#"}
           [:div {:class "media"}
            [:span {:class "pull-left"}
             [:img {:class "media-object", :src "http://placehold.it/50x50"}]]
            [:div {:class "media-body"}
             [:h5 {:class "media-heading"}
              [:strong "John Smith"]]
             [:p {:class "small text-muted"}
              [:i {:class "fa fa-clock-o"}] " Yesterday at 4:32 PM"]
             [:p "Lorem ipsum dolor sit amet, consectetur..."]]]]]
         [:li {:class "message-preview"}
          [:a {:href "#"}
           [:div {:class "media"}
            [:span {:class "pull-left"}
             [:img {:class "media-object", :src "http://placehold.it/50x50"}]]
            [:div {:class "media-body"}
             [:h5 {:class "media-heading"}
              [:strong "John Smith"]]
             [:p {:class "small text-muted"}
              [:i {:class "fa fa-clock-o"}] " Yesterday at 4:32 PM"]
             [:p "Lorem ipsum dolor sit amet, consectetur..."]]]]]
         [:li {:class "message-footer"}
          [:a {:href "#"} "Read All New Messages"]]]]
       [:li {:class "dropdown"}
        [:a {:href "#", :class "dropdown-toggle", :data-toggle "dropdown", :aria-expanded "false"}
         [:i {:class "fa fa-gear"} " records per page "]
         [:b {:class "caret"}]]
        [:ul {:class "dropdown-menu alert-dropdown"}
         [:li
          [:a {:href "#"} "Alert Name "
           [:span {:class "label label-default"} "Alert Badge"]]]
         [:li
          [:a {:href "#"} "Alert Name "
           [:span {:class "label label-primary"} "Alert Badge"]]]
         [:li
          [:a {:href "#"} "Alert Name "
           [:span {:class "label label-success"} "Alert Badge"]]]
         [:li
          [:a {:href "#"} "Alert Name "
           [:span {:class "label label-info"} "Alert Badge"]]]
         [:li
          [:a {:href "#"} "Alert Name "
           [:span {:class "label label-warning"} "Alert Badge"]]]
         [:li
          [:a {:href "#"} "Alert Name "
           [:span {:class "label label-danger"} "Alert Badge"]]]
         [:li {:class "divider"}]
         [:li
          [:a {:href "#"} "View All"]]]]
       #_[:li {:class "dropdown"}
        [:a {:href "#", :class "dropdown-toggle", :data-toggle "dropdown", :aria-expanded "false"}
         [:i {:class "fa fa-user"}] " John Smith "
         [:b {:class "caret"}]]
        [:ul {:class "dropdown-menu"}
         [:li
          [:a {:href "#"}
           [:i {:class "fa fa-fw fa-user"}] " Profile"]]
         [:li
          [:a {:href "#"}
           [:i {:class "fa fa-fw fa-envelope"}] " Inbox"]]
         [:li
          [:a {:href "#"}
           [:i {:class "fa fa-fw fa-gear"}] " Settings"]]
         [:li {:class "divider"}]
         [:li
          [:a {:href "#"}
           [:i {:class "fa fa-fw fa-power-off"}] " Log Out"]]]]])))

(def navbar-right (om/factory NavbarRight))

(defui MenuItem
  Object
  (render [this]
    (let [{:keys [href name]} (om/props this)]
      (html
        [:li {}
         [:a {:href href
              :alt  name}
          [:span {} name]]]))))

(def menu-item (om/factory MenuItem {:keyfn :name}))

(defui SidebarMenuItems
  Object
  (render [this]
    (let [{:keys [menu-items]} (om/props this)]
      (html
        [:div.collapse.navbar-collapse.navbar-ex1-collapse {}
         [:ul.nav.navbar-nav.side-nav {}
          (mapv menu-item menu-items)]]))))

(def brand-toggle (om/factory BrandToggle))
(def sidebar-menu-items (om/factory SidebarMenuItems))

(defui NavBar
  Object
  (render [this]
    (let [props (om/props this)]
      (html [:nav.navbar.navbar-inverse.navbar-fixed-top {:role "navigation"}
             (brand-toggle)
             (navbar-right)
             (sidebar-menu-items props)]))))

(def nav-bar (om/factory NavBar))
