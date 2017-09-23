(ns test-display-and-control.core
  (:gen-class)
  (:use [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
        [test-display-and-control.paint :as tdcpaint]
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

(def main-canvas
  (sc/canvas :id         :maincanvas
             :background :lightgrey
             :paint tdcpaint/paint-main-canvas
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
