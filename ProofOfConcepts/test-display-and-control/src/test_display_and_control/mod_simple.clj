(ns test-display-and-control.mod-simple
  (:gen-class)
  (:require [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
        [clj-time.core :as t]
  ))

;simple minimal module to demo module functions

(defn describe [] 
  (hash-map 
    :module-name "mod-simple", 
    :module-description "Simple minimal module that provides blank"
  )
)



