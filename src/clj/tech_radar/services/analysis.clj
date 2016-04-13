(ns tech-radar.services.analysis
  (:require [clojure.core.async :refer [thread chan <!! >!! close!]]
            [taoensso.timbre :as timbre]
            [tech-radar.components.counter :refer [increment
                                                   decrement]]
            [clojure.java.jdbc :as jdbc]
            [honeysql.format :as format]
            [clj-time.local :as local]
            [clj-time.core :as time]
            [clj-time.coerce :refer [to-sql-time]]
            [tech-radar.utils.database :refer [to-dashes]]))

(defn- get-texts-query [{:keys [topic from to]}]
  (let [limit  200
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

(defn- get-hashtags-query [{:keys [topic from to]}]
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

(defn- load-topic [database params]
  (let [texts-query    (get-texts-query params)
        texts          (->> (jdbc/query database texts-query :identifiers to-dashes)
                            (into []))
        hashtags-query (get-hashtags-query params)
        hashtags       (->> (jdbc/query database hashtags-query :identifiers to-dashes)
                            (map (fn [{:keys [hashtag count]}]
                                   [hashtag count]))
                            (into {}))]
    {:texts    texts
     :hashtags hashtags}))

(defn load-data [database topics]
  (let [from (-> (local/local-now)
                 (time/minus (time/hours 1))
                 (to-sql-time))
        to   (-> (local/local-now)
                 (to-sql-time))]
    (->> topics
         (reduce (fn [data topic]
                   (->> (load-topic database {:topic topic
                                              :from  from
                                              :to    to})
                        (assoc data topic))) {}))))

(defn- add-topic-fn [tweet-info hashtags]
  (fn [data topic]
    (let [data* (update-in data [topic :texts] (fn [values]
                                                 (if values
                                                   (conj values tweet-info)
                                                   [tweet-info])))]
      (update-in data* [topic :hashtags] (fn [values]
                                           (let [values* (or values {})]
                                             (reduce (fn [acc v]
                                                       (update-in acc [v] (fn [x]
                                                                            (if x
                                                                              (inc x)
                                                                              1)))) values* hashtags)))))))

(defn- add-tweet [data tweet]
  (->> tweet
       (swap! data (fn [data {:keys [topics hashtags] :as tweet}]
                     (let [tweet-info (select-keys tweet [:id :text :created-at :hashtags])]
                       (reduce (add-topic-fn tweet-info hashtags) data topics))))))

(defn- get-trends [data]
  (->> @data
       (map (fn [[topic {hashtags :hashtags}]]
              [topic hashtags]))
       (into {})))

(defn- get-texts [data topic]
  (or (get-in @data [(keyword topic) :texts]) []))

(defn run-analysis [{:keys [analysis-chan metrics database topics]}]
  (let [data (-> database
                 (load-data topics)
                 (atom))]
    (thread
      (timbre/info "analysis started")
      (loop []
        (when-let [tweet (<!! analysis-chan)]
          (decrement metrics :analysis-chan)
          (add-tweet data tweet)
          (increment metrics :total-texts)
          (recur)))
      (timbre/info "analysis finished"))
    {:get-trends-fn (fn []
                      (get-trends data))
     :get-texts-fn  (fn [topic]
                      (get-texts data topic))}))
