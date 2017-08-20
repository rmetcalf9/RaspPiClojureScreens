(ns test-display.core
  (:gen-class)
  (:use seesaw.core)
  (:require [clojure.java.io :as io]))

  ;; testing how to display information

(native!)

;; this is our image file - make sure that this image exists in the projects resources folder
(def image-file "rainbow_pic.jpg")

;; we can rescale by height or width as required in the future. 100 and 100 define the scaling dimensions
(def resized-file (.getScaledInstance (javax.imageio.ImageIO/read (io/resource image-file)) 500 500 1))

(def picture (label :icon resized-file)) 

;; function to create a frame and show it
(defn create-frame-with-pic
  [frame-width frame-height pic]
  "create a frame that is filled with the picture file
  picture file must be in the resources folder"
  (frame :title "picFrame" :width frame-width :height frame-height :content pic))

;; (full-screen! f) ;; this will make a frame full screen

(defn -main
  "Scale the picture into a frame of the same size."
  []
  (->
   (create-frame-with-pic 500 500 picture)
   show!))
