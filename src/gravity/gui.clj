;
; Copyright © 2019 Peter Monks Some Rights Reserved
;
; This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
; To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/ or send a
; letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
;

(ns gravity.gui
  (:require [clojure2d.core :as c2d]
            [gravity.core   :as gc]))

(defn circle
  [c x y r colour]
  (c2d/with-canvas-> c
    (c2d/set-color colour)
    (c2d/ellipse x y r r)))

(defn draw-obj
  [c obj]
  (if (and (:old-x obj) (:old-y obj))
    (circle c (:old-x obj) (:old-y obj) (+ 2 (* 2 (:mass obj))) :black))    ; Erase object at old location
  (circle c (:x obj) (:y obj) (* 2 (:mass obj)) (get obj :colour :white)))  ; Draw object at new location

(defn countdown
  [c]
  (doall (for [x (reverse (range 5))]
    (do
      (c2d/with-canvas-> c
        (c2d/set-font-attributes 50 :bold)
        (c2d/set-color :black)
        (c2d/rect 10 10 50 50)
        (c2d/set-color :white)
        (c2d/text (str (inc x)) 10 50))
      (Thread/sleep 1000))))
  (c2d/with-canvas-> c
    (c2d/set-color :black)
    (c2d/rect 0 0 60 60)))

(defn simulate
  [width height objs]
  ; Create a window, draw the initial objects, then ...
  (let [c        (c2d/canvas width height)
        w        (c2d/show-window {:canvas c :window-name "Gravity Simulation" :background :black})
        draw-fn  (partial draw-obj c)]
    (doall (map draw-fn objs))
    (countdown c)         ; ...give the user a chance to open the window before the simulation runs, then...
;    (Thread/sleep 5000)   ; ...give the user a chance to open the window before the simulation runs, then...

    ; ...run the simulation...
    (loop [objs objs]
      (doall (map draw-fn objs))
      (let [objs     (map #(assoc % :old-x (:x %) :old-y (:y %)) objs)  ; Save previous locations (for erasing)
            new-objs (gc/step-simul objs true 0 0 width height)]
        (Thread/sleep 5)
        (if (c2d/key-pressed? w)   ; ...until a key is pressed.
          (c2d/close-window w)
          (recur new-objs))))))
