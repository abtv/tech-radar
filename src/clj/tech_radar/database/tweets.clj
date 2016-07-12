(ns tech-radar.database.tweets
  (:require [clojure.string]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers :refer [insert-into
                                      columns
                                      values]]
            [honeysql.format :as format]
            [tech-radar.utils.database :refer [to-underscores
                                               to-dashes
                                               map->db-fn]]
            [clj-time.coerce :refer [to-sql-time]]))

(def tweet->db (map->db-fn {:date-columns #{:created-at
                                            :user-created-at}}))

(def topic->db (map->db-fn {:date-columns #{:created-at}}))

(defn insert-tweet [tweet database]
  (let [data (jdbc/insert! database :tweets (-> tweet
                                                (select-keys [:twitter-id :text :created-at :retweeted
                                                              :user-created-at :user-name :user-location :user-description
                                                              :followers-count :friends-count :statuses-count :user-lang])
                                                (tweet->db)) :entities to-underscores)]
    (-> data
        (first)
        (:id))))

(defn insert-tweet-topics [{:keys [tweet-id created-at topics]} database]
  (let [topics* (map (fn [topic]
                       {:tweet-id   tweet-id
                        :created-at created-at
                        :topic      (name topic)}) topics)]
    (doseq [topic topics*]
      (jdbc/insert! database :topics (topic->db topic) :entities to-underscores))))

(defn- tweets-per-topic-query [topic max-records-count]
  (let [topic* (name topic)]
    (-> {:select   [:tweets.id :tweets.twitter-id :tweets.text :tweets.created-at]
         :from     [:tweets]
         :join     [:topics [:= :tweets.id :topics.tweet-id]]
         :where    [:= :topics.topic :?topic]
         :order-by [[:tweets.created-at :desc]]
         :limit    max-records-count}
        (format/format :params {:topic topic*}))))

(defn load-tweets-per-topic [database {:keys [topic max-record-count]}]
  (let [tweets-query* (tweets-per-topic-query topic max-record-count)]
    (->> (jdbc/query database tweets-query* :identifiers to-dashes)
         (reverse)
         (into []))))

(defn- tweets-query [max-record-count]
  (-> {:select   [:tweets.id :tweets.twitter-id :tweets.text :tweets.created-at :topics.topic]
       :from     [:tweets]
       :join     [:topics [:= :tweets.id :topics.tweet-id]]
       :order-by [[:tweets.created-at :desc]]
       :limit    max-record-count}
      (format/format)))

(defn- to-tweet [{:keys [id twitter-id text created-at topic]}]
  (let [topic (keyword topic)]
    {:id         id
     :twitter-id twitter-id
     :text       text
     :created-at created-at
     :topics     #{topic}}))

(defn- join-to-tweets [xs]
  (when (seq xs)
    (loop [tweet  (-> (first xs)
                      (to-tweet))
           xs     (next xs)
           tweets []]
      (let [x (first xs)]
        (cond
          (nil? x) (conj tweets tweet)
          (= (:id tweet) (:id x)) (recur (update-in tweet [:topics] conj (-> (:topic x)
                                                                             (keyword)))
                                         (next xs)
                                         tweets)
          :else (recur (to-tweet x)
                       (next xs)
                       (conj tweets tweet)))))))

(defn load-tweets [database max-record-count]
  (let [tweets-query* (tweets-query max-record-count)
        tweets        (jdbc/query database tweets-query* :identifiers to-dashes)]
    (-> (reverse tweets)
        (join-to-tweets))))
