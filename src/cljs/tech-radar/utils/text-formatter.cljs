(ns tech-radar.utils.text-formatter
  (:require [clojure.string :as s]))

(def splitter #"\s")

(defn- create-link [text key]
  [:a {:key    key
       :href   text
       :target "_blank"}
   (str text " ")])

(defn format [text key]
  (let [chunks (s/split text splitter)]
    (->> chunks
         (map-indexed (fn [idx chunk]
                        (let [child-key (str key "-" idx)]
                          (cond
                            (.startsWith chunk "http://") (create-link chunk child-key)
                            (.startsWith chunk "https://") (create-link chunk child-key)
                            :else [:span {:key child-key} (str chunk " ")])))))))
