(ns tech-radar.systems
  (:require [com.stuartsierra.component :as component]
            [tech-radar.components.database :refer [new-database]]
            [tech-radar.components.loader :refer [new-loader]]
            [tech-radar.components.preprocessor :refer [new-preprocessor]]
            [tech-radar.components.metrics :refer [new-metrics]]
            [tech-radar.components.analysis :refer [new-analysis]]
            [tech-radar.components.web-server :refer [new-web-server]]))

(defn new-system []
  (-> (component/system-map
        :database (new-database)
        :metrics (new-metrics)
        :loader (new-loader)
        :preprocessor (new-preprocessor)
        :analysis (new-analysis)
        :web-server (new-web-server))
      (component/system-using
        {:database     []
         :metrics      []
         :loader       [:database :metrics]
         :preprocessor [:database :metrics :loader]
         :analysis     [:database :metrics :preprocessor]
         :web-server   [:database :metrics :analysis]})))
