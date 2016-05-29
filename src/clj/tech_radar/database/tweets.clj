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

(defn- tweets-query [topic max-tweet-count]
  (let [topic* (name topic)]
    (-> {:select   [:tweets.id :tweets.twitter-id :tweets.text :tweets.created-at]
         :from     [:tweets]
         :join     [:topics [:= :tweets.id :topics.tweet-id]]
         :where    [:= :topics.topic :?topic]
         :order-by [[:tweets.created-at :desc]]
         :limit    max-tweet-count}
        (format/format :params {:topic topic*}))))

(defn load-tweets [database {:keys [topic max-tweet-count]}]
  (let [tweets-query* (tweets-query topic max-tweet-count)]
    (->> (jdbc/query database tweets-query* :identifiers to-dashes)
         (reverse)
         (into []))))
