(ns test-display-and-control.paint
  (:gen-class)
  (:use [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
        [test-display-and-control.clock :as tdcclock]
  ))

(defn paint-main-canvas [c g]
  (tdcclock/draw-clock c g [300 300] 150 5)
)

