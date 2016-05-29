(ns tech-radar.analytics.cache)

(defn new-cache []
  (atom
    {:trends {}
     :texts  {}}))

(defn get-cached-trends [cache]
  (:trends @cache))

(defn set-cached-trends [cache trends]
  (swap! cache assoc-in [:trends] trends))

(defn get-cached-texts [cache topic]
  (get-in @cache [:texts topic]))

(defn set-cached-texts [cache topic texts]
  (swap! cache assoc-in [:texts topic] texts))
