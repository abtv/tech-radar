(ns tech-radar.services.search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan close! <! put!]]
            [tech-radar.services.web :refer [web]]
            [tech-radar.history :refer [navigate-to-url!]]))

(defn- set-search-results [state topic text {:keys [total texts]}]
  (navigate-to-url! (str "#" (name topic) "/search"))
  (swap! state (fn [state]
                 (-> state
                     (assoc-in [:topics topic] texts)
                     (assoc-in [:settings :search-text] text)
                     (assoc-in [:settings :search-topic] topic)
                     (assoc-in [:current-screen] :topic)
                     (assoc-in [:current-topic] topic)))))

(defn make-search [state topic text]
  (go
    (let [results (<! (web :search/get {:topic (name topic)
                                        :text  (str "#" text)}))]
      (set-search-results state topic text results))))

