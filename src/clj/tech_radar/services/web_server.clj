(ns tech-radar.services.web-server
  (:require [bidi.bidi :as bidi]
            [bidi.ring :as bidi-ring]
            [tech-radar.services.resources :refer [trends-resource
                                                  topic-resource]]))

(defn- create-resources [analysis]
  {:trends (trends-resource analysis)
   :topics (topic-resource analysis)})

(def get-routes {"trends"           :trends
                 ["topics/" :topic] :topics})

(def site-routes ["/" {:get get-routes}])

(defn create-ring-handler [analysis]
  (let [resources  (create-resources analysis)
        route      (bidi/compile-route site-routes)
        handler-fn (fn [id]
                     (resources id))]
    (bidi-ring/make-handler route handler-fn)))

(defn allow-cross-origin
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE,OPTIONS")
          (assoc-in [:headers "Access-Control-Allow-Headers"]
                    "X-Requested-With,Content-Type,Cache-Control,token")))))
