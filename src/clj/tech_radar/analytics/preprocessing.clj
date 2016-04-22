(ns tech-radar.analytics.preprocessing
  (:require [taoensso.timbre :as timbre]
            [tech-radar.analytics.classification :refer [classify
                                                         get-hashtags]]
            [tech-radar.utils.parsers :refer [parse-twitter-date]]))

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

(defn stream-preprocessor [{:keys [indexed-topics
                                   max-topics-per-tweet
                                   max-hashtags-per-tweet]}]
  (comp (filter identity)
        (map streamed-tweet->tweet)
        (map (partial enrich-tweet-with-topics indexed-topics max-topics-per-tweet))
        (map (partial enrich-tweet-with-hashtags max-hashtags-per-tweet))))
