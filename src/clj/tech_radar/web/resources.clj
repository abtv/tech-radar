(ns tech-radar.web.resources
  (:require [tech-radar.utils.resources :refer [resource-handler
                                                entry-params]]))

(defn trends-resource [{:keys [get-trends-fn] :as analysis}]
  (resource-handler entry-params
    :handle-ok (fn [ctx]
                 (get-trends-fn))))

(defn topic-resource [{:keys [get-texts-fn] :as analysis}]
  (resource-handler entry-params
    :handle-ok (fn [{{{topic :topic} :params} :request :as ctx}]
                 (get-texts-fn topic))))
