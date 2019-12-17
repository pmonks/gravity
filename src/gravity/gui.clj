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

(defn draw-obj
  [c obj]
  (c2d/with-canvas-> c
    (c2d/set-color (get obj :colour :white))
    (c2d/ellipse (:x obj) (:y obj) (* 2 (:mass obj)) (* 2 (:mass obj)))))

(defn simulate
  [width height objs]
  ; Create a window, draw the initial objects, then ...
  (let [c        (c2d/canvas width height)
        w        (c2d/show-window {:canvas c :window-name "Gravity Simulation" :always-on-top? true :background :black})
        draw-fn  (partial draw-obj c)
        ]
    (doall (map draw-fn objs))
    (Thread/sleep 5000)   ; ...give the user a chance to open the window before the simulation runs, then...

    ; ...run the simulation...
    (loop [objs objs]
      (c2d/with-canvas-> c
        (c2d/set-background :black))
      (doall (map draw-fn objs))
      (Thread/sleep 10)
      (if (c2d/key-pressed? w)   ; ...until a key is pressed.
        (c2d/close-window w)
        (recur (gc/step-simul objs true 0 0 width height))))))
