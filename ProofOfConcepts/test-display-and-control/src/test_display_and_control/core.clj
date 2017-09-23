(ns test-display-and-control.core
  (:gen-class)
  (:use [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
  ))


;forward declaration
(def main-window)

;set the swing style to native
(sc/native!)

;; #object[java.awt.event.KeyEvent 0x7cb04c5a java.awt.event.KeyEvent[KEY_RELEASED,keyCode=27,keyText=Escape,keyChar=Escape,keyLocation=KEY_LOCATION_STANDARD,rawCode=9,primaryLevelUnicode=27,scancode=0,extendedKeyCode=0x1b] on frame0]


(defn keyreleaseHandler
  [event]
  ;; (alert event (str "<html>Hello from <b>Clojure</b>. keyreleaseHandler event  "))
  (if (= (.getKeyCode event) 27) 
   (dispose! main-window)
  )
)

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

	;Draw second hand
	(paint-hand 10 :red (* 9 (/ radius 10)) (/ stroke_width 2))

	;Draw minute hand
	(paint-hand 20 :blue (* 7 (/ radius 9)) stroke_width)

	;Draw hour hand
	(paint-hand 30 :green (* 3(/ radius 6)) (* stroke_width 2))

)

(defn paint-main-canvas [c g]
  (draw-clock c g [300 300] 150 5)
)

(def main-canvas
  (sc/canvas :id         :maincanvas
             :background :lightgrey
             :paint paint-main-canvas
  )
)

(def main-window
  (sc/frame 
				:title "SlideShow Application Frame" 
				:content main-canvas
				:on-close :exit
				:listen [:key-released keyreleaseHandler]
  )
)

(defn -main
  "Main entry point for slideshow application"
  [& args]
  (println "Start Execution")
  (sc/full-screen! main-window)
  (sc/pack! main-window)
  (sc/show! main-window)
  (println "End Execution")
  )
