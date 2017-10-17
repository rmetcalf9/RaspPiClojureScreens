(ns slack-api.core
  (:gen-class)
  (:require 
    [clojure.core.async :as async :refer [go go-loop]]
    [cheshire.core :refer [parse-string generate-string]]
    [clj-http.client :as http]
    [gniazdo.core :as ws]
    [clojure.core.async :as async :refer [>! <! go go-loop]]
    [clojure.string :as str]
;	[lamina.core :as lamina] ;moved to manifold as lamina is depreciated
    [manifold.stream :as manifold] ;https://github.com/ztellman/manifold
  )
  (:import 
    java.io.StringWriter
  )
)

;if set to true will output more debug messages
(def verbose-output false)

(defn get-websocket-info [api-token]
  (let [response (-> (http/get "https://slack.com/api/rtm.start"
                               {:query-params {:token      api-token
                                               :no_unreads true}
                                :as :json})
                     :body)]
     ;(println response) ;use this line to dump the recieved data to help write code to extract relevant data
    (when (:ok response)
      {:botid (:id (:self response)) :botname (:name (:self response)) :url (:url response) :channels (:channels response)}
    )
  )
) ;defn get-websocket-into

(defn send-message-to-socket [idcounteratom socket orig-msg]
  (let [
      next-id (fn [] (swap! idcounteratom inc))
	  msg-with-id (assoc orig-msg :id (next-id))
    ]
	(ws/send-msg socket (generate-string msg-with-id))
  )
) ;send-message-to-socket

(defn worker-to-send-messages-from-queue [idcounteratom socket queue-of-messages-to-send]
  "Worker thread that will send pending messages"
  (loop [] 
    (do
      (let [
		msg @(manifold/take! queue-of-messages-to-send)
		slack-msg {
          :type "message"
          :channel (:channel msg)
          :text    (:message-string msg)
        }	  
      ]
		(send-message-to-socket idcounteratom socket slack-msg)
      )

	  (Thread/sleep 200) ;only send outgoing messages 5 times a second
	                    ; don't worry about one at a time - this will have
						; the effect of throttleing the output
      (recur)
	)
  )
)

(defn connect-socket [url queue-of-messages-to-send]
  (let [
        idcounteratom (atom 0)
        in (async/chan)
        out (async/chan)
        socket (ws/connect
                 url
                 :on-receive
                 (fn [m]
                   (async/put! in (parse-string m true)))
                 :on-error
                 (fn [_]
                   (async/close! in)))
       ]

    (do
	  (-> (Thread. (fn [] (worker-to-send-messages-from-queue idcounteratom socket queue-of-messages-to-send))) .start)
      (go-loop []
        (let [
	        orig-msg (async/<! out)
	      ]
	      (do
		    ;(println (str "Sending id " (:id msg-with-id)))
            (send-message-to-socket idcounteratom socket orig-msg)
            (recur)
		  )
	     )
	   )
	 )
    [in out])
) ;defn connect-socket

(defn remove-particular-start
  "Given a string remove a prefix if it is present. Return an indicator to show if it was removed as well as the resultant string"
  [input-string prefix-to-remove]
    (if (str/starts-with? (str/lower-case input-string) (str/lower-case (str prefix-to-remove " ")))
      [true (subs input-string (+ 1 (count prefix-to-remove)))]
      [false input-string]
    )
)

;enrich message recieved from slack with some common information
; work out if it is a message or presense indicator or something else;
;  if it is a message work out if it is for this bot
;  remove the prefix
;this eavoids doing this caculation in caller
(defn enrich-recieved-message
  "Given a message from slack, enrich it with extra info"
  [message-from-slack name-of-this-bot id-of-this-bot]
  (let [
      lcasemsgtext (if (str/blank? (:text message-from-slack)) "" (str/lower-case (:text message-from-slack)))
    ]
    (if (= (:type message-from-slack) "message") 

      ;This point of the code is reached if it is a message
      ;now use a let to try and remove all from the start.
      ;if it is not sucessfully removed try and remove this bot id
      ;otherwise we know it isn't a message for this bot

      (let [
           [is-all message-without-all-prefix] (remove-particular-start (:text message-from-slack) "all")
         ]
         (if is-all
          { ;slack message all
            :origmsg message-from-slack
  	        :id-of-this-bot id-of-this-bot
  	        :name-of-this-bot name-of-this-bot
    	      :is-message-for-my-attention true
    	      :is-message-broadcast true
	          :actual_text message-without-all-prefix
          }
          (let [
               [is-bot message-without-botid-prefix] (remove-particular-start (:text message-from-slack) (str "<@" id-of-this-bot ">"))
             ]
             (if is-bot
               { ;slack message for this bot
                 :origmsg message-from-slack
  	             :id-of-this-bot id-of-this-bot
  	             :name-of-this-bot name-of-this-bot
                 :is-message-for-my-attention true
    	           :is-message-broadcast false
	               :actual_text message-without-botid-prefix
               }
               { ;slack message not for this bot, and not for all
                 :origmsg message-from-slack
  	             :id-of-this-bot id-of-this-bot
  	             :name-of-this-bot name-of-this-bot
                 :is-message-for-my-attention false
    	           :is-message-broadcast false
	               :actual_text (:text message-from-slack)
               }
             )
          )
         )
      )

      ;slack message type NOT ismessage so we 
      { 
        :origmsg message-from-slack
  	    :id-of-this-bot id-of-this-bot
  	    :name-of-this-bot name-of-this-bot
	      :is-message-for-my-attention false
 	      :is-message-broadcast false
        :actual_text "" ;not a message so no actual text
      }

    )
   ) ;let
)

