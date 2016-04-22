(ns tech-radar.analytics.protocols)

(defprotocol Analyze
  (trends [this])
  (topic [this topic]))

(defprotocol Storage
  (init [this initial-data]))

(defprotocol Tweet
  (add [this tweet]))
