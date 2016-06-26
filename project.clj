(defproject tech-radar "1.0.0"
  :description "tech-radar (technology radar project)"
  :url "https://github.com/abtv/tech-radar"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.stuartsierra/component "0.3.1"]
                 [reloaded.repl "0.2.2"]
                 [org.clojure/core.async "0.2.374"]
                 [org.immutant/immutant "2.1.2"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.2.0"]
                 [ring/ring-json "0.4.0"]
                 [ring-transit "0.1.4"]
                 [compojure "1.5.0"]
                 [liberator "0.14.1"]
                 [bidi "2.0.4"]
                 [com.cognitect/transit-clj "0.8.285"]
                 [cheshire "5.5.0"]
                 [ragtime "0.5.3"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [honeysql "0.6.3"]
                 [clj-dbcp "0.8.1"]
                 [clj-time "0.11.0"]
                 [environ "1.0.2"]
                 [com.taoensso/timbre "4.3.1"]
                 [camel-snake-kebab "0.3.2"]
                 [com.twitter/hbc-core "2.2.0"]
                 [org.omcljs/om "1.0.0-alpha37"]
                 [sablono "0.6.2"]
                 [cljs-ajax "0.5.4"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [secretary "1.2.3"]
                 [cljsjs/dimple "2.1.2-0"]
                 [com.cemerick/url "0.1.1"]]

  :min-lein-version "2.5.0"

  :uberjar-name "backend.jar"

  :source-paths ["src/cljc" "src/clj" "src/cljs"]

  :plugins [[lein-environ "1.0.2"]
            [ragtime/ragtime.lein "0.3.9"]
            [lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.0-6"]]

  :aliases {"migrate"  ["run" "-m" "tech-radar.migrations/migrate"]
            "rollback" ["run" "-m" "tech-radar.migrations/rollback"]}

  :cljsbuild {:builds {:dev     {:source-paths ["src/cljs"]
                                 :figwheel     {:websocket-host "localhost"}
                                 :compiler     {:main       tech-radar.core
                                                :asset-path "js"
                                                :output-dir "resources/public/js"}}
                       :release {:source-paths ["src/cljs"]
                                 :compiler     {:optimizations :advanced
                                                :output-to     "frontend-release/main.js"}}}}

  :profiles {:dev     {:env {:host                    "localhost"
                             :port                    "3000"
                             :database                "jdbc:postgresql://localhost/tech_radar?user=postgres&password=postgres"
                             :twitter-security        "twitter-security.edn"
                             :twitter-settings        "twitter-settings.edn"
                             :classify-settings       "classify-settings.edn"
                             :hashtag-filter-settings "hashtag-filter-settings.edn"
                             :cache-update-timeout-s  "10"
                             :max-hashtags-per-trend  "25"
                             :max-texts-per-request   "200"
                             :max-tweet-count         "500000"
                             :metrics-timeout-s       "100"
                             :log-path                "./logs/tech-radar.log"
                             :max-log-size-mb         "1"
                             :backlog                 "2"}}
             :uberjar {:main tech-radar.core
                       :aot  [tech-radar.core]
                       :env  {:host                    "0.0.0.0"
                              :port                    "3000"
                              :database                "jdbc:postgresql://localhost/tech_radar?user=postgres&password=postgres"
                              :twitter-security        "twitter-security.edn"
                              :twitter-settings        "twitter-settings.edn"
                              :classify-settings       "classify-settings.edn"
                              :hashtag-filter-settings "hashtag-filter-settings.edn"
                              :cache-update-timeout-s  "30"
                              :max-hashtags-per-trend  "25"
                              :max-texts-per-request   "200"
                              :max-tweet-count         "500000"
                              :metrics-timeout-s       "300"
                              :log-path                "./logs/tech-radar.log"
                              :max-log-size-mb         "1"
                              :backlog                 "10"}}})
