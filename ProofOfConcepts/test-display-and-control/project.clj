(defproject test-display-and-control "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [seesaw "1.4.5"]
    [clj-time "0.14.0"]
    [environ "1.0.2"]
    [org.clojure/core.async "0.1.346.0-17112a-alpha"]
    [clj-http "2.0.1"]
    [cheshire "5.5.0"]
  ]
  :main ^:skip-aot test-display-and-control.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
