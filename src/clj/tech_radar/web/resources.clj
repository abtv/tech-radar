(ns tech-radar.web.resources
  (:require [tech-radar.utils.resources :refer [resource-handler
                                                get-resource
                                                post-resource]]))

(defn trends-resource [{:keys [trends-fn] :as analysis}]
  (resource-handler get-resource
    :handle-ok (fn [ctx]
                 (trends-fn))))

(defn topic-resource [{:keys [texts-fn] :as analysis}]
  (resource-handler get-resource
    :handle-ok (fn [{{{topic :topic} :params} :request :as ctx}]
                 (texts-fn (keyword topic)))))

(defn search-resource [{:keys [search-fn] :as analysis}]
  (resource-handler post-resource
    :post! (fn [{{{topic :topic} :params
                  {text :text}   :body} :request :as ctx}]
             {:result (search-fn (keyword topic) text)})
    :handle-created :result))

(defn index-resource [{:keys [index-info-fn] :as analysis}]
  (resource-handler get-resource
    :handle-ok (fn [ctx]
                 (index-info-fn))))
