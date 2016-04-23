(ns tech-radar.state)

(defonce app-state (atom {:topic-items      {:jobs       {:href "#/topic/jobs"
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
                          :records-per-page 10
                          :current-screen   :trends
                          :current-topic    nil
                          :trends           {}
                          :topics           {}}))
