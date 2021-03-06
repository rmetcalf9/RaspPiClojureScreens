(ns test-display-and-control.slack
  (:gen-class)
  (:require 
    [slack-api.core :as slack-api]
    [clojure.string :as str]
  )
)

;Single announcement function used when chatbot needs to send message to no particular channel
; as we do not know the channel id's until we have quired slack this function is stored in an
; atom. The start function will fill this atom with the correct function to call.
(def announcement-function (atom (fn [annouancment-txt] (println "Error!"))))
  
;Atom pointing to a List of Sets which contains the commands that the program supports
;commands have the structure:
; :name
; :exec - execute function
(def commands (atom []))
(defn register-list-of-commands [list-of-commands-to-add]
  (swap! commands concat list-of-commands-to-add)
)

(defn recieved-message [msg replyfn] (do
  (if (:is-message-for-my-attention msg) (do
    (def command-recieved (first (str/split (str (:actual_text msg) " ") #" ")))
    (def command (first (filter (fn [x] (= command-recieved (:name x))) @commands)))
    (if (nil? command)
      (replyfn "I don't understand that command") ;not a recognised command ignore it
      (if (= command ())
        (replyfn "I don't understand that command") ;not a recognised command ignore it
        ((:exec command) msg replyfn) ;execute the command
      )
	)
;    (replyfn (str "This is a test reply to msg: " (:actual_text msg)))
  ))
))

(defn command-description-string [command] (str (:name command) " - " (:description command)))

(defn cmd-list "Command to list availialbe commands" [msg replyfn] (do
  (replyfn (clojure.string/join "\n" (concat ["*I understand the following commands:*"] (map command-description-string @commands))))
))

(defn cmd-help "Display help message for particular command" [msg replyfn] (do
  (let [
      cmdArr (rest (str/split (str (:actual_text msg) " ") #" "))
    ]
    (if (= (count cmdArr) 1)
      (let [
          command-recieved-text (first cmdArr)
          command-looked-up (first (filter (fn [x] (= command-recieved-text (:name x))) @commands))
        ]
        (if (nil? command-looked-up)
          (replyfn (str "There is no command " command-recieved-text))
          (replyfn (str "Help for" (:name command-looked-up) "\n" (:helptext command-looked-up)))
        )
      )
      (replyfn "You must supply one argument which is the name of the command to get help on.\ne.g. help list")
    )
  )
))

(defn start
  [config]

  ;Add list commands command
  (register-list-of-commands 
    [
      {:name "list" :exec cmd-list :description "Command to list availiable commands" :helptext "TODO"}
      {:name "help" :exec cmd-help :description "Display the help text for a command" :helptext "Takes one paramater - the name of the command to display help text for"}
    ]
  )

  (let [[send-fn slack-vars] (slack-api/start config recieved-message)]
	(if (= (:announcement-channel config) nil)
	 (reset! announcement-function (fn [annouancment-txt] nil)); no announcement channel - do nothing when we get announcements
	 (do
	   (def announcement-channel (filter (fn [x] (= (:announcement-channel config) (:name x))) (:channels slack-vars)))
	   (if (= announcement-channel ())
	     (do
	       (println (str"Warning announcement channel not found - " (:announcement-channel config)))
		   (reset! announcement-function (fn [annouancment-txt] nil))
		 )
         (reset! announcement-function (fn [annouancment-txt] (send-fn {:channel (:id (first announcement-channel)) :message-string annouancment-txt})))
	   )
	 )
	)
	(@announcement-function "Hello everyone!")
  )
  {:onclosehandler (partial @announcement-function "Goodbye all")}
)

