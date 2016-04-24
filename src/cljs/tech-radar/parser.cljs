(ns tech-radar.parser
  (:require [om.next :as om]))

(defn read [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ v] (find st key)]
      {:value v}
      {:value :not-found})))

(defmulti mutate om/dispatch)

(defmethod mutate 'records-per-page/set [{:keys [state] :as env} key params]
  (js/console.log "records-per-page/set")
  (js/console.log (clj->js params))
  (let [{:keys [records-per-page]} params]
    {:value  {:keys [:records-per-page]}
     :action (swap! state assoc-in [:records-per-page] records-per-page)}))

(defmethod mutate :default [{:keys [state] :as env} key params]
  (js/console.log "mutate not found")
  {:value :not-found})

(def parser (om/parser {:read   read
                        :mutate mutate}))
