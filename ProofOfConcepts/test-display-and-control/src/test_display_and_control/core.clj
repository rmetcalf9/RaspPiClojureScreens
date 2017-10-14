(ns test-display-and-control.core
  (:gen-class)
  (:require 
        [seesaw.core :as sc]
        [seesaw.graphics :as sg]
        [seesaw.color :as scolor]
        [test-display-and-control.paint :as tdcpaint]
        [test-display-and-control.config :as config]
        [test-display-and-control.slack :as slack]
        [clj-time.core :as t]
  ))

;create a list of functions we should call when the app 
;will close down
;functions will have no paramaters
(def close-down-notificaiton-functions (atom []))
(defn register-function-for-close-down-notification [fn] (swap! close-down-notificaiton-functions concat [fn]))
  
;forward declaration
(def main-window)

;set the swing style to native
(sc/native!)

;; #object[java.awt.event.KeyEvent 0x7cb04c5a java.awt.event.KeyEvent[KEY_RELEASED,keyCode=27,keyText=Escape,keyChar=Escape,keyLocation=KEY_LOCATION_STANDARD,rawCode=9,primaryLevelUnicode=27,scancode=0,extendedKeyCode=0x1b] on frame0]

;0 if not running non zero otherwise
(def appRunning (atom 1))

(defn keyreleaseHandler
  [event]
  ;; (alert event (str "<html>Hello from <b>Clojure</b>. keyreleaseHandler event  "))
  (if (= (.getKeyCode event) 27) 
   (sc/dispose! main-window)
  )
)


(defn oncloseHandler
  [event]
  (do
    (println "Application onclose START")
    (swap! appRunning dec)
	
	;execute all our close down functions
	(doseq [i @close-down-notificaiton-functions] (i) )
	
    ;wait some time to allow threads to complete
    (Thread/sleep 1000)
    (println "Application onclose")
    (System/exit 0)
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
				:listen [:key-released keyreleaseHandler :window-closed oncloseHandler]
  )
)

;timer function runs frequently
; it will repaint the display 
; https://icyrock.com/blog/2012/01/clojure-and-seesaw/q
; (timer (fn [e] (sc/repaint! (sc/select f [:#clock])) 1000))))
(defn timer-fn [timer-state] (do
  (sc/repaint! (sc/select main-window [:#maincanvas]))
))


;To stop screensaver from kicking in simulate pressing and releasing the ctl key
;https://stackoverflow.com/questions/6178132/fullscreen-java-app-minimizes-when-screensaver-turns-on
(def robot (new java.awt.Robot))
(def ctlKey java.awt.event.KeyEvent/VK_CONTROL)
(defn prevent-screensaver [] (do
  (.waitForIdle robot)
  (.keyPress robot ctlKey)
  (.keyRelease robot ctlKey)
  (print ".")
))

;Worker thread that will call any function (fn) repeatadly (with a wait delay) until the app terminates
(defn worker [fn wait] (do
  (println "start of worker")
  (loop [] (do
    (fn)
    ;Can't just sleep for wait seconds as
    ;    if wait is long this will be a problem - worker may not end
    ;    instead it should wake up mutiple times during it's wait period and
    ;    terminate immedatadly if delay is over
    (def endSleepTime (atom (t/plus (t/now) (t/millis wait))))
    (while (t/before? (t/now) @endSleepTime) (do
      (Thread/sleep 100)
      (if (not(pos? @appRunning)) (do
        (reset! endSleepTime  (t/now))
      ))
    ))
    ;(Thread/sleep wait)
    (if (pos? @appRunning) (recur))
  ))
  (println "end of worker")
))

(defn -main
  "Main entry point for slideshow application"
  [& args]
  (println "Start Execution")

  (let [config (config/read-config)]

    ;(println (config :comm))
    (def slack-ret (slack/start config))
	  (register-function-for-close-down-notification (:onclosehandler slack-ret))
	
    ;Only go into fullscreen if config allows
    (if (:supress-fullscreen config)
      (println "Fullscreen supressed due to config")
      (sc/full-screen! main-window)
    )

    ;(sc/pack! main-window) not needed as we don't need the frame to resize to fit contents
    (sc/show! main-window)

    ;press and unpress ctl key to stop screen saver from starting
    ;https://stackoverflow.com/questions/1768567/how-does-one-start-a-thread-in-clojure
    (future (worker prevent-screensaver 10000))

    ;start a timer that runs every half second to repaint screen
    (sc/timer timer-fn :initial-value [0] :delay 50)

  )

  (println "End Main Function")
  )
