(ns test-display-and-control.moduleControl
  (:gen-class)
  (:require 
        [test-display-and-control.modules.clock :as mod-clock]
        [test-display-and-control.modules.simple :as mod-simple]
		[test-display-and-control.slack :as slack]
  ))

;List of all the loaded modules - this points to the modules describe function.
; the modules describe function is called to return all data about the module
(def module-list [
  mod-clock/describe
  mod-simple/describe
])

(defn module-description-string [module] (let [mod (module)] (str (:module-name mod) " - " (:module-description mod))))

(defn cmd-modules "Command to list availialbe modules" [msg replyfn] (do
  (replyfn (clojure.string/join "\n" (concat ["*I have the following modules availiable:*"] (map module-description-string module-list))))
))

(slack/register-list-of-commands 
  [
    {:name "modules" :exec cmd-modules :description "Command to list availiable modules" :helptext "TODO"}
  ]
)

(defn paint-canvas [c g]
  (mod-clock/draw-clock c g [300 300] 150 5)

  ;;(println ((first module-list)))
)

