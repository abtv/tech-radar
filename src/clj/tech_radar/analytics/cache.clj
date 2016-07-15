(ns tech-radar.analytics.cache)

(defn new-cache []
  (atom {}))

(defn get-cached-trends [cache]
  @cache)

(defn set-cached-trends [cache trends]
  (swap! cache (fn [cache]
                 (reduce (fn [cache [topic hashtags]]
                           (assoc-in cache [topic :hashtags] hashtags))
                         cache trends))))
