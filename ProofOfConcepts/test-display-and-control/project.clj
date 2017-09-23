(defproject test-display-and-control "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"][seesaw "1.4.5"][clj-time "0.14.0"]]
  :main ^:skip-aot test-display-and-control.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
