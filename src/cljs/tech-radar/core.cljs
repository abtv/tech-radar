(ns tech-radar.core
  (:require [om.next :as om]
            [tech-radar.state :refer [app-state]]
            [tech-radar.history :refer [init-history]]
            [tech-radar.routes :refer [init-routes]]
            [tech-radar.ui.root-component :refer [RootComponent]]
            [tech-radar.parser :refer [parser]]
            [tech-radar.services.statistic :refer [statistic-request]]))

(enable-console-print!)
(init-routes)
(init-history)
(statistic-request app-state)

(def reconciler
  (om/reconciler {:state app-state
                  :normalize false
                  :parser parser}))

(om/add-root! reconciler
              RootComponent
              (.getElementById js/document "app"))
