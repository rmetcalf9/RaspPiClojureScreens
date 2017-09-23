(ns test-display-and-control.clock
  (:gen-class)
  (:use [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
        [clj-time.core :as t]
  ))

;Time library documentation
;https://github.com/seancorfield/clj-time

(defn draw-clock [c g center radius stroke_width]
  (translate g (first center) (last center))

  ;center square
  (sg/draw g (rect -2 -2 2 2)
           (style :foreground (scolor/color :black) :stroke (stroke :width stroke_width))
  )

  ;Draw number marks
  (def bigticksize (/ radius 5))
  (def smallticksize (/ radius 20))

  (defn paint-tick [num ticksize] (do
    (sg/rotate g (* num (/ 360 12)))
    (sg/draw g (polygon [0 (- 0 radius)] [0 (- 0 (- radius ticksize))])
        (style :foreground (scolor/color :black) :stroke (stroke :width stroke_width))
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
    (sg/draw g (polygon [0 (- 0 length)] [0 0])
        (style :foreground (scolor/color colour) :stroke (stroke :width width))
    )
    (sg/rotate g (- 0 angle))
	))

	(def curtime (t/now))
	(def h (t/hour curtime))
	(def m (t/minute curtime))
	(def s (t/second curtime))
	(def ms (t/milli curtime))

	(def mss (+ (* s 1000) ms))

	;Draw second hand
	(paint-hand (* mss (/ 360 60000)) :red (* 9 (/ radius 10)) (/ stroke_width 2))

	;Draw minute hand
	(def minutes_and_seconds (+ s (* m 60)))
	(paint-hand (* minutes_and_seconds (/ 360 (* 60 60))) :blue (* 7 (/ radius 9)) stroke_width)

	;Draw hour hand
	(def hours_minutes_and_seconds (+ minutes_and_seconds (* h 60 60)))
	(paint-hand (* hours_minutes_and_seconds (/ 360 (* 60 60 60))) :green (* 3(/ radius 6)) (* stroke_width 2))

)

