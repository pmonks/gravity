;
; Copyright © 2019 Peter Monks Some Rights Reserved
;
; This file is part of pmonks/gravity.
;
; pmonks/gravity is free software: you can redistribute it and/or modify
; it under the terms of the GNU Affero General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
;
; pmonks/gravity is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU Affero General Public License for more details.
;
; You should have received a copy of the GNU Affero General Public License
; along with pmonks/gravity.  If not, see <https://www.gnu.org/licenses/>.
;

(ns gravity.gui
  (:require [clojure2d.core :as c2d]
            [gravity.core   :as gc]))

(def window-name-prefix "Gravity Simulator")
(def window-number      (atom 1))

(def window-registry (atom {}))

(defn- circle
  "Draws a solid, filled circle at location [x y], of radius r, and in colour colour."
  [c x y r colour]
  (let [diameter (* 2 r)]
    (c2d/with-canvas-> c
      (c2d/set-color colour)
      (c2d/ellipse x y diameter diameter))))

(defn- draw-obj
  "Draws an 'object'"
  [c obj]
  (circle c (:x obj) (:y obj) (gc/radius obj) (get obj :colour :white)))    ; Draw object at new location

(defn- draw-frame
  "Draws one frame of the simulation and then steps it forward"
  [c w _ _]
  (try
    (let [{objs            :objs
           collisions?     :collisions
           bounces?        :bounces
           trails?         :trails :as state} (c2d/get-state w)]
      (when-not trails?
        (c2d/with-canvas-> c
          (c2d/set-color :black)
          (c2d/rect 0 0 (c2d/width c) (c2d/height c))))
      (doall (map (partial draw-obj c) objs))
      (c2d/set-state! w (assoc state :objs (gc/step-simul objs collisions? bounces? 0 0 (c2d/width c) (c2d/height c))))
      nil)
    (catch Exception e
       (binding [*out* *err*]
         (println e)     ; TODO: add proper logging
         (flush)))))

(defn- close-window-by-name
  "Closes the given window, identified by its name"
  [window-name]
  (if-let [window (get @window-registry window-name)]
    (do
      (c2d/close-window window)
      (swap! window-registry #(dissoc % window-name)))
    (throw (ex-info (str "Could not find window " window-name) {}))))

(defn simulate
  "Opens a window of size width x height and simulates the given set of objects in it, continuing until the window is
   closed or the 'q' key is pressed.  Returns a handle to the window."
  [width height objs & {:keys [collisions bounces trails]
                        :or {collisions true bounces true trails false}}]
  (let [window-name (str window-name-prefix " " @window-number)
        window      (c2d/show-window {:canvas      (c2d/canvas width height)
                                      :window-name window-name
                                      :background  :black
                                      :state       {:objs       objs
                                                    :collisions collisions
                                                    :bounces    bounces
                                                    :trails     trails}
                                      :draw-fn     draw-frame})]
    (swap! window-number inc)
    (swap! window-registry #(assoc % window-name window))
    (defmethod c2d/key-typed [window-name \q] [_ _] (close-window-by-name window-name))    ; This is pretty hokey...
    (defmethod c2d/key-typed [window-name \Q] [_ _] (close-window-by-name window-name))
    window))
