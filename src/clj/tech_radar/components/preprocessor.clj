(ns tech-radar.components.preprocessor
  (:require [tech-radar.services.preprocessor :refer [run]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan close!]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [tech-radar.utils.settings :refer [load-classify-settings]]
            [tech-radar.analytics.classification :refer [index-topics]]))

(defrecord Preprocessor [database metrics loader analysis-chan]
  component/Lifecycle
  (start [component]
    (if analysis-chan
      component
      (do
        (timbre/info "Initializing preprocessor")
        (let [{:keys [languages topics
                      max-topics-per-tweet
                      max-hashtags-per-tweet]} (load-classify-settings)
              analysis-chan (chan 10)]
          (run {:tweet-chan             (:tweet-chan loader)
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
      (timbre/info "Stopping preprocessor")
      (close! analysis-chan)
      (dissoc component :analysis-chan))))

(defn new-preprocessor []
  (map->Preprocessor {}))
