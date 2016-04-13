(ns tech-radar.core
  (:require [reloaded.repl :refer [go]]
            [tech-radar.systems :refer [new-system]]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn -main [& args]
  (timbre/info "Starting collector")
  (reloaded.repl/set-init! new-system)
  (go))
