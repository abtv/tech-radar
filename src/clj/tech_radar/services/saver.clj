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
                                                   decrement]]
            [tech-radar.database.tweets :refer [insert-tweet
                                                insert-tweet-topics]]
            [tech-radar.database.hashtags :refer [insert-tweet-hashtags]]))

(defn- save-tweet [database tweet]
  (try
    (when-let [topics (seq (:topics tweet))]
      (jdbc/with-db-transaction [conn database]
        (let [tweet-id (insert-tweet tweet conn)]
          (doseq [topic topics]
            (insert-tweet-hashtags (assoc tweet :topic topic) conn))
          (insert-tweet-topics (assoc tweet :tweet-id tweet-id) conn)
          (assoc tweet :id tweet-id))))
    (catch Exception ex
      (timbre/error (.getMessage ex))
      nil)))

(defn run [{:keys [save-chan analysis-chan metrics database]}]
  (thread
    (timbre/info "saver started")
    (loop []
      (when-let [tweet (<!! save-chan)]
        (decrement metrics :save-chan)
        (when-let [tweet (save-tweet database tweet)]
          (increment metrics :analysis-chan)
          (>!! analysis-chan {:type  :tweet
                              :tweet tweet}))
        (recur)))
    (timbre/info "saver finished")))
