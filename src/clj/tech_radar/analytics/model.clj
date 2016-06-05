(ns tech-radar.analytics.model
  (:require [tech-radar.analytics.protocols :refer [Analyze
                                                    Storage
                                                    Tweet]]
            [tech-radar.analytics.search :refer [new-search
                                                 add-text
                                                 remove-oldest-item
                                                 search-texts]]))

(defn- remove-old-tweets [tweets max-tweet-count]
  (let [tweet-count (count tweets)]
    (if (> tweet-count max-tweet-count)
      (subvec tweets (- tweet-count max-tweet-count))
      tweets)))

(defn- add-topic-fn [tweet-model hashtags max-tweet-count]
  (fn [data topic]
    (let [data* (update-in data [topic :texts] (fnil (fn [coll]
                                                       (-> coll
                                                           (conj tweet-model)
                                                           (remove-old-tweets max-tweet-count))) []))]
      (update-in data* [topic :hashtags :daily]
                 (fn [values]
                   (let [values* (or values {})]
                     (reduce (fn [acc v]
                               (update-in acc [v] (fnil inc 0))) values* hashtags)))))))

(defn- to-tweet-model [tweet]
  (select-keys tweet [:id :text :created-at]))

(defn- add* [data tweet max-tweet-count]
  (swap! data (fn [data {:keys [topics hashtags] :as tweet}]
                (let [tweet-model (to-tweet-model tweet)]
                  (reduce (add-topic-fn tweet-model hashtags max-tweet-count) data topics))) tweet))

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

(defn- add-tweets [search tweets]
  (doseq [tweet tweets]
    (add-text search tweet)))

(defrecord Model [data search settings topics]
  Storage
  (init [this initial-data]
    (let [{:keys [data search]} this
          {:keys [topics tweets]} initial-data]
      (reset! data topics)
      (add-tweets search tweets))
    nil)
  (reset-trends [this hashtags-type hashtags]
    (doseq [[topic hashtags] hashtags]
      (swap! (:data this) assoc-in [topic :hashtags hashtags-type] hashtags))
    nil)
  Tweet
  (add [this tweet]
    (let [{:keys [data search]} this
          {:keys [max-texts-per-request max-tweet-count]} (:settings this)]
      (add* data tweet max-texts-per-request)
      (add-text search tweet)
      (let [texts-count (-> @search
                            (:texts)
                            (count))]
        (when (> texts-count max-tweet-count)
          (remove-oldest-item search))))
    nil)
  Analyze
  (search [this topic text]
    (search-texts (:search this) topic text))
  (trends [this]
    (let [{:keys [data settings]} this]
      (get-trends data settings)))
  (texts [this topic]
    (let [{:keys [data]} this]
      (or (get-in @data [(keyword topic) :texts])
          []))))

(defn new-model [topics settings]
  (let [{:keys [max-tweet-count max-hashtags-per-trend max-texts-per-request]} settings]
    (when-not max-tweet-count
      (throw (Exception. "you have to provide max-tweet-count param")))
    (when-not max-texts-per-request
      (throw (Exception. "you have to provide max-texts-per-request param")))
    (when-not max-hashtags-per-trend
      (throw (Exception. "you have to provide max-hashtags-per-trend param")))

    (map->Model {:data     (atom {})
                 :search   (new-search)
                 :settings settings
                 :topics   topics})))
