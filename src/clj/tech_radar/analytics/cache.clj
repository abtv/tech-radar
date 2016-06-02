(ns tech-radar.analytics.cache)

(defn new-cache []
  (atom {}))

(defn get-cached-trends [cache]
  @cache)

(defn set-cached-trends [cache trends]
  (reset! cache trends))
