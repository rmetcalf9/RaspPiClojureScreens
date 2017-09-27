(ns slack-api.core
  (:gen-class)
  (:require 
    [clojure.core.async :as async :refer [go go-loop]]
    [cheshire.core :refer [parse-string generate-string]]
    [clj-http.client :as http]
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

;worker that runs in seperate thread
(defn worker [{:keys [api-token]}] (do
  (println "slack worker started")
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
  )
  (println "slack worker ended")
))

(defn start
  [config]
  (future (worker config))
  
  ;TODO Errors aren't appearing in future. moving out of future to help with debugging
  ;(worker config)
)

