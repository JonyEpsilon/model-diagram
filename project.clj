(defproject model-diagram "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [hiccup "1.0.5"]
                 [hiccups "0.3.0"]
                 [thi.ng/geom "0.0.881"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [org.omcljs/om "0.9.0"]]
  :plugins [[lein-gorilla "0.3.4"]
            [lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.7"]
            [jonase/eastwood "0.2.1"]
            [lein-kibit "0.1.2"]
            [lein-cljfmt "0.3.0"]]
  :cljsbuild {
              :builds [{:id           "model-diagram"
                        :source-paths ["src/"]
                        :figwheel     true
                        :compiler     {:main       "model-diagram.core"
                                       :asset-path "js/out"
                                       :output-to  "resources/public/js/diagram.js"
                                       :output-dir "resources/public/js/out"}}]})
