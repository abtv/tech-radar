(ns tech-radar.systems
  (:require [com.stuartsierra.component :as component]
            [tech-radar.components.database :refer [new-database]]
            [tech-radar.components.loader :refer [new-loader]]
            [tech-radar.components.processor :refer [new-processor]]
            [tech-radar.components.metrics :refer [new-metrics]]
            [tech-radar.components.analysis :refer [new-analysis]]
            [tech-radar.components.web-server :refer [new-web-server]]))

;TODO implement metrics component (count of messages in every channel, maybe CPU load, free memory)
(defn new-system []
  (-> (component/system-map
        :database (new-database)
        :metrics (new-metrics)
        :loader (new-loader)
        :processor (new-processor)
        :analysis (new-analysis)
        :web-server (new-web-server))
      (component/system-using
        {:database   []
         :metrics    []
         :loader     [:database :metrics]
         :processor  [:database :metrics :loader]
         :analysis   [:database :metrics :processor]
         :web-server [:database :metrics :analysis]})))
