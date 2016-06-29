(ns tech-radar.services.web
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan close! <! put!]]
            [ajax.core :as clj-ajax]
            [cemerick.url :refer [url-encode]]))

;(def base-url "http://localhost:3000/")
(def base-url "http://193.124.179.227:3000/")

(defn ajax-request [method url & {:as params}]
  (let [url            (str base-url url)
        result-channel (chan)
        params*        (-> {:handler         #(put! result-channel %)
                            :error-handler   #(put! result-channel {:error %})
                            :finally         #(close! result-channel)
                            :response-format :transit}
                           (merge params))]
    (clj-ajax/easy-ajax-request url method params*)
    result-channel))

(defmulti web (fn [id params]
                id))

(defmethod web :trends/get
  [id params]
  (go (let [url      "trends"
            response (<! (ajax-request :get url))]
        response)))

(defmethod web :topics/get
  [id {:keys [topic] :as params}]
  (go (let [url      (str "topics/" topic)
            response (<! (ajax-request :get url))]
        (if (:error response)
          []
          (sort-by :created-at (fn [x y]
                                 (compare y x)) response)))))

(defmethod web :search/get
  [id {:keys [topic text] :as params}]
  (go (let [url      (str "search/" topic)
            response (<! (ajax-request :post url :params {:text text}))]
        (if (:error response)
          []
          (let [{:keys [total texts]} response]
            {:total total
             :texts (sort-by :created-at (fn [x y]
                                           (compare y x)) texts)})))))
