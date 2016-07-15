(ns tech-radar.services.search
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan close! <! put!]]
            [tech-radar.services.web :refer [web]]
            [tech-radar.history :refer [navigate-to-url!]]
            [clojure.string :as s]
            [tech-radar.history :as history]))

(defn navigate-to-search [topic search-text]
  (history/navigate-to-url! (str "/topic/" (name topic) "/search?text=" (js/encodeURI search-text))))

(defn- set-search-results [state topic text {:keys [total texts]}]
  (swap! state (fn [state]
                 (-> state
                     (assoc-in [:topics topic] texts)
                     (assoc-in [:settings :search-text] text)
                     (assoc-in [:settings :page-number] 1)
                     (assoc-in [:current-screen] :topic)
                     (assoc-in [:current-topic] topic)))))

(defn make-search [state topic text]
  (when (and topic (not (s/blank? text)))
    (go
      (let [results (<! (web :search/get {:topic (name topic)
                                          :text  text}))]
        (set-search-results state topic text results)))))
