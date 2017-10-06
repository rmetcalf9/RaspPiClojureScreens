(ns test-display-and-control.slack
  (:gen-class)
  (:require 
    [slack-api.core :as slack-api]
  )
  )

(defn recieved-message [msg] (do
  (println "Rec msg")
  (println msg)
))

(defn start
  [config]
  (slack-api/start config recieved-message)
)

