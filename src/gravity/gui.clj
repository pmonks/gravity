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

(defn circle
  "Draws a solid, filled circle at location [x y], of radius r, and in colour colour."
  [c x y r colour]
  (c2d/with-canvas-> c
    (c2d/set-color colour)
    (c2d/ellipse x y r r)))

(defn draw-obj
  "Draws an 'object', erasing it from its previous location (if it had one)"
  [c obj]
  (if (and (::old-x obj) (::old-y obj))
    (circle c (::old-x obj) (::old-y obj) (+ 2 (* 2 (:mass obj))) :black))    ; Erase object at old location (if we have old coords)
  (circle c (:x obj) (:y obj) (* 2 (:mass obj)) (get obj :colour :white)))    ; Draw object at new location

(defn draw-objs
  "Draws all 'objects' in objs"
  [c objs]
  (doall (map (partial draw-obj c) objs)))

(defn countdown
  "Displays a countdown timer in the top right corner for s seconds (defaults to 5 if not provided)."
  ([c] (countdown c 5))
  ([c s]
   (doall
     (for [x (map inc (reverse (range s)))]
       (let [old-text-width (c2d/with-canvas-> c
                              (c2d/set-font-attributes 50 :bold)
                              (c2d/text-width (str (inc x))))]
         (c2d/with-canvas-> c
           (c2d/set-font-attributes 50 :bold)
           ; Erase old counter
           (c2d/set-color :black)
           (c2d/rect 10 10 (+ 10 old-text-width) 50)
           ; Draw new counter
           (c2d/set-color :white)
           (c2d/text (str x) 10 50))
         ; Sleep for 1 second
         (Thread/sleep 1000))))
   (c2d/with-canvas-> c
     (c2d/set-color :black)
     (c2d/rect 0 0 60 60))))

(defn draw-frame
  "Draws one frame of the simulation and then moves it forward"
  [c w f loop-state]
  (if (or (not (c2d/window-active? w))   ; Quit if the window has been closed...
          (c2d/key-pressed? w))          ; ...or any key has been pressed
    (c2d/close-window w))
  (let [{width :width, height :height, objs :objs, running :running, :as state} (c2d/get-state w)]
    (if running
      (do
        (draw-objs c objs)
        (let [objs     (map #(assoc % ::old-x (:x %) ::old-y (:y %)) objs)  ; Save previous locations (for erasing)
              new-objs (gc/step-simul objs true 0 0 width height)]          ; Step the simulation
          (c2d/set-state! w (assoc state :objs new-objs)))))))

(defn simulate
  "Opens a window of size width x height and simulates the given set of objects in it, continuing until a key is pressed.
   Note: this method blocks the caller for 5 seconds (during the countdown), before returning."
  [width height objs]
  ; Create a window, draw the initial objects, then ...
  (let [initial-state {:width           width
                       :height          height
                       :objs            objs
                       :running         false}
        c             (c2d/canvas width height)
        w             (c2d/show-window {:canvas      c
                                        :window-name "Gravity Simulation"
                                        :background  :black
                                        :state       initial-state
                                        :draw-fn     draw-frame})]
    (draw-objs      c objs)
    (countdown      c 5)                                      ; ...give the user 5 seconds to focus the window before the simulation runs, then...
    (c2d/set-state! w (assoc initial-state :running true)))   ; Start the simulation running
    nil)
