(ns dolphin.slack
  (:gen-class))

(def list-of-commands (atom []))

(defn add-to-list-of-commands 
	"This will add a list of new commands to the list of commands"
	[list-of-commands-to-add]
	(swap! list-of-commands concat list-of-commands-to-add))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
