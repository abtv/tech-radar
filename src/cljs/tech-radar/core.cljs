(ns tech-radar.core
  (:require [om.next :as om]
            [tech-radar.state :refer [app-state]]
            [tech-radar.history :refer [init-history]]
            [tech-radar.routes :refer [init-routes]]
            [tech-radar.ui.root-component :refer [RootComponent]]))

(init-routes)
(init-history)

(om/add-root! (om/reconciler {:state app-state})
              RootComponent
              (.getElementById js/document "app"))
