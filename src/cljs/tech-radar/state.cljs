(ns tech-radar.state)

(def app-state
  (atom
    {:settings       {:menu-items       {:trends     {:href "#/trends"
                                                      :name "Trends"}
                                         :jobs       {:href "#/topic/jobs"
                                                      :name "Jobs"}
                                         :clojure    {:href "#/topic/clojure"
                                                      :name "Clojure"}
                                         :jvm        {:href "#/topic/jvm"
                                                      :name "JVM"}
                                         :javascript {:href "#/topic/javascript"
                                                      :name "JavaScript"}
                                         :golang     {:href "#/topic/golang"
                                                      :name "Golang"}
                                         :linux      {:href "#/topic/linux"
                                                      :name "Linux"}
                                         :nosql      {:href "#/topic/nosql"
                                                      :name "NoSQL"}}
                      :topic-items      #{:jobs
                                          :clojure
                                          :jvm
                                          :javascript
                                          :golang
                                          :linux
                                          :nosql}
                      :records-per-page 15
                      :page-number      1}
     :current-screen :trends
     :current-topic  nil
     :trends         {}
     :topics         {}}))
