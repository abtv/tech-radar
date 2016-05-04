(ns tech-radar.parser
  (:require [om.next :as om]))

(defmulti read om/dispatch)

(defmethod read :default [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmethod read :navbar-settings [{:keys [state]} key params]
  (let [st @state]
    {:value (:settings st)}))

(defmethod read :topicview-settings [{:keys [state]} key params]
  (let [st @state]
    {:value (:settings st)}))

(defmulti mutate om/dispatch)

(defmethod mutate 'records-per-page/set [{:keys [state] :as env} key params]
  (let [{:keys [records-per-page]} params]
    {:action (swap! state (fn [st]
                            (-> st
                                (assoc-in [:settings :records-per-page] records-per-page)
                                (assoc-in [:settings :page-number] 1))))}))

(defmethod mutate 'page-number/set [{:keys [state] :as env} key params]
  (let [{:keys [page-number]} params]
    {:action (swap! state assoc-in [:settings :page-number] page-number)}))

(defmethod mutate :default [{:keys [state] :as env} key params]
  (js/console.log "mutate not found")
  {:value :not-found})

(def parser (om/parser {:read   read
                        :mutate mutate}))
