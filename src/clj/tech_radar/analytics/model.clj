(ns tech-radar.analytics.model
  (:require [tech-radar.analytics.protocols :refer [Analyze
                                                    Storage
                                                    Tweet]]
            [tech-radar.analytics.search :as search :refer [new-search
                                                            add-text
                                                            remove-oldest-item
                                                            search-texts]]))

(defn- remove-old-tweets [tweets max-tweet-count]
  (let [tweet-count (count tweets)]
    (if (> tweet-count max-tweet-count)
      (subvec tweets (- tweet-count max-tweet-count))
      tweets)))

(defn- add-topic-fn [tweet-model hashtags max-tweet-count hashtag-filter-settings]
  (fn [data topic]
    (let [data*          (update-in data [topic :texts] (fnil (fn [coll]
                                                                (-> coll
                                                                    (conj tweet-model)
                                                                    (remove-old-tweets max-tweet-count))) []))
          hashtag-filter (topic hashtag-filter-settings)
          hashtags       (->> hashtags
                              (map (fn [^String hashtag]
                                     (.toLowerCase hashtag)))
                              (filter (fn [^String hashtag]
                                        (-> (contains? hashtag-filter hashtag)
                                            (not)))))]
      (update-in data* [topic :hashtags :daily]
                 (fn [values]
                   (let [values* (or values {})]
                     (reduce (fn [acc v]
                               (update-in acc [v] (fnil inc 0))) values* hashtags)))))))

(defn- to-tweet-model [tweet]
  (select-keys tweet [:id :text :created-at :twitter-id]))

(defn- add* [data tweet max-tweet-count hashtag-filter-settings]
  (swap! data (fn [data {:keys [topics hashtags] :as tweet}]
                (let [tweet-model (to-tweet-model tweet)]
                  (reduce (add-topic-fn tweet-model hashtags max-tweet-count hashtag-filter-settings) data topics))) tweet))

(defn- get-top-hashtags [max-count trends]
  (->> trends
       (map (fn [[hashtag-type data]]
              [hashtag-type (->> data
                                 (sort-by (comp - second))
                                 (take max-count)
                                 (into {}))]))
       (into {})))

(defn- get-trends [data settings]
  (let [{:keys [max-hashtags-per-trend]} settings]
    (->> @data
         (map (fn [[topic {hashtags :hashtags}]]
                [topic (get-top-hashtags max-hashtags-per-trend hashtags)]))
         (into {}))))

(defn- add-tweets [search tweets statistic]
  (doseq [tweet tweets]
    (add-text search tweet statistic)))

(defrecord Model [data search settings topics statistic]
  Storage
  (init [this initial-data]
    (let [{:keys [data search statistic]} this
          {:keys [topics tweets]} initial-data]
      (reset! data topics)
      (add-tweets search tweets statistic))
    nil)
  (reset-trends [this hashtags-type hashtags]
    (doseq [[topic hashtags] hashtags]
      (swap! (:data this) assoc-in [topic :hashtags hashtags-type] hashtags))
    nil)
  Tweet
  (add [this tweet]
    (let [{:keys [data search statistic]} this
          {:keys [max-texts-per-request max-tweet-count hashtag-filter-settings]} (:settings this)]
      (add* data tweet max-texts-per-request hashtag-filter-settings)
      (add-text search tweet statistic)
      (let [texts-count (-> @search
                            (:texts)
                            (count))]
        (when (> texts-count max-tweet-count)
          (remove-oldest-item search statistic))))
    nil)
  Analyze
  (statistic [this]
    (->> (:statistic this)
         (deref)
         (map (fn [[k v]]
                {:hashtag k
                 :count   v}))
         (sort-by :hashtag)
         (into [])))
  (search [this topic text]
    (search-texts (:search this) topic text))
  (index-info [this]
    (search/index-info (:search this)))
  (trends [this]
    (let [{:keys [data settings]} this]
      (get-trends data settings)))
  (texts [this topic]
    (let [{:keys [data]} this]
      (or (get-in @data [(keyword topic) :texts])
          []))))

(defn- lowercase-set [set]
  (->> (map (fn [^String s]
              (.toLowerCase s)) set)
       (into #{})))

(defn- lowercase-settings [settings]
  (reduce (fn [settings [topic filter-settings]]
            (assoc settings topic (lowercase-set filter-settings))) {} settings))

(defn new-model [topics settings]
  (let [{:keys [max-tweet-count max-hashtags-per-trend max-texts-per-request hashtag-filter-settings]} settings]
    (when-not max-tweet-count
      (throw (Exception. "you have to provide max-tweet-count param")))
    (when-not max-texts-per-request
      (throw (Exception. "you have to provide max-texts-per-request param")))
    (when-not max-hashtags-per-trend
      (throw (Exception. "you have to provide max-hashtags-per-trend param")))
    (when-not hashtag-filter-settings
      (throw (Exception. "you have to provide hashtag-filter-settings param")))

    (map->Model {:data      (atom {})
                 :search    (new-search)
                 :settings  (assoc settings :hashtag-filter-settings (lowercase-settings hashtag-filter-settings))
                 :topics    topics
                 :statistic (atom {})})))
