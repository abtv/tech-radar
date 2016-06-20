(ns tech-radar.ui.navbar
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]
            [tech-radar.utils.view :refer [prevent-propagation]]
            [tech-radar.state :refer [app-state]]
            [tech-radar.services.search :refer [make-search]]))

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

(defn records-per-page-settings [records-per-page set-record-count]
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
              [:a {:on-click #(set-record-count records-count)}
               records-count]]) [15 20 25 30])]]])

(defn menu-item [{:keys [href name selected]}]
  [:li {:key   (str "menu-item-" name)
        :class (when selected
                 "active")}
   [:a {:href href
        :alt  name}
    [:span {} name]]])

(defn sidebar-menu-items [topic-items current-topic]
  [:div.collapse.navbar-collapse.navbar-ex1-collapse {}
   [:ul.nav.navbar-nav.side-nav {}
    (mapv (fn [[id params]]
            (menu-item (assoc params :selected (= id current-topic)))) topic-items)]])

(defn search-input [navbar-component topic search-text]
  [:form.navbar-form.navbar-right {}
   [:div.input-group {}
    [:input.form-control {:type        "text"
                          :placeholder "Search..."
                          :value       (or search-text "")
                          :on-change   (fn [e]
                                         (let [value (-> e .-target .-value)]
                                           (om/transact! navbar-component `[(search-text/set {:search-text ~value})
                                                                            {:settings [:search-text]}])))}]
    [:span.input-group-btn {}
     [:button.btn.btn-default {:on-click (fn [e]
                                           (make-search app-state topic search-text)
                                           (prevent-propagation e))}
      [:i.fa.fa-search {} ""]]]]])

(defui NavBar
  static om/IQuery
  (query [this]
    [:records-per-page
     :menu-items
     :page-number
     :search-text])
  Object
  (set-record-count [this cnt]
    (om/transact! this `[(records-per-page/set {:records-per-page ~cnt})
                         {:settings [:records-per-page]}]))
  (render [this]
    (let [{:keys [records-per-page menu-items search-text]} (om/props this)
          {:keys [current-screen current-topic]} (om/get-computed this)]
      (html [:nav.navbar.navbar-inverse.navbar-fixed-top {:role "navigation"}
             (brand-toggle)
             (when (= :topic current-screen)
               (search-input this current-topic search-text))
             (when (= :topic current-screen)
               (records-per-page-settings records-per-page #(.set-record-count this %)))
             (sidebar-menu-items menu-items current-topic)]))))

(def nav-bar (om/factory NavBar))
