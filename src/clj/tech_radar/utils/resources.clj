(ns tech-radar.utils.resources
  (:require [taoensso.timbre :as timbre]
            [liberator.core :refer [resource]]
            [cognitect.transit :as transit])
  (:import (org.joda.time DateTime ReadableInstant)
           (java.io ByteArrayOutputStream)))

(def available-media-types ["application/transit+json"])

(defn- handle-exception [{:keys [exception] :as ctx}]
  (timbre/error exception "exception in handler"))

(def joda-time-writer
  (transit/write-handler
    (constantly "m")
    (fn [v] (-> ^ReadableInstant v .getMillis))
    (fn [v] (-> ^ReadableInstant v .getMillis .toString))))

(defn- write [x]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos :json {:handlers {DateTime joda-time-writer}})
        _    (transit/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))

(defn- as-response [data ctx]
  {:body (write data)})

(defn resource-handler [defaults & kvs]
  (apply resource defaults kvs))

(def entry-params
  {:allowed-methods       [:get]
   :available-media-types available-media-types
   :as-response           as-response
   :handle-exception      handle-exception})
