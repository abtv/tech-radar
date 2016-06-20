(ns tech-radar.parser
  (:require [om.next :as om]))

;;; --------------------------------------------------------------------------
;;; Read functions
(defmulti read-fn om/dispatch)

(defmethod read-fn :default
  [{:keys [parser ast query state] :as env} k _]
  (let [[_ v] (find @state k)
        value (condp = (:type ast)
                :join (parser (assoc env :state (atom v)) query)
                :prop v
                :not-found)]
    {:value value}))

;; TODO: refactor & remove
(defmethod read-fn :state [{:keys [state]} _ _]
  {:value @state})

;;; --------------------------------------------------------------------------
;;; Mutations
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

(defmethod mutate 'trend-type/set [{:keys [state] :as env} key params]
  (let [{:keys [trend-type]} params]
    {:action (swap! state assoc-in [:trend-type] trend-type)}))

(defmethod mutate 'current-trend/set [{:keys [state] :as env} key params]
  (let [{:keys [current-trend]} params]
    {:action (swap! state assoc-in [:current-trend] current-trend)}))

(defmethod mutate 'search-text/set [{:keys [state] :as env} key params]
  (let [{:keys [search-text]} params]
    {:action (swap! state assoc-in [:settings :search-text] search-text)}))

(defmethod mutate :default [{:keys [state] :as env} key params]
  (js/console.log "mutate not found")
  {:value :not-found})

(def parser (om/parser {:read   read-fn
                        :mutate mutate}))
