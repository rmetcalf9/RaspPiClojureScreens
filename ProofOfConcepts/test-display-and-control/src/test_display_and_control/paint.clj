(ns test-display-and-control.paint
  (:gen-class)
  (:require [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
        [test-display-and-control.moduleControl :as tdmodcontrol]
  ))

(defn paint-main-canvas [c g]
  (tdmodcontrol/paint-canvas c g)
)

