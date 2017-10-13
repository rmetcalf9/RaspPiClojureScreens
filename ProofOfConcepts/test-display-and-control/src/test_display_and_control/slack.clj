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
  (let [[send-fn slack-vars] (slack-api/start config recieved-message)]
    ;TODO we need to derive the channel we need to send the message to
	
	(println slack-vars)
	
    (send-fn {:channel "a" :message-string "Hello everyone!"})
  )
  
)

