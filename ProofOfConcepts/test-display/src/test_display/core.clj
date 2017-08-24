(ns test-display.core
  (:gen-class)
  (:use seesaw.core)
  (:require [clojure.java.io :as io]))

  ;; testing how to display information

(native!)

;; this is our image file - make sure that this image exists in the projects resources folder
(def image-file (io/resource "rainbow_pic.jpg"))

;; we can rescale by height or width as required in the future. 500 and 500 define the scaling dimensions
(def resized-file (.getScaledInstance (javax.imageio.ImageIO/read image-file) 500 500 1))

(def picture (label :icon resized-file)) 

;; function to create a frame and show it
(defn create-frame-with-pic
  [frame-width frame-height pic]
  "create a frame that is filled with the picture file
  picture file must be in the resources folder"
  (frame :title "picFrame" :width frame-width :height frame-height :content pic))

;; (full-screen! f) ;; this will make a frame full screen

;; Display some html

(def html-file (io/resource "test.html")) ;; here is the html file

(def html (editor-pane :page html-file :content-type "text/html"))

(defn create-frame-with-html
  [html-content]
  "create a frame that contains some html"
  (frame :title "htmlFrame" :content html-content))

(defn -main
  "Display the pic frame and the html frame."
  []
  (-> (create-frame-with-pic 500 500 picture)
      show!)
  (-> (create-frame-with-html html)
      pack!
      show!)
  )
