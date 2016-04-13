(ns tech-radar.services.processor
  (:require [clojure.core.async :refer [thread chan <!! >!! close!]]
            [clojure.string]
            [taoensso.timbre :as timbre]
            [tech-radar.components.counter :refer [increment
                                                   decrement]]
            [tech-radar.utils.parsers :refer [parse-twitter-date]]
            [tech-radar.services.classifier :refer [classify
                                                    get-hashtags]]
            [tech-radar.services.saver :refer [run-saver]]))

(defn- streamed-tweet->tweet [{:keys [id-str text created-at retweeted user]}]
  {:twitter-id       id-str
   :text             text
   :created-at       (parse-twitter-date created-at)
   :retweeted        retweeted
   :user-created-at  (-> user
                         (:created-at)
                         (parse-twitter-date))
   :user-name        (:name user)
   :user-location    (:location user)
   :user-description (:description user)
   :followers-count  (:followers-count user)
   :friends-count    (:friends-count user)
   :statuses-count   (:statuses-count user)
   :user-lang        (:lang user)})

(defn- enrich-tweet-with-topics [indexed-topics max-topics-per-tweet {text :text
                                                                      :as  tweet}]
  (let [topics* (->> (classify text indexed-topics)
                     (take max-topics-per-tweet))]
    (when-not (seq topics*)
      (timbre/warn (str "Can't classify text: \"" text "\"")))
    (assoc tweet :topics topics*)))

(defn- remove-hash-sign [hashtag]
  (if (.startsWith hashtag "#")
    (subs hashtag 1)
    hashtag))

(defn- enrich-tweet-with-hashtags [max-hashtags-per-tweet {text :text
                                                           :as  tweet}]
  (->> (get-hashtags text)
       (map remove-hash-sign)
       (take max-hashtags-per-tweet)
       (assoc tweet :hashtags)))

(defn- stream-processor [{:keys [indexed-topics
                                 max-topics-per-tweet
                                 max-hashtags-per-tweet]}]
  (comp (filter identity)
        (map streamed-tweet->tweet)
        (map (partial enrich-tweet-with-topics indexed-topics max-topics-per-tweet))
        (map (partial enrich-tweet-with-hashtags max-hashtags-per-tweet))))

(defn run-tweet-processing [{:keys [tweet-chan analysis-chan metrics languages database] :as params}]
  (thread
    (timbre/info "twit-processing started")
    (let [save-chan (->> (stream-processor params)
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
