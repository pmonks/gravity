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

(def window-name "Gravity Simulator")

(defn- circle
  "Draws a solid, filled circle at location [x y], of radius r, and in colour colour."
  [c x y r colour]
  (c2d/with-canvas-> c
    (c2d/set-color colour)
    (c2d/ellipse x y r r)))

(defn- draw-obj
  "Draws an 'object', erasing it from its previous location (if it had one)"
  [c obj]
  (when (and (::old-x obj) (::old-y obj))
    (circle c (::old-x obj) (::old-y obj) (+ 4 (:mass obj)) :black))    ; Erase object at old location (if we have old coords)
  (circle c (:x obj) (:y obj) (:mass obj) (get obj :colour :white)))    ; Draw object at new location

(defn- draw-objs
  "Draws all 'objects' in objs"
  [c objs]
  (doall (map (partial draw-obj c) objs)))

(defn- draw-frame
  "Draws one frame of the simulation and then moves it forward"
  [c w _ _]
  (let [{width :width, height :height, objs :objs, :as state} (c2d/get-state w)]
    ; If the window has been closed, or a key pressed, close the window and quit
    (if (or (not (c2d/window-active? w))
            (c2d/key-pressed? w))
      (c2d/close-window w)
      ; Display one frame of the simulation, then step it
      (do
        (draw-objs c objs)
        (let [objs (map #(assoc % ::old-x (:x %) ::old-y (:y %)) objs)  ; Save previous locations (for erasing)
              objs (gc/step-simul objs true true 0 0 width height)]
          (c2d/set-state! w (assoc state :objs objs))))))
  nil)

(defn simulate
  "Opens a window of size width x height and simulates the given set of objects in it, continuing until the window is
   closed or a key is pressed.  Returns a handle to the window."
  [width height objs]
  (c2d/show-window {:canvas      (c2d/canvas width height)
                    :window-name window-name
                    :background  :black
                    :state       {:width   width
                                  :height  height
                                  :objs    objs}
                    :draw-fn     draw-frame}))
