(ns tech-radar.ui.message-view
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :refer-macros [html]]))

(defui MessageView
  Object
  (render [this]
    (let [{:keys [text]} (om/props this)]
      (html
        [:div {}
         [:p {:class "lead"} text]]))))

(def message-view (om/factory MessageView))
