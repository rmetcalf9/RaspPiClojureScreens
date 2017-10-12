(ns test-display-and-control.slack
  (:gen-class)
  (:require 
    [slack-api.core :as slack-api]
  )
  )

(defn recieved-message [msg replyfn] (do
  (if (:is-message-for-my-attention msg) (do
    (println "Rec msg for my attention")
    (println msg)
	(replyfn msg (str "This is a test reply to msg: " (:actual_text msg)))
  ))
))

(defn start
  [config]
  (slack-api/start config recieved-message)
)

