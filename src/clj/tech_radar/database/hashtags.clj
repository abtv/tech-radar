(ns tech-radar.database.hashtags
  (:require [clojure.string]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers :refer [insert-into
                                      columns
                                      values]]
            [honeysql.format :as format]
            [tech-radar.utils.database :refer [to-underscores
                                               to-dashes
                                               map->db-fn]]
            [clj-time.core :as time]
            [clj-time.local :as local]
            [clj-time.coerce :refer [to-sql-time]]))

(def hashtag->db (map->db-fn {:date-columns #{:created-at}}))

(defn insert-tweet-hashtags [{:keys [created-at topic hashtags]} database]
  (let [hashtags* (map (fn [hashtag]
                         {:created-at created-at
                          :topic      (name topic)
                          :hashtag    hashtag}) hashtags)]
    (doseq [hashtag hashtags*]
      (jdbc/insert! database :hashtags (hashtag->db hashtag) :entities to-underscores))))

(defn- hashtags-query [{:keys [topic from to max-hashtags]}]
  (let [topic* (name topic)]
    (-> {:select   [:hashtags.hashtag :%count.*]
         :from     [:hashtags]
         :where    [:and
                    [:= :hashtags.topic :?topic]
                    [:>= :hashtags.created-at :?from]
                    [:<= :hashtags.created-at :?to]]
         :order-by [[:%count.* :desc]]
         :group-by [:hashtags.hashtag]
         :limit    max-hashtags}
        (format/format :params {:topic topic*
                                :from  from
                                :to    to}))))

(defn hashtags->map [max-hashtags data]
  (->> data
       (sort-by second >)
       (take max-hashtags)
       (into {})))

(defn- load-hashtags* [database {:keys [topic offset-back max-hashtags hashtag-filter]}]
  (let [from            (-> (local/local-now)
                            (time/minus offset-back)
                            (to-sql-time))
        to              (-> (local/local-now)
                            (to-sql-time))
        hashtags-query* (hashtags-query {:topic        topic
                                         :from         from
                                         :to           to
                                         :max-hashtags (+ max-hashtags (* 10 (count hashtag-filter)))})]
    (->> (jdbc/query database hashtags-query* :identifiers to-dashes)
         (map (fn [{:keys [hashtag count]}]
                [(.toLowerCase hashtag) count]))
         (reduce (fn [coll [hashtag count]]
                   (if (hashtag-filter hashtag)
                     coll
                     (if-let [old-count (get coll hashtag)]
                       (assoc coll hashtag (+ old-count count))
                       (assoc coll hashtag count)))) {})
         (hashtags->map max-hashtags))))

(defn load-daily-hashtags [database topic hashtag-filter]
  (load-hashtags* database {:topic          topic
                            :offset-back    (time/days 1)
                            :max-hashtags   1000
                            :hashtag-filter hashtag-filter}))

(defn load-weekly-hashtags [database topic hashtag-filter]
  (load-hashtags* database {:topic          topic
                            :offset-back    (time/weeks 1)
                            :max-hashtags   25
                            :hashtag-filter hashtag-filter}))

(defn load-monthly-hashtags [database topic hashtag-filter]
  (load-hashtags* database {:topic          topic
                            :offset-back    (time/months 1)
                            :max-hashtags   25
                            :hashtag-filter hashtag-filter}))
