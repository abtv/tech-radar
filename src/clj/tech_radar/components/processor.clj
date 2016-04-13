(ns tech-radar.components.processor
  (:require [tech-radar.services.processor :refer [run-tweet-processing]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan close!]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [tech-radar.utils.settings :refer [load-classify-settings]]
            [tech-radar.services.classifier :refer [index-topics]]))

(defrecord Processor [database metrics loader analysis-chan]
  component/Lifecycle
  (start [component]
    (if analysis-chan
      component
      (do
        (timbre/info "Initializing processor")
        (let [{:keys [languages topics
                      max-topics-per-tweet
                      max-hashtags-per-tweet]} (load-classify-settings)
              analysis-chan (chan 10)]
          (run-tweet-processing {:tweet-chan             (:tweet-chan loader)
                                 :analysis-chan          analysis-chan
                                 :database               (:database database)
                                 :metrics                metrics
                                 :languages              (set languages)
                                 :indexed-topics         (index-topics topics)
                                 :max-topics-per-tweet   max-topics-per-tweet
                                 :max-hashtags-per-tweet max-hashtags-per-tweet})
          (assoc component :analysis-chan analysis-chan)))))
  (stop [component]
    (when analysis-chan
      (timbre/info "Stopping processor")
      (close! analysis-chan)
      (dissoc component :analysis-chan))))

(defn new-processor []
  (map->Processor {}))
