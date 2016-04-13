(ns tech-radar.components.web-server
  (:require [com.stuartsierra.component :as component]
            [immutant.web :as web]
            [immutant.web.undertow :as undertow]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]]
            [tech-radar.services.web-server :refer [create-ring-handler
                                                   allow-cross-origin]]
            [compojure.handler :refer [site]]))

(defn parse-int [v]
  (when v
    (or (and (string? v)
             (Integer/parseInt v))
        v)))

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
                               (allow-cross-origin)
                               (site))
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

