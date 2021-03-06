(ns test-display-and-control.modules.clock
  (:gen-class)
  (:require [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
        [clj-time.core :as t]
  ))

;Module that provides slides that display a clock


;Time library documentation
;https://github.com/seancorfield/clj-time

;Example of canvas drawing
;http://jng.imagine27.com/index.php/2009-09-12-122605_pong_in_clojure.html

(defn draw-clock [c g center radius stroke_width]
  (sg/translate g (first center) (last center))

  ;center square
  (sg/draw g (sg/rect -2 -2 2 2)
           (sg/style :foreground (scolor/color :black) :stroke (sg/stroke :width stroke_width))
  )

  ;Draw number marks
  (def bigticksize (/ radius 5))
  (def smallticksize (/ radius 20))

  (defn paint-tick [num ticksize] (do
    (sg/rotate g (* num (/ 360 12)))
    (sg/draw g (sg/polygon [0 (- 0 radius)] [0 (- 0 (- radius ticksize))])
        (sg/style :foreground (scolor/color :black) :stroke (sg/stroke :width stroke_width))
    )
		;rotate back same amount to reset canvas
    (sg/rotate g (- 0 (* num (/ 360 12))))
  ))

	;TRYING to make it work over a list [0 90 180 270]
	(paint-tick 1 smallticksize)
	(paint-tick 2 smallticksize)
	(paint-tick 3 bigticksize)
	(paint-tick 4 smallticksize)
	(paint-tick 5 smallticksize)
	(paint-tick 6 bigticksize)
	(paint-tick 7 smallticksize)
	(paint-tick 8 smallticksize)
	(paint-tick 9 bigticksize)
	(paint-tick 10 smallticksize)
	(paint-tick 11 smallticksize)
	(paint-tick 12 bigticksize)


	(defn paint-hand [angle colour length width] (do
    (sg/rotate g angle)
    (sg/draw g (sg/polygon [0 (- 0 length)] [0 0])
        (sg/style :foreground (scolor/color colour) :stroke (sg/stroke :width width))
    )
    (sg/rotate g (- 0 angle))
	))

	(def curtime (t/now))
	(def h (t/hour curtime))
	(def m (t/minute curtime))
	(def s (t/second curtime))
	(def ms (t/milli curtime))

	(def time_string (str h ":" m ":" s))
	;(println time_string)

	(def mss (+ (* s 1000) ms))

	;Draw second hand
	(paint-hand (* mss (/ 360 60000)) :red (* 9 (/ radius 10)) (/ stroke_width 2))

	;Draw minute hand
	(def minutes_and_seconds (+ s (* m 60)))
	(paint-hand (* minutes_and_seconds (/ 360 (* 60 60))) :blue (* 7 (/ radius 9)) stroke_width)

	;Draw hour hand
	;Example caculation
	;At 4pm the values will be:
	;minutes_and_seconds = 0
	;hours_minutes_and_seconds = (4 * 60 * 60) + 0 = 14,400
	;total hours minutes and seconds in 1 rotation = 60 * 60 * 60 = 216,000
	;angle per hour min and sec = (/ 360 (* 12 60 60)) = 0.0083333333333
	;angle = 14,400 * 0.0083333333333 = 120

	(def hours_minutes_and_seconds (+ minutes_and_seconds (* h 60 60)))
	(paint-hand (* hours_minutes_and_seconds (/ 360 (* 12 60 60))) :green (* 3(/ radius 6)) (* stroke_width 2))


  ;drawing dummy empty polygon to canvas to change style for drawstring calls
  (sg/draw g (sg/polygon)
      (sg/style :foreground (scolor/color :black) :stroke (sg/stroke :width 5))
  )
  (.drawString g "Example of writing string to canvas" 0 (+ radius 20))
  (.drawString g time_string 0 (+ radius 40))


  ;reset canvas back to origional settings
  (sg/translate g (- 0 (first center)) (- 0 (last center)))
)

(defn paint-canvas [c g]
  (draw-clock c g [300 300] 150 5)
)

(defn describe [] 
  (hash-map 
    :module-name "mod-clock", 
    :module-description "Slides that display a clock"
    :paint-canvas paint-canvas
  )
)


