(defproject dolphin "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot dolphin.core
  :target-path "target/%s"
  :profiles {
    :uberjar {:aot :all}
    :dev {:plugins [[com.jakemccrary/lein-test-refresh "0.21.1"]]}
  }
)
