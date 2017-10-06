(ns slack-api.core
  (:gen-class)
  (:require 
    [clojure.core.async :as async :refer [go go-loop]]
    [cheshire.core :refer [parse-string generate-string]]
    [clj-http.client :as http]
    [gniazdo.core :as ws]
    [clojure.core.async :as async :refer [>! <! go go-loop]]
	[clojure.string :as str]
  )
  (:import 
    java.io.StringWriter
  )
)

(def verbose-output false)

;*****************
;Tmp code added to try and debug No implementation of method: :take! of protocol error
;*****************

(defn format-result-for-slack [result]
  (str "<@" (get-in result [:meta :user]) ">:"
       (let [r (:evaluator/result result)]
         (if (:status r)
           (str "```"
                "=> " (:form r) "\n"
                (when-let [o (:output r)]
                  o)
                (if (nil? (:result r))
                  "nil"
                  (:result r))
                "```")
           (str "```"
                "==> " (or (:form r) (:input r)) "\n"
                (or (:result r) "Unknown Error")
                "```")))))

;*****************
;End ot tmp code
;*****************

(defn get-websocket-info [api-token]
  (let [response (-> (http/get "https://slack.com/api/rtm.start"
                               {:query-params {:token      api-token
                                               :no_unreads true}
                                :as :json})
                     :body)]
;     (println response) ;use this line to dump the recieved data to help write code to extract relevant data
    (when (:ok response)
      {:botid (:id (:self response)) :botname (:name (:self response)) :url (:url response)}
    )
  )
) ;defn get-websocket-into

(defn connect-socket [url]
  (let [in (async/chan)
        out (async/chan)
        socket (ws/connect
                 url
                 :on-receive
                 (fn [m]
                   (async/put! in (parse-string m true)))
                 :on-error
                 (fn [_]
                   (async/close! in)))]
    (go-loop []
      (let [m (async/<! out)
            s (generate-string m)]
        (ws/send-msg socket s)
        (recur)))
    [in out])
) ;defn connect-socket

;I want my slack API to pre-caculate some date for me
(defn enrich-recieved-message
  "Given a message from slack, enrich it with extra info"
  [message-from-slack name-of-this-bot id-of-this-bot]
  (let [
      lcasemsgtext (if (str/blank? (:text message-from-slack)) "" (str/lower-case (:text message-from-slack)))
    ]
    {
      :origmsg message-from-slack
  	  :id-of-this-bot id-of-this-bot
  	  :name-of-this-bot name-of-this-bot
	  :is-message-for-my-attention (if (= (:type message-from-slack) "message") 
	    (if (str/starts-with? lcasemsgtext "all") 
	      true  ;starts with all then needs attention
		  (if (str/starts-with? (:text message-from-slack) (str "<@" id-of-this-bot ">")) ;TODO need to search for <@U7F92UZ4N> in :text
		    true
			false ;not all, dosen't start with @ my name
		  )
  	    )
	    false ;type is not message - so not a message to this bot
	  )
	  :actual_text "TODO - remove all or <@X> from start of message"
    }
   ) ;let
)

(defn get-comm-channel 
  "Given an api-token return a com channel with input and output functions [cin cout stop]"
  [api-token]
  (let [cin (async/chan 10)
        cout (async/chan 10)
        {:keys [botid botname url]} (get-websocket-info api-token)
        counter (atom 0)
        next-id (fn []
                  (swap! counter inc))
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

    ;; start a loop to process messages
    (go-loop [[in out] (connect-socket url)
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
            (async/>! out {:id   (next-id)
                           :type "ping"
                           :ts   (System/currentTimeMillis)})
            (if (> ping-count 5)
              (recur (connect-socket url) 0 (mk-timeout))
              (recur [in out] (inc ping-count) (mk-timeout))))
          (if (nil? v)
            (do
              (println "A channel returned nil, may be its dead? Leaving loop.")
              (shutdown)
			)
			(do
              (if (= p cout)
                ;; the user sent us something, time to send it to the remote end point
                (async/>! out {:id      (next-id) :type "message"
                               :channel (get-in v [:meta :channel])
                               :text    (-> v
                                            format-result-for-slack) ;TODO Replace this section with code to send outgoing messages to slack
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
                ))
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


;worker
(defn worker [{:keys [api-token]} recieved-message-function] (do
  (println "slack worker started")
  (let [
         inst-comm (fn[] (get-comm-channel api-token))
     ]
    (go-loop [[in out stop] (inst-comm)]
      (if-let [form (<! in)] 
	     (do
		  (recieved-message-function form)
          (recur [in out stop])
		 )
;        (let [input (:input form)
;		       res (eval-expr input)
;              ]
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
    ) ;go-loop
	;(.join (Thread/currentThread))
  )


  (println "slack worker ended")
))

(defn start
  [config recieved-message-function]
  ;(future (worker config))
  
  (do
    ;TODO Workout if future is needed or do the async queues handle this?
    (worker config recieved-message-function)

  )
)

