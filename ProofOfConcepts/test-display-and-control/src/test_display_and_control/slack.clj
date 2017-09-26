(ns test-display-and-control.slack
  (:gen-class)
  (:require 
    [slack-api.core :as slack-api]
  )
  )

(defn start
  [config]
  (slack-api/start config)
)

