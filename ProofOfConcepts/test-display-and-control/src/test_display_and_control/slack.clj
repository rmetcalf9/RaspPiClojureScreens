(ns test-display-and-control.slack
  (:gen-class)
  (:require 
    [slack-api.core :as slack-api]
  )
  )

(defn return-second-arg [arg1 arg2] (identity arg2))
(defn set-new-atom-value [atom newval] (
  swap! atom return-second-arg newval)
)
(def announcement-function (atom (fn [annouancment-txt] (println "Error!"))))
  
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
	
	(@announcement-function "Hello")
	
	(if (= (:announcement-channel config) nil)
	 (set-new-atom-value announcement-function (fn [annouancment-txt] nil)); no announcement channel - do nothing when we get announcements
	 (do
	   (def announcement-channel (filter (fn [x] (= (:announcement-channel config) (:name x))) (:channels slack-vars)))
	   (if (= announcement-channel ())
	     (do
	       (println (str"Warning announcement channel not found - " (:announcement-channel config)))
		   (set-new-atom-value announcement-function (fn [annouancment-txt] nil))
		 )
	     (do
		   (println (:id (first announcement-channel)))
		   (set-new-atom-value announcement-function (fn [annouancment-txt] (send-fn {:channel (:id (first announcement-channel)) :message-string annouancment-txt})))
		 )
	   )
	 )
	)
	(@announcement-function "Hello everyone!")
  )
  
)

