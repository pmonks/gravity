;
; Copyright © 2019 Peter Monks Some Rights Reserved
;
; This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
; To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/ or send a
; letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
;

(ns gravity.core
  (:require [clojure.math.combinatorics :as comb]))

(def G            1.5)     ; Our version of the gravitational constant
(def min-distance 4)       ; Minimum "allowed" distance between objects (to minimise ejections)
(def speed-limit  1.5)     ; Maximum allowed rectilinear velocity, in either dimension

(defn sq
  "The square of x."
  [x]
  (* x x))

(defn square-distance
  "The distance, squared, between o1 and o2."
  [o1 o2]
  (max min-distance  ; Clamp the minimum distance, to reduce "ejections"
       (+ (sq (- (:x o2) (:x o1)))
          (sq (- (:y o2) (:y o1))))))

(defn rads-to-degs
  "The angle in degrees of rad (radians)."
  [rad]
  (* rad (/ 180 Math/PI)))

(defn g-force-polar
  "Returns gravitational force between two 'objects' as a polar vector of 2 elements:
   1. magnitude of gravitational force
   2. direction of gravitational force (radians)"
  [o1 o2]
  [(* G (/ (* (:mass o1) (:mass o2)) (square-distance o1 o2)))
   (Math/atan2 (- (:y o2) (:y o1)) (- (:x o2) (:x o1)))])

(defn g-force-rect
  "Returns gravitational force between two 'objects' as a rectilinear vector of 2 elements:
   1. magnitude of force in horizontal (X) plane
   2. magnitude of force in vertical (Y) plane"
  [o1 o2]
  (let [gf  (g-force-polar o1 o2)
        mag (first  gf)
        dir (second gf)]
    [(* mag (Math/cos dir)) (* mag (Math/sin dir))]))

(defn accel-rect
  "The acceleration of o1 due to o2 as a rectilinear vector of 2 elements:
   1. acceleration in horizontal (X) plane
   2. acceleration in vertical (Y) plane"
  [o1 o2]
  (let [gf (g-force-rect o1 o2)]
    [(/ (first  gf) (:mass o1))
     (/ (second gf) (:mass o1))]))

(defn step-simul-pair
  "Returns the accelerations obj1 and obj2 have on each other, as a vector of 2 elements,
  each of which is a map containing the obj (:obj) and the acceleration (as per accel-rect) it is experiencing (:accel)."
  [[obj1 obj2]]
  [{:obj   obj1
    :accel (accel-rect obj1 obj2)}
   {:obj   obj2
    :accel (accel-rect obj2 obj1)}])

(def sum (partial apply +))

(defn step-simul
  "Produces a new set of objects, based on the gravitational force the input set of objects apply on each other.
   Note: this is not a physically accurate algorithm; it's simply fun to play with."
  [objs]
  (let [pairwise-accelerations (mapcat step-simul-pair (comb/combinations objs 2))
        accelerations-per-obj  (group-by :obj pairwise-accelerations)
        net-accelerations      (map #(assoc % :x-accel (sum (map (fn [x] (first  (:accel x))) (get accelerations-per-obj %)))
                                              :y-accel (sum (map (fn [x] (second (:accel x))) (get accelerations-per-obj %))))
                                    (keys accelerations-per-obj))]
    (for [obj net-accelerations]
      (dissoc (assoc obj
                     :x      (+ (:x obj) (:x-vel obj))
                     :y      (+ (:y obj) (:y-vel obj))
                     :x-vel  (max (* -1 speed-limit) (min speed-limit (+ (:x-vel obj) (:x-accel obj))))
                     :y-vel  (max (* -1 speed-limit) (min speed-limit (+ (:y-vel obj) (:y-accel obj)))))
              :x-accel
              :y-accel))))
