(ns tech-radar.history
  (:require goog.events
            [goog.string :as gstring]
            [secretary.core :as secretary]
            [goog.history.EventType :as EventType]
            [goog.events])
  (:import goog.History))

(declare nav-history)

(defn init-history []
  (when-not nav-history
    (secretary/set-config! :prefix "#")
    (def nav-history (History.))
    (doto nav-history
      (goog.events/listen EventType/NAVIGATE
                          (fn [e]
                            (let [token (.-token e)]
                              (secretary/dispatch! token)
                              (js/window.scrollTo 0 0))))
      (.setEnabled true))))

(defn navigate-to-url! [url-token]
  (let [url-token (if (gstring/startsWith url-token "#")
                    (.substring url-token 1)
                    url-token)]
    (.setToken nav-history url-token)))

