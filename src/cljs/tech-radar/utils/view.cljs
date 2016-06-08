(ns tech-radar.utils.view)

(defn prevent-propagation [e]
  (doto e
    (.preventDefault)
    (.stopPropagation)))
