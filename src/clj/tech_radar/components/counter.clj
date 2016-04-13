(ns tech-radar.components.counter)

(defprotocol Counter
  (increment [component counter])
  (decrement [component counter]))
