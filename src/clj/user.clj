(ns user
  (:require [reloaded.repl :refer [system init start stop go reset]]
            [tech-radar.systems :refer [new-system]]
            [environ.core :refer [env]]))

(reloaded.repl/set-init! new-system)
