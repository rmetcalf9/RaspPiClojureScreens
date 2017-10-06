(ns slack-api.core
  (:gen-class)
  (:require 
    [clojure.core.async :as async :refer [go go-loop]]
    [cheshire.core :refer [parse-string generate-string]]
    [clj-http.client :as http]
    [gniazdo.core :as ws]
    [clojure.core.async :as async :refer [>! <! go go-loop]]
  )
)

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
)

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
)

(defn get-comm-channel [api-token] (do
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
              (shutdown))
            (do
              (if (= p cout)
                ;; the user sent us something, time to send it to the remote end point
                (async/>! out {:id      (next-id) :type "message"
                               :channel (get-in v [:meta :channel])

;TODO Replace this section with code to send outgoing messages to slack
;                               :text    (-> v util/format-result-for-slack)}
                               :text    "TODO"}
                )

                ;; the websocket has sent us something, figure out if its of interest
                ;; to us, and if it is, send it to the evaluator
                (do
                  (println ":: incoming:" v)
                  (if (= (:type v) "pong")
                    (println ":: pong! latency: " (- (System/currentTimeMillis) (:ts v)) "ms.")

;TODO Replace this section with code to handle incomming messages from SLACK
;                  (when (can-handle? v prefix)
;                    (async/>! cin {:input (subs (:text v) 1) :meta  v})
;                  )
                )))
              (recur [in out]
                     (if (= (:type v) "pong")
                       (dec ping-count) ping-count)
                     (mk-timeout)))))))
    

  )
))


(defn safe-resolve [kw]
  (let [user-ns (symbol (namespace kw))
        user-fn (symbol (name kw))]
    (try
      (ns-resolve user-ns user-fn)
      (catch Exception e
        (require user-ns)
        (ns-resolve user-ns user-fn)))))


(defn kw->fn [kw]
  (try
    (safe-resolve kw)
    (catch Throwable e
      (throw (ex-info "Could not resolve symbol on classpath" {:kw kw})))))

 (defn make-comm [id api-token]
   (let [f (kw->fn id)] 
     (f api-token)))

;worker
(defn worker [{:keys [api-token]} recieved-message-function] (do
  (println "slack worker started")
  (let [
        inst-comm (fn []
                    (println ":: building com:")
                    (make-comm (":slack-api.core/get-comm-channel") api-token))
     ]
    (go-loop [[in out stop] (inst-comm)]
      (println ":: waiting for input")
    )
  )

;    (go-loop [[in out stop] (inst-comm)]
;      (println ":: waiting for input")
;      (if-let [form (<! in)]

;         (let [input (:input form)]
;           (recieved-message-function input)
;         )

;        (let [input (:input form)
;              res (recieved-message-function input)]
;TODO Replace this part - this is not how we want to send responses
;          (println ":: form >> " input)
;          (println ":: => " res)
;          (>! out (assoc form :evaluator/result res))
;          (recur [in out stop])
;        )

;        ;; something wrong happened, re init
;        (do
;          (println ":: WARNING! The comms went down, going to restart.")
;          (stop)
;          (<! (async/timeout 3000))
;          (recur (inst-comm)))))

;    (.join (Thread/currentThread))
;  )

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

