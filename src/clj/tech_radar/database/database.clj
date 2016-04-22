(ns tech-radar.database.database
  (:require [clojure.string]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers :refer [insert-into
                                      columns
                                      values]]
            [honeysql.format :as format]
            [tech-radar.utils.database :refer [to-underscores
                                               to-dashes
                                               map->db-fn]]
            [tech-radar.components.counter :refer [increment
                                                   decrement]]))

(def tweet->db (map->db-fn {:date-columns #{:created-at
                                            :user-created-at}}))

(def hashtag->db (map->db-fn {:date-columns #{:created-at}}))

(def topic->db (map->db-fn {:date-columns #{:created-at}}))

(defn insert-tweet [tweet database]
  (let [data (jdbc/insert! database :tweets (-> tweet
                                                (dissoc :hashtags :topics)
                                                (tweet->db)) :entities to-underscores)]
    (-> data
        (first)
        (:id))))

(defn insert-tweet-hashtags [{:keys [created-at topic hashtags]} database]
  (let [hashtags* (map (fn [hashtag]
                         {:created-at created-at
                          :topic      (name topic)
                          :hashtag    hashtag}) hashtags)]
    (doseq [hashtag hashtags*]
      (jdbc/insert! database :hashtags (hashtag->db hashtag) :entities to-underscores))))

(defn insert-tweet-topics [{:keys [tweet-id created-at topics]} database]
  (let [topics* (map (fn [topic]
                       {:tweet-id   tweet-id
                        :created-at created-at
                        :topic      (name topic)}) topics)]
    (doseq [topic topics*]
      (jdbc/insert! database :topics (topic->db topic) :entities to-underscores))))

(def max-texts-to-load 200)

(defn- texts-query [{:keys [topic from to]}]
  (let [limit  max-texts-to-load
        topic* (name topic)]
    (-> {:select   [:tweets.id :tweets.text :tweets.created-at]
         :from     [:tweets]
         :join     [:topics [:= :tweets.id :topics.tweet-id]]
         :where    [:and
                    [:= :topics.topic :?topic]
                    [:>= :topics.created-at :?from]
                    [:<= :topics.created-at :?to]]
         :order-by [[:tweets.created-at :desc]]
         :limit    limit}
        (format/format :params {:topic topic*
                                :from  from
                                :to    to}))))

(defn- hashtags-query [{:keys [topic from to]}]
  (let [topic* (name topic)]
    (-> {:select   [:hashtags.hashtag :%count.*]
         :from     [:hashtags]
         :where    [:and
                    [:= :hashtags.topic :?topic]
                    [:>= :hashtags.created-at :?from]
                    [:<= :hashtags.created-at :?to]]
         :group-by [:hashtags.hashtag]}
        (format/format :params {:topic topic*
                                :from  from
                                :to    to}))))

(defn load-topic [database params]
  (let [texts-query*    (texts-query params)
        texts           (->> (jdbc/query database texts-query* :identifiers to-dashes)
                             (into []))
        hashtags-query* (hashtags-query params)
        hashtags        (->> (jdbc/query database hashtags-query* :identifiers to-dashes)
                             (map (fn [{:keys [hashtag count]}]
                                    [hashtag count]))
                             (into {}))]
    {:texts    texts
     :hashtags hashtags}))

