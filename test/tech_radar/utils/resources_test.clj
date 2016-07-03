(ns tech-radar.utils.resources-test
  (:require [clojure.test :refer :all]
            [tech-radar.utils.resources :as resources]
            [clj-time.core :as time]))

(deftest encoding-test
  (let [data           {:id       1
                        :time     (.toDate (time/now))
                        :eng-text "english text"
                        :rus-text "русский текст"}
        transit-data   (resources/write-transit data)
        converted-data (resources/read-transit transit-data)]
    (is (= data converted-data))))
