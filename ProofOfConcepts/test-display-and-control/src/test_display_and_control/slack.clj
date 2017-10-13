(ns test-display-and-control.slack
  (:gen-class)
  (:require 
    [slack-api.core :as slack-api]
  )
  )

(defn set-new-atom-value [atom newval] (
  swap! atom (fn [arg1 arg2] arg2) newval)
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
	(if (= (:announcement-channel config) nil)
	 (set-new-atom-value announcement-function (fn [annouancment-txt] nil)); no announcement channel - do nothing when we get announcements
	 (do
	   (def announcement-channel (filter (fn [x] (= (:announcement-channel config) (:name x))) (:channels slack-vars)))
	   (if (= announcement-channel ())
	     (do
	       (println (str"Warning announcement channel not found - " (:announcement-channel config)))
		   (set-new-atom-value announcement-function (fn [annouancment-txt] nil))
		 )
         (set-new-atom-value announcement-function (fn [annouancment-txt] (send-fn {:channel (:id (first announcement-channel)) :message-string annouancment-txt})))
	   )
	 )
	)
	(@announcement-function "Hello everyone!")
  )
  {:onclosehandler (partial @announcement-function "Goodbye all")}
)

