(ns tech-radar.components.analysis
  (:require [tech-radar.services.analysis :refer [load-data
                                                 run-analysis]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan close!]]
            [taoensso.timbre :as timbre]
            [tech-radar.utils.settings :refer [load-classify-settings]]))

(defrecord Analysis [database metrics processor get-trends-fn get-texts-fn]
  component/Lifecycle
  (start [component]
    (if get-trends-fn
      component
      (do
        (timbre/info "Initializing analysis")
        (let [{:keys [topics]} (load-classify-settings)
              {:keys [get-trends-fn get-texts-fn]} (run-analysis {:analysis-chan (:analysis-chan processor)
                                                                  :metrics       metrics
                                                                  :database      (:database database)
                                                                  :topics        (map first topics)})]
          (assoc component :get-trends-fn get-trends-fn
                           :get-texts-fn get-texts-fn)))))
  (stop [component]
    (when get-trends-fn
      (timbre/info "Stopping analysis")
      (dissoc component :get-trends-fn :get-texts-fn))))

(defn new-analysis []
  (map->Analysis {}))
