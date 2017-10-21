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
  (mod-clock/describe)
  (mod-simple/describe)
])

(defn module-description-string [module] (let [mod (module)] (str (:module-name mod) " - " (:module-description mod))))

(defn cmd-modules "Command to list availialbe modules" [msg replyfn] (do
  (replyfn (clojure.string/join "\n" (concat ["*I have the following modules availiable:*"] (map module-description-string module-list))))
))



;TODO We need to create a slide list, query it from active modules and display relevant slides
;     currently this is coded to just flip between the two modules when the next slide command
;     is used
;     Also the next two atoms should be put in transactional memory so they always change
;     together
(def active-module-idx (atom 0))

(defn cmd-next [msg replyfn] "Switch to next MODULE (TODO change when slides are implemented)"
  (let [
      next-idx (+ 1 @active-module-idx)
    ]
    (if (= next-idx (count module-list))
      (reset! active-module-idx 0)
      (reset! active-module-idx next-idx)
    )
    (replyfn "Done")
  )
)


(defn paint-canvas [c g]
  ((:paint-canvas (nth module-list @active-module-idx)) c g)
)



(slack/register-list-of-commands 
  [
    {:name "modules" :exec cmd-modules :description "Command to list availiable modules" :helptext "TODO"}
    {:name "next" :exec cmd-next :description "Switch to next MODULE (TODO change when slides are implemented)" :helptext "No help - command will be got rid of later"}
  ]
)

