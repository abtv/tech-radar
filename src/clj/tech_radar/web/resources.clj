(ns tech-radar.web.resources
  (:require [tech-radar.utils.resources :refer [resource-handler
                                                get-resource
                                                post-resource]]))

(defn trends-resource [{:keys [get-trends-fn] :as analysis}]
  (resource-handler get-resource
    :handle-ok (fn [ctx]
                 (get-trends-fn))))

(defn topic-resource [{:keys [get-texts-fn] :as analysis}]
  (resource-handler get-resource
    :handle-ok (fn [{{{topic :topic} :params} :request :as ctx}]
                 (get-texts-fn (keyword topic)))))

(defn search-resource [{:keys [search-fn] :as analysis}]
  (resource-handler post-resource
    :post! (fn [{{{topic :topic} :params
                  {text :text}   :body} :request :as ctx}]
             {:result (search-fn (keyword topic) text)})
    :handle-created :result))
