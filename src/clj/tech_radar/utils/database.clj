(ns tech-radar.utils.database
  (:require [clj-time.coerce :as tc]))

(defn to-underscores [s]
  (.replace s \- \_))

(defn to-dashes [s]
  (.replace s \_ \-))

(defn map->db-fn [{:keys [keyword-columns date-columns]
                   :or   {keyword-columns #{}
                          date-columns    #{}}}]
  (fn [map*]
    (if (empty? map*)
      map*
      (->> (map (fn [[k v]]
                  (cond
                    (keyword-columns k) [k (when v
                                             (name v))]
                    (date-columns k) [k (tc/to-timestamp v)]
                    true [k v])) map*)
           (into {})))))
