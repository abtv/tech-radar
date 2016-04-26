(ns tech-radar.core
  (:require [om.next :as om]
            [tech-radar.state :refer [app-state]]
            [tech-radar.history :refer [init-history]]
            [tech-radar.routes :refer [init-routes]]
            [tech-radar.ui.root-component :refer [RootComponent]]
            [tech-radar.parser :refer [parser]]))

(init-routes)
(init-history)

(def reconciler (om/reconciler {:state  app-state
                                :parser parser}))

(om/add-root! reconciler
              RootComponent
              (.getElementById js/document "app"))
