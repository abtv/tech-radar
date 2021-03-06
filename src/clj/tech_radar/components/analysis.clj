(ns tech-radar.components.analysis
  (:require [tech-radar.services.analysis :refer [load-data
                                                  cache-update-fn
                                                  run-model-update
                                                  run-hashtags-update
                                                  run-cache-update]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan close!]]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [tech-radar.utils.parsers :refer [parse-int
                                              parse-double]]
            [tech-radar.utils.settings :refer [load-classify-settings
                                               load-hashtag-filter-settings]]
            [tech-radar.analytics.model :refer [new-model]]
            [tech-radar.analytics.cache :refer [new-cache
                                                get-cached-trends]]
            [tech-radar.analytics.protocols :as protocols]
            [immutant.scheduling :refer [schedule every in stop id]]
            [tech-radar.services.hype-meter :as hype-meter]
            [clojure.string :as s]))

(defn- get-settings []
  {:max-hashtags-per-trend (-> (env :max-hashtags-per-trend)
                               (parse-int))
   :max-texts-per-request  (-> (env :max-texts-per-request)
                               (parse-int))
   :max-tweet-count        (-> (env :max-tweet-count)
                               (parse-int))
   :cache-update-timeout-s (-> (env :cache-update-timeout-s)
                               (parse-int))})

(defn- load-stop-words [file-name]
  (->> (slurp file-name)
       (s/split-lines)
       (filter (comp not s/blank?))
       (set)))

(defrecord Analysis [database metrics preprocessor
                     stop-hashtags-update-fn stop-cache-update-fn
                     hype-meter-job
                     trends-fn texts-fn search-fn index-info-fn]
  component/Lifecycle
  (start [component]
    (if trends-fn
      component
      (do
        (timbre/info "Initializing analysis")
        (let [{:keys [topics]} (load-classify-settings)
              hashtag-filter-settings (load-hashtag-filter-settings)
              {:keys [max-hashtags-per-trend max-texts-per-request max-tweet-count cache-update-timeout-s]} (get-settings)
              topics                  (map first topics)
              database                (:database database)
              analysis-chan           (:analysis-chan preprocessor)
              model                   (new-model topics {:max-tweet-count         max-tweet-count
                                                         :max-hashtags-per-trend  max-hashtags-per-trend
                                                         :max-texts-per-request   max-texts-per-request
                                                         :hashtag-filter-settings hashtag-filter-settings})
              cache                   (new-cache)
              initial-data            (load-data database {:topics                  topics
                                                           :max-texts-per-request   max-texts-per-request
                                                           :max-tweet-count         max-tweet-count
                                                           :hashtag-filter-settings hashtag-filter-settings})
              _                       (do
                                        (protocols/init model initial-data)
                                        (cache-update-fn model cache)
                                        (run-model-update {:model         model
                                                           :analysis-chan analysis-chan
                                                           :metrics       metrics}))
              stop-hashtags-update-fn (run-hashtags-update {:database                database
                                                            :topics                  topics
                                                            :hashtag-filter-settings hashtag-filter-settings
                                                            :analysis-chan           analysis-chan
                                                            :metrics                 metrics})
              stop-cache-update-fn    (run-cache-update {:model                  model
                                                         :cache                  cache
                                                         :cache-update-timeout-s cache-update-timeout-s})
              hype-meter-fn           (hype-meter/new-hype-meter-fn {:cache                cache
                                                                     :database             database
                                                                     :topics               topics
                                                                     :hype-tweet-count     (-> (env :hype-tweet-count)
                                                                                               (parse-int))
                                                                     :similarity-threshold (-> (env :similarity-threshold)
                                                                                               (parse-double))
                                                                     :stop-words           (-> (env :stop-words)
                                                                                               (load-stop-words))})]
          (assoc component :stop-hashtags-update-fn stop-hashtags-update-fn
                           :stop-cache-update-fn stop-cache-update-fn
                           :hype-meter-job (schedule hype-meter-fn
                                                     (-> (id :hype-meter)
                                                         (in 0 :minute)
                                                         (every 1 :hours)))
                           :statistic-fn (fn []
                                           (protocols/statistic model))
                           :trends-fn (fn []
                                        (get-cached-trends cache))
                           :texts-fn (fn [topic]
                                       (protocols/texts model topic))
                           :search-fn (fn [topic text]
                                        (protocols/search model topic text))
                           :index-info-fn (fn []
                                            (protocols/index-info model)))))))
  (stop [component]
    (when stop-cache-update-fn
      (timbre/info "Stopping analysis")
      (if (stop hype-meter-job)
        (timbre/info "Stopped hype-meter-job")
        (timbre/error "Failed to stop hype-meter-job"))
      (stop-cache-update-fn)
      (stop-hashtags-update-fn)
      (dissoc component :stop-hashtags-update-fn :stop-cache-update-fn :hype-meter-job :trends-fn :texts-fn :search-fn :index-info-fn))))

(defn new-analysis []
  (map->Analysis {}))