(defn get-comm-channel 
  "Given an api-token return a com channel with input and output functions [cin cout stop]"
  [api-token queue-of-messages-to-send set-recieved-slack-vars]
  (let [cin (async/chan 10)
        cout (async/chan 10)
        {:keys [botid botname url channels]} (get-websocket-info api-token)
        shutdown (fn []
                   (async/close! cin)
                   (async/close! cout))
        mk-timeout #(async/timeout 15000)]
    (when (clojure.string/blank? url) (do
      (println "slack error - nil RTM Wewbsocket URL")
      (throw (ex-info "Could not get RTM Websocket URL" {}))
    ))

    (if verbose-output 
	  (println ":: got websocket url:" url)
	)
    (println ":: Name of this bot is:" botname)
	
	(set-recieved-slack-vars {
	    :botname botname
		:channels channels
	  }
	)

    ;; start a loop to process messages
    (go-loop [[in out] (connect-socket url queue-of-messages-to-send)
              ping-count 0
              to (mk-timeout)]
      ;; get whatever needs to be done for either data coming from the socket
      ;; or from the user
      (let [[v p] (async/alts! [cout in to])]
        ;; if something goes wrong, just die for now
        ;; we should do something smarter, may be try and reconnect
        (if (= p to)
          ;; time to ping
          (do
            (if verbose-output 
			  (println ":: ping? pending pings:" ping-count)
			)
            (async/>! out {
                           :type "ping"
                           :ts   (System/currentTimeMillis)})
            (if (> ping-count 5)
              (recur (connect-socket url) 0 (mk-timeout))
              (recur [in out] (inc ping-count) (mk-timeout)))
		   ) ;end of ping code
           (if (nil? v)
              (do
                (println "A channel returned nil, may be its dead? Leaving loop.")
                (shutdown)
			  )
			  (do
                (if (= p cout)
                  ;; the worker sent us something, time to send it to the remote end point
				  ;; after RJM re-write the worker no longer sends messages this way. Instead
				  ;; they are sent to the queue and processed by the worker started from connect-socket
                  (async/>! out {
				               :type "message"
                               :channel (get-in v [:meta :channel])
                               :text    "Not Used Code" ;TODO Replace this section with code to send outgoing messages to slack
							   }
                  )

                  ;; the websocket has sent us something
                  (do
                    (if (= (:type v) "pong")
                      (if verbose-output 
			            (println ":: pong! latency: " (- (System/currentTimeMillis) (:ts v)) "ms.")
					    (print "") ;error if i don't print something - I don't know why
					  )
                      (async/>! cin (enrich-recieved-message v botname botid))
                    )
                  )
				)
              (recur [in out]
                     (if (= (:type v) "pong")
                       (dec ping-count) ping-count)
                     (mk-timeout)
		      )
			)
		  )
		)
     )
   ) ;go-loop
    

   [cin cout shutdown] ;return values
  ) ;let
) ;defn get-comm-channel

(defn send-message-to-channel
  "Function to send a message to a particular channel"
  [queue-of-messages-to-send {:keys [channel message-string]}]
  (manifold/put! queue-of-messages-to-send {:channel channel :message-string message-string})
)
(defn message-reply-function
  "Function to reply to a message"
  [queue-of-messages-to-send msg reply-message-string]
  (send-message-to-channel queue-of-messages-to-send {:channel (:channel (:origmsg msg)) :message-string reply-message-string})
)

;worker
(defn worker [{:keys [api-token]} recieved-message-function queue-of-messages-to-send] (do
  (println "slack worker started")
  
  (def recieved-slack-vars (atom {}))
  (defn set-recieved-slack-vars [newval] (
    reset! recieved-slack-vars newval)
  )
 ; (set-recieved-slack-vars rec-slack-vars)
 
  
  (let [
         inst-comm (fn[] (get-comm-channel api-token queue-of-messages-to-send set-recieved-slack-vars))
     ]
    (go-loop [[in out stop] (inst-comm)]
	  (do
        (if-let [form (<! in)] 
	     (do
		  (recieved-message-function form (partial message-reply-function queue-of-messages-to-send form))
          (recur [in out stop])
		 )
		 ;worker is no longer sending messages back using the out channel
		 ; if we did want to we would use code like this:
         ;        (let [input (:input form)
		 ;		       res (eval-expr input)
		 ;          (println ":: form >> " input)
		 ;          (>! out (assoc form :evaluator/result "ABC"))
		 ;          (recur [in out stop])
		 ;		) ;let
		
        ;; something wrong happened, re init
        (do
          (println ":: WARNING! The comms went down, going to restart.")
          (stop)
          (<! (async/timeout 3000))
          (recur (inst-comm))
		 )
       ) ;if-let
	 )
    ) ;go-loop
	;(.join (Thread/currentThread))
  )
  
  ;wait until we have recieved data from slack
  (loop [i 0]  
	(when (= @recieved-slack-vars {})
      (Thread/sleep 100)
      (recur (inc i)); loop i will take this value
  ))

  (println "slack worker setup complete")
  {:slack-vars @recieved-slack-vars}
))


(defn start
  [config recieved-message-function]
  
  (do
    (def queue-of-messages-to-send (manifold/stream))
   
    (def worker-return-value (worker config recieved-message-function queue-of-messages-to-send))
  )

  ;return a function to the caller to allow them to send messages to slack
  [(partial send-message-to-channel queue-of-messages-to-send) (:slack-vars worker-return-value)]
)

