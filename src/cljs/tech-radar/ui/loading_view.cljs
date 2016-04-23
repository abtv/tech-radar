(ns tech-radar.ui.loading-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]))

(defui LoadingView
  Object
  (render [this]
    (let [{:keys [text]} (om/props this)]
      (html
        [:div {}
         [:p {:class "lead"} text]]))))

(def loading-view (om/factory LoadingView))
