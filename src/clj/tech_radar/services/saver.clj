(ns tech-radar.services.saver
  (:require [clojure.core.async :refer [thread chan <!! >!! go-loop close!]]
            [clojure.string]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers :refer [insert-into
                                      columns
                                      values]]
            [tech-radar.utils.database :refer [to-underscores
                                               map->db-fn]]
            [taoensso.timbre :as timbre]
            [tech-radar.components.counter :refer [increment
                                                   decrement]]))

(def tweet->db (map->db-fn {:date-columns #{:created-at
                                            :user-created-at}}))

(def hashtag->db (map->db-fn {:date-columns #{:created-at}}))

(def topic->db (map->db-fn {:date-columns #{:created-at}}))

(defn- insert-tweet [tweet database]
  (let [data (jdbc/insert! database :tweets (-> tweet
                                                (dissoc :hashtags :topics)
                                                (tweet->db)) :entities to-underscores)]
    (-> data
        (first)
        (:id))))

(defn- insert-tweet-hashtags [{:keys [created-at topic hashtags]} database]
  (let [hashtags* (map (fn [hashtag]
                         {:created-at created-at
                          :topic      (name topic)
                          :hashtag    hashtag}) hashtags)]
    (doseq [hashtag hashtags*]
      (jdbc/insert! database :hashtags (hashtag->db hashtag) :entities to-underscores))))

(defn- insert-tweet-topics [{:keys [tweet-id created-at topics]} database]
  (let [topics* (map (fn [topic]
                       {:tweet-id   tweet-id
                        :created-at created-at
                        :topic      (name topic)}) topics)]
    (doseq [topic topics*]
      (jdbc/insert! database :topics (topic->db topic) :entities to-underscores))))

(defn- save-tweet [database tweet]
  (try
    (jdbc/with-db-transaction [conn database]
      (let [tweet-id (insert-tweet tweet conn)
            topics   (:topics tweet)]
        (doseq [topic topics]
          (insert-tweet-hashtags (assoc tweet :topic topic) conn))
        (insert-tweet-topics (assoc tweet :tweet-id tweet-id) conn)
        (assoc tweet :id tweet-id)))
    (catch Exception ex
      (timbre/error (.getMessage ex)))))

(defn run-saver [{:keys [save-chan analysis-chan metrics database]}]
  (thread
    (timbre/info "saver started")
    (loop []
      (when-let [tweet (<!! save-chan)]
        (decrement metrics :save-chan)
        (let [tweet (save-tweet database tweet)]
          (increment metrics :analysis-chan)
          (>!! analysis-chan tweet)
          (recur))))
    (timbre/info "saver finished")))
