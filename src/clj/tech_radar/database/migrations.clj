(ns tech-radar.database.migrations
  (:require [environ.core :refer [env]]
            [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]))

(defn- get-config []
  {:datastore  (jdbc/sql-database {:connection-uri (env :database)})
   :migrations (jdbc/load-resources "migrations")})

(defn migrate []
  (-> (get-config)
      (repl/migrate)))

(defn rollback []
  (-> (get-config)
      (repl/rollback)))
