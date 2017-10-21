(ns test-display-and-control.modules.simple
  (:gen-class)
  (:require [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
        [clj-time.core :as t]
  ))

;simple minimal module to demo module functions

(defn paint-canvas [c g]
  (.drawString g "Example simple module" 10 10)
)


(defn describe [] 
  (hash-map 
    :module-name "mod-simple", 
    :module-description "Simple minimal module that provides slide with text"
    :paint-canvas paint-canvas
  )
)



