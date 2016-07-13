(ns tech-radar.utils.convert)

(def trend-types {:daily   "Daily"
                  :weekly  "Weekly"
                  :monthly "Monthly"})

(def topics {:jobs       "Jobs"
             :clojure    "Clojure"
             :jvm        "JVM"
             :javascript "JavaScript"
             :golang     "Golang"
             :linux      "Linux"
             :nosql      "NoSQL"})

(defn- trend-type->trend-type-name [type]
  (get trend-types type))

(defn- trend-type-name->trend-type [name]
  (->> trend-types
       (filter (fn [[trend-tag trend-name]]
                 (= name trend-name)))
       (first)
       (first)))

(defn- trend-item->trend-name [topic]
  (get topics topic))

(defn- trend-name->trend-item [name]
  (->> topics
       (filter (fn [[topic-tag topic-name]]
                 (= topic-name name)))
       (first)
       (first)))
