(ns tech-radar.utils.resources
  (:require [taoensso.timbre :as timbre]
            [liberator.core :refer [resource]]
            [cognitect.transit :as transit])
  (:import (org.joda.time DateTime ReadableInstant)
           (java.io ByteArrayOutputStream ByteArrayInputStream)))

(def joda-time-writer
  (transit/write-handler
    (constantly "m")
    (fn [v] (-> ^ReadableInstant v .getMillis))
    (fn [v] (-> ^ReadableInstant v .getMillis .toString))))

(defn ^String write-transit [x]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos :json {:handlers {DateTime joda-time-writer}})
        _    (transit/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))

(defn read-transit [^String s]
  (let [bais   (ByteArrayInputStream. (.getBytes s))
        reader (transit/reader bais :json)
        ret    (transit/read reader)]
    (.reset bais)
    ret))

(defn as-response [data ctx]
  {:body (write-transit data)})

(defn resource-handler [defaults & kvs]
  (apply resource defaults kvs))

(def available-media-types ["application/transit+json"])

(defn- handle-exception [{:keys [exception] :as ctx}]
  (timbre/error exception "exception in handler"))

(def get-resource
  {:allowed-methods       [:get]
   :available-media-types available-media-types
   :as-response           as-response
   :handle-exception      handle-exception})

(def post-resource
  {:allowed-methods       [:post]
   :available-media-types available-media-types
   :as-response           as-response
   :handle-exception      handle-exception})
