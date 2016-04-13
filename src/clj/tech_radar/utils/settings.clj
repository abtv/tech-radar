(ns tech-radar.utils.settings
  (:require [clojure.edn :as edn]
            [environ.core :refer [env]]))

(defn load-parameter [parameter]
  (-> (parameter env)
      (slurp)
      (edn/read-string)))

(defn load-twitter-settings []
  (load-parameter :twitter-settings))

(defn load-twitter-security []
  (load-parameter :twitter-security))

(defn load-classify-settings []
  (load-parameter :classify-settings))
