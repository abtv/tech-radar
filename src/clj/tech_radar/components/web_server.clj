(ns tech-radar.components.web-server
  (:require [com.stuartsierra.component :as component]
            [immutant.web :as web]
            [immutant.web.undertow :as undertow]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [tech-radar.web.web-server :refer [create-ring-handler
                                               wrap-charset
                                               allow-cross-origin
                                               wrap-exception]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.transit :refer [wrap-transit-body]]
            [tech-radar.utils.parsers :refer [parse-int]]))

(defrecord WebServer [database metrics analysis web-server]
  component/Lifecycle
  (start [component]
    (if web-server
      component
      (do
        (let [host         (:host env)
              port         (-> env
                               (:port)
                               (parse-int))
              _            (timbre/info "Starting web server on host:" host ", with port:" port)
              ring-handler (-> (create-ring-handler analysis)
                               (wrap-charset)
                               (allow-cross-origin)
                               (wrap-transit-body {:keywords? true :opts {}})
                               (wrap-exception))
              web-server   (->> (undertow/options {:host host
                                                   :port port})
                                (web/run ring-handler))]
          (assoc component :web-server web-server)))))
  (stop [component]
    (timbre/info "Stopping web server")
    (when web-server
      (web/stop web-server)
      (dissoc component :web-server))))

(defn new-web-server
  []
  (map->WebServer {}))

