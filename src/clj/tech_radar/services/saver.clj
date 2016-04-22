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
            [tech-radar.database.database :refer [insert-tweet
                                                  insert-tweet-hashtags
                                                  insert-tweet-topics]]))

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
