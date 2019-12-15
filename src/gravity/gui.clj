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
;    (c2d/set-background :black)  ; This isn't smooth at all...
    (c2d/set-color (get obj :colour :white))
    (c2d/ellipse (:x obj) (:y obj) (* 2 (:mass obj)) (* 2 (:mass obj)))))

(defn simulate
  [width height objs]
  (let [c       (c2d/canvas width height)
        w       (c2d/show-window {:canvas c :window-name "Gravity Simulation" :always-on-top? true :background :black})
        draw-fn (partial draw-obj c)]
    (doall (map draw-fn objs))
    (Thread/sleep 5000)   ; Give the user a chance to open the window before the simulation runs
    (loop [objs objs]
      (doall (map draw-fn objs))
      (Thread/sleep 10)
;      (Thread/sleep (/ 1000 250))  ; approximate 250 fps
      (if (c2d/key-pressed? w)   ; If a key was pressed, close window and return
        (c2d/close-window w)
        (recur (gc/step-simul objs))))))
