(ns tech-radar.parser
  (:require [om.next :as om]))

(defn read [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      (do
        (js/console.log "not found")
        (js/console.log (clj->js key))
        {:value :not-found}))))

(defmulti mutate om/dispatch)

(defmethod mutate 'records-per-page/set [{:keys [state] :as env} key params]
  (let [{:keys [records-per-page]} params]
    {:action (swap! state assoc-in [:settings :records-per-page] records-per-page)}))

(defmethod mutate 'page-number/set [{:keys [state] :as env} key params]
  (let [{:keys [page-number]} params]
    {:action (swap! state assoc-in [:settings :page-number] page-number)}))

(defmethod mutate :default [{:keys [state] :as env} key params]
  (js/console.log "mutate not found")
  {:value :not-found})

(def parser (om/parser {:read   read
                        :mutate mutate}))
