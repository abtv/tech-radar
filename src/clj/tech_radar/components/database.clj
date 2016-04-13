(ns tech-radar.components.database
  (:require [tech-radar.services.loader :refer [run-statuses-filter]]
            [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan close!]]
            [taoensso.timbre :as timbre]
            [clj-dbcp.core :as cp]
            [environ.core :refer [env]]
            [tech-radar.migrations :refer [migrate]]))

(defrecord Database [database]
  component/Lifecycle
  (start [component]
    (if database
      component
      (do
        (timbre/info "Checking database schema")
        (migrate)
        (timbre/info "Starting database connection pool")
        (let [ds (cp/make-datasource :postgresql {:jdbc-url (:database env)})]
          (assoc component :database {:datasource ds})))))
  (stop [component]
    (timbre/info "Stopping database connection pool")
    (dissoc component :database)))

(defn new-database []
  (map->Database {}))
