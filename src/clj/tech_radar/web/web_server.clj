(ns tech-radar.web.web-server
  (:require [bidi.bidi :as bidi]
            [bidi.ring :as bidi-ring]
            [tech-radar.web.resources :as resources]
            [taoensso.timbre :as timbre]
            [ring.util.response :refer [charset]]))

(defn- create-resources [analysis]
  {:statistic (resources/statistic-resource analysis)
   :trends    (resources/trends-resource analysis)
   :topics    (resources/topic-resource analysis)
   :search    (resources/search-resource analysis)
   :index     (resources/index-resource analysis)})

(def get-routes {"trends"           :trends
                 ["topics/" :topic] :topics
                 "index"            :index
                 "statistic"        :statistic})

(def post-routes {["search/" :topic] :search})

(def site-routes ["/" {:get  get-routes
                       :post post-routes}])

(defn create-ring-handler [analysis]
  (let [resources  (create-resources analysis)
        route      (bidi/compile-route site-routes)
        handler-fn (fn [id]
                     (resources id))]
    (bidi-ring/make-handler route handler-fn)))

(defn wrap-charset [handler]
  (fn [request]
    (let [response (handler request)]
      (charset response "UTF-8"))))

(defn allow-cross-origin [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (assoc-in [:headers "Access-Control-Allow-Origin"] "*")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE,OPTIONS")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "X-Requested-With,Content-Type,Cache-Control,token")))))

(defn wrap-exception [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (timbre/error e)
        {:status 500
         :body   "Server error"}))))
