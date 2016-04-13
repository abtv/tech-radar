(ns tech-radar.components.loader
  (:require [tech-radar.services.loader :refer [run-statuses-filter]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan sliding-buffer close!]]
            [taoensso.timbre :as timbre]
            [tech-radar.utils.settings :refer [load-twitter-security
                                              load-twitter-settings]]))

(defrecord Loader [database metrics input-chan tweet-chan cancel-fn]
  component/Lifecycle
  (start [component]
    (if input-chan
      component
      (do
        (timbre/info "Initializing loader")
        (let [{:keys [track]} (load-twitter-settings)
              {:keys [app-key app-secret user-token user-token-secret]} (load-twitter-security)
              input-chan (chan (sliding-buffer 10))
              tweet-chan (chan 10)
              cancel-fn  (run-statuses-filter {:track             track
                                               :input-chan        input-chan
                                               :tweet-chan        tweet-chan
                                               :app-key           app-key
                                               :app-secret        app-secret
                                               :user-token        user-token
                                               :user-token-secret user-token-secret
                                               :metrics           metrics})]
          (assoc component :input-chan input-chan
                           :tweet-chan tweet-chan
                           :cancel-fn cancel-fn)))))
  (stop [component]
    (when input-chan
      (timbre/info "Stopping loader")
      (cancel-fn)
      (close! input-chan)
      (close! tweet-chan)
      (dissoc component :input-chan :tweet-chan :cancel-fn))))

(defn new-loader []
  (map->Loader {}))
