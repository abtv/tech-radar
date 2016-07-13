(ns tech-radar.services.loader
  (:require
    [cheshire.core :refer [decode]]
    [camel-snake-kebab.core :refer [->kebab-case-keyword
                                    ->snake_case]]
    [camel-snake-kebab.extras :refer [transform-keys]]
    [clojure.core.async :refer [thread chan >!! <!! put!]]
    [clojure.string]
    [taoensso.timbre :as timbre]
    [tech-radar.components.counter :refer [increment]])
  (:import (com.twitter.hbc.core.endpoint StatusesFilterEndpoint)
           (com.twitter.hbc.httpclient.auth OAuth1)
           (com.twitter.hbc ClientBuilder)
           (java.util.concurrent LinkedBlockingQueue)
           (com.twitter.hbc.core Constants Constants$FilterLevel)
           (com.twitter.hbc.core.processor StringDelimitedProcessor)
           (com.twitter.hbc.httpclient BasicClient)))

(defn- new-endpoint [terms]
  (let [end-point (StatusesFilterEndpoint.)
        terms*    (java.util.ArrayList.)]
    (doseq [term terms]
      (.add terms* term))
    (.trackTerms end-point terms*)
    (.filterLevel end-point Constants$FilterLevel/None)
    end-point))

(defn- ^BasicClient new-client [{:keys [queue end-point auth]}]
  (let [cb (doto (ClientBuilder.)
             (.hosts Constants/STREAM_HOST)
             (.endpoint end-point)
             (.processor (StringDelimitedProcessor. queue))
             (.authentication auth))]
    (.build cb)))

(defn- decode-tweet [msg]
  (try
    (decode msg ->kebab-case-keyword)
    (catch Exception ex
      (timbre/error ex (str "decode tweet error: " msg))
      nil)))

(defn- run-loader-worker [{:keys [track
                                  tweet-chan should-work
                                  app-key app-secret user-token user-token-secret
                                  metrics]}]
  (thread
    (timbre/info "twitter loader started")
    (try
      (let [queue     (LinkedBlockingQueue. 1000)
            end-point (new-endpoint track)
            auth      (OAuth1. app-key app-secret user-token user-token-secret)
            client    (new-client {:queue     queue
                                   :end-point end-point
                                   :auth      auth})]
        (.connect client)

        (while @should-work
          (let [msg   (.take queue)
                tweet (decode-tweet msg)]
            (increment metrics :tweet-chan)
            (>!! tweet-chan tweet)))

        (.stop client))
      (catch Exception ex
        (timbre/error ex "twitter-loader exception")))
    (timbre/info "twitter loader finished")))

(defn run [params]
  (let [should-work (atom true)
        params*     (assoc params :should-work should-work)
        worker      (run-loader-worker params*)]
    (fn []
      (reset! should-work false)
      (<!! worker))))

