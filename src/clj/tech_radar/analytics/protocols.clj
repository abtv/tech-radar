(ns tech-radar.analytics.protocols)

(defprotocol Analyze
  (search [this topic text])
  (index-info [this])
  (trends [this])
  (texts [this topic])
  (statistic [this]))

(defprotocol Storage
  (init [this initial-data])
  (reset-trends [this hashtags-type hashtags]))

(defprotocol Tweet
  (add [this tweet]))
