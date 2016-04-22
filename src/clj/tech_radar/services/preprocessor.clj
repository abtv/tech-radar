(ns tech-radar.services.preprocessor
  (:require [clojure.core.async :refer [thread chan <!! >!! close!]]
            [clojure.string]
            [taoensso.timbre :as timbre]
            [tech-radar.analytics.preprocessing :refer [stream-preprocessor]]
            [tech-radar.components.counter :refer [increment
                                                   decrement]]
            [tech-radar.services.saver :refer [run-saver]]))

(defn run [{:keys [tweet-chan analysis-chan metrics languages database] :as params}]
  (thread
    (timbre/info "twit-processing started")
    (let [save-chan (->> (stream-preprocessor params)
                         (chan 10))]
      (run-saver {:save-chan     save-chan
                  :analysis-chan analysis-chan
                  :metrics       metrics
                  :database      database})
      (loop []
        (when-let [tweet (<!! tweet-chan)]
          (decrement metrics :tweet-chan)
          (when (-> tweet
                    (:user)
                    (:lang)
                    (languages))
            (increment metrics :save-chan)
            (>!! save-chan tweet))
          (recur)))
      (close! save-chan))
    (timbre/info "twit-processing finished")))
