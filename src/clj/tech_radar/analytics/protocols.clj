(ns tech-radar.analytics.protocols)

(defprotocol Analyze
  (search [this topic text])
  (trends [this])
  (texts [this topic]))

(defprotocol Storage
  (init [this initial-data])
  (reset-trends [this hashtags-type hashtags]))

(defprotocol Tweet
  (add [this tweet]))
