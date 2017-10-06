(ns slack-api.core
  (:gen-class)
  (:require 
    [clojure.core.async :as async :refer [go go-loop]]
    [cheshire.core :refer [parse-string generate-string]]
    [clj-http.client :as http]
    [gniazdo.core :as ws]
    [clojure.core.async :as async :refer [>! <! go go-loop]]
  )
  (:import 
    java.io.StringWriter
  )
)

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

(def sb (fn[] "ABC"))
(defn eval-expr
  "XXXXEvaluate the given string"
  [s]
  (try
    (with-open [out (StringWriter.)]
      (let [form (binding [*read-eval* false] (read-string s))
            result (sb form {#'*out* out})]
        {:status true
         :input s
         :form form
         :result result
         :output (.toString out)}))
    (catch Exception e
      {:status false
       :input s
       :result (.getMessage e)})))

;*****************
;End ot tmp code
;*****************

(defn get-websocket-info [api-token]
  (let [response (-> (http/get "https://slack.com/api/rtm.start"
                               {:query-params {:token      api-token
                                               :no_unreads true}
                                :as :json})
                     :body)]
;     (println "Got response for rtm start")
;     (println (:ok response))
;     (println (:url response))
    (when (:ok response)
      {:botname (:name (:self response)) :url (:url response)}
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

(defn get-comm-channel 
  "Given an api-token return a com channel with input and output functions [cin cout stop]"
  [api-token]
  (let [cin (async/chan 10)
        cout (async/chan 10)
        {:keys [botname url]} (get-websocket-info api-token)
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

    (println ":: got websocket url:" url)
    (println ":: Name of this bot is:" botname)

    ;; start a loop to process messages
    (go-loop [[in out] (connect-socket url)
              ping-count 0
              to (mk-timeout)]
      ;; get whatever needs to be done for either data coming from the socket
      ;; or from the user
	  (println "Debug - go loop")
      (let [[v p] (async/alts! [cout in to])]
        ;; if something goes wrong, just die for now
        ;; we should do something smarter, may be try and reconnect
        (if (= p to)
          ;; time to ping
          (do
            (println ":: ping? pending pings:" ping-count)
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

                ;; the websocket has sent us something, figure out if its of interest
                ;; to us, and if it is, send it to the evaluator
                (do
                  (println ":: incoming:" v)
                  (if (= (:type v) "pong")
                    (println ":: pong! latency: " (- (System/currentTimeMillis) (:ts v)) "ms.")
                    (println "TODO code to handle incomming messages from SLACK")
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
      (if-let [form (<! in)] ;this line gives me "No implementation of method: :take! of protocol:" (because in='x')
	     (println "debug worker taken form")
;        (let [input (:input form)
;		       res (eval-expr input)
;              ]
;          (println ":: form >> " input)
;          (>! out (assoc form :evaluator/result "ABC"))
;          (recur [in out stop])
;		) ;let
		
        ;; something wrong happened, re init
;        (do
;          (println ":: WARNING! The comms went down, going to restart.")
;          (stop)
;          (<! (async/timeout 3000))
;          (recur (inst-comm)))
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

