(defproject tech-radar "1.0.0"
  :description "Tech-radar backend"
  :url "https://github.com/abtv/tech-radar"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/core.async "0.2.374"]
                 [org.immutant/immutant "2.1.2"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.2.0"]
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
                 [com.twitter/hbc-core "2.2.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [reloaded.repl "0.2.1"]
                 [environ "1.0.2"]
                 [com.taoensso/timbre "4.3.1"]
                 [environ "1.0.2"]
                 [com.taoensso/timbre "4.3.1"]
                 [camel-snake-kebab "0.3.2"]
                 [org.omcljs/om "1.0.0-alpha31"]
                 [sablono "0.6.2"]
                 [cljs-ajax "0.5.4"]
                 [figwheel-sidecar "0.5.0-6"]
                 [com.cemerick/piggieback "0.2.1"]
                 [secretary "1.2.3"]
                 [cljsjs/dimple "2.1.2-0"]
                 [com.cognitect/transit-clj "0.8.285"]]


  :source-paths ["src/cljc" "src/clj" "src/cljs"]

  :plugins [[lein-environ "1.0.2"]
            [ragtime/ragtime.lein "0.3.9"]
            [lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.0-6"]]

  :aliases {"migrate"  ["run" "-m" "tech-radar.migrations/migrate"]
            "rollback" ["run" "-m" "tech-radar.migrations/rollback"]}

  :min-lein-version "2.5.0"

  :uberjar-name "backend.jar"

  :clean-targets ^{:protect false} ["resources/public/js/" "target/"]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]
                 :init             (do (use 'figwheel-sidecar.repl-api) (start-figwheel!))}
  :cljsbuild {:builds {:dev     {:source-paths ["src/cljs"]
                                 :figwheel     {:websocket-host "localhost"}
                                 :compiler     {:main       tech-radar.core
                                                :asset-path "js"
                                                :output-dir "resources/public/js"}}
                       :release {:source-paths ["src/cljs"]
                                 :compiler     {:optimizations :advanced
                                                :output-to     "release/main.js"}}}}

  :profiles {:dev     {:env {:host              "localhost"
                             :port              3000
                             :database          "jdbc:postgresql://localhost/analytics?user=postgres&password=postgres"
                             :twitter-security  "twitter-security.edn"
                             :twitter-settings  "twitter-settings.edn"
                             :classify-settings "classify-settings.edn"}}
             :uberjar {:main        tech-radar.core
                       :aot         [tech-radar.core]
                       :global-vars {;*warn-on-reflection* true
                                     *assert* false}
                       :env         {:host              "0.0.0.0"
                                     :port              3000
                                     :database          "jdbc:postgresql://localhost/analytics?user=postgres&password=postgres"
                                     :twitter-security  "twitter-security.edn"
                                     :twitter-settings  "twitter-settings.edn"
                                     :classify-settings "classify-settings.edn"}}})
