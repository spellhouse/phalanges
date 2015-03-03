(defproject spellhouse/phalanges "0.1.6"
  :description "ClojureScript library providing utilities for working with JavaScript KeyboardEvents."
  :url "https://github.com/spellhouse/phalanges"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies
  [[org.clojure/clojure "1.5.1"]]

  :profiles
  {:dev {:source-paths ["src" "dev"]
         :dependencies [[weasel "0.2.0"]
                        [com.cemerick/piggieback "0.1.3"]
                        [figwheel "0.1.3-SNAPSHOT"]
                        [org.clojure/clojurescript "0.0-2227" :scope "provided"]]
         :plugins [[com.cemerick/austin "0.1.3"]
                   [lein-figwheel "0.1.3-SNAPSHOT"]
                   [lein-cljsbuild "1.0.3"]]
         :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}
   :build {:source-paths ["src"]}}

  :cljsbuild
  {:builds [{:id "dev"
             :source-paths ["src" "dev/phalanges"]
             :compiler {:output-to "resources/public/js/phalanges.js"
                        :output-dir "resources/public/js/out"
                        :source-map "resources/public/js/phalanges.js.map"
                        :optimizations :none}}]})
