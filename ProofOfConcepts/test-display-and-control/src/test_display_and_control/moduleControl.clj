(ns test-display-and-control.moduleControl
  (:gen-class)
  (:require 
        [test-display-and-control.mod-clock :as mod-clock]
        [test-display-and-control.mod-simple :as mod-simple]
  ))

;List of all the loaded modules - this points to the modules describe function.
; the modules describe function is called to return all data about the module
(def module-list [
  mod-clock/describe
  mod-simple/describe
])


(defn paint-canvas [c g]
  (mod-clock/draw-clock c g [300 300] 150 5)

  ;;(println ((first module-list)))
)

