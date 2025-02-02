;
; Copyright © 2019 Peter Monks
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

(ns gravity.core
  (:require [clojure.math.combinatorics :as comb]
            [embroidery.api             :as e]))

; Note: because we store numbers in maps, we cannot have fully unboxed numbers (the JVM doesn't (yet) support primitives in maps)
;(set! *warn-on-reflection* true)
;(set! *unchecked-math* :warn-on-boxed)

(def ^{:tag 'double} G                       1.5)    ; Our dodgy version of the gravitational constant
(def ^{:tag 'double} mass-factor             1.25)   ; Exponent for mass, to help keep the simulation compact
(def ^{:tag 'double} mass-to-radius-exponent 0.6)    ; Exponent of mass to radius. Value for real stars is ~0.8 on average (with broad variance based on temperature)

(defmacro sq
  "The square of x."
  [x]
  `(let [val# ~x]
     (* val# val#)))

(defmacro square-distance
  "The distance, squared, between o1 and o2."
  [o1 o2]
  `(+ (sq (- (:x ~o2) (:x ~o1)))
      (sq (- (:y ~o2) (:y ~o1)))))

(def ^{:tag 'double} π java.lang.Math/PI)

(defmacro rad-to-deg
  "The angle of rad (radians) in degrees."
  [rad]
  `(* ~rad (/ 180 π)))

(defmacro pow
  [x y]
  `(java.lang.Math/pow ~x ~y))

(defmacro atan2
  [x y]
  `(java.lang.Math/atan2 ~x ~y))

(defmacro cos
  [x]
  `(java.lang.Math/cos ~x))

(defmacro sin
  [x]
  `(java.lang.Math/sin ~x))

(defmacro scaled-mass
  [o]
  `(let [mass# (get ~o :mass 1)]
     (pow mass# mass-factor)))

(defn g-force-polar
  "Returns gravitational force between two 'objects' as a polar vector of 2 elements:
   1. magnitude of gravitational force
   2. direction of gravitational force (radians)"
  [o1 o2]
  [(* G (/ (* (scaled-mass o1) (scaled-mass o2))
           (max (square-distance o1 o2) 0.00000001)))   ; Ensure we don't divide by zero
   (atan2 (- (:y o2) (:y o1)) (- (:x o2) (:x o1)))])

(defn g-force-rect
  "Returns gravitational force between two 'objects' as a rectilinear vector of 2 elements:
   1. magnitude of force in horizontal (X) plane
   2. magnitude of force in vertical (Y) plane"
  [o1 o2]
  (let [[mag dir] (g-force-polar o1 o2)]
    [(* mag (cos dir)) (* mag (sin dir))]))

(defn accel-rect
  "The acceleration of o1 due to o2 as a rectilinear vector of 2 elements:
   1. acceleration in horizontal (X) plane
   2. acceleration in vertical (Y) plane"
  [o1 o2]
  (let [[x-force y-force] (g-force-rect o1 o2)]
    [(/ x-force (scaled-mass o1))
     (/ y-force (scaled-mass o1))]))

(defn step-simul-pair
  "Returns the accelerations obj1 and obj2 have on each other, as a vector of 2 elements,
  each of which is a map containing the obj (:obj) and the acceleration (as per accel-rect) it is experiencing (::accel)."
  [[obj1 obj2]]
  [{:obj   obj1
    ::accel (accel-rect obj1 obj2)}
   {:obj   obj2
    ::accel (accel-rect obj2 obj1)}])

(defn pmapcat
  [f batches]
  (->> batches
       (e/pmap* f)
       (apply concat)
       doall))

(def sum (partial reduce +))

; Note: unused
;(defmacro avg
;  "Calculates the average of the given nums."
;  [nums]
;  `(when (> (count ~nums) 0)
;     (/ (sum ~nums) (count ~nums))))

(defmacro radius
  "The radius of the given object based on its mass, rounded down to the nearest integer (long)."
  [o]
  `(long (max 1 (pow (:mass ~o) mass-to-radius-exponent))))

(defn- next-locs-and-vels
  "Calculates the next locations and velocities of the given objects."
  [objs bounce-at-edge? min-x min-y max-x max-y]
  (case (count objs)
    ; No objects?  Do nothing.
    0 []

    ; 1 object? Just keep it moving with its current velocity
    1 (let [obj    (first objs)
            new-x  (+ (:x obj) (:x-vel obj))
            new-y  (+ (:y obj) (:y-vel obj))
            radius (radius obj)]
        [(assoc obj
                ::x-accel 0
                ::y-accel 0
                :x-vel (* (:x-vel obj)
                          (if (and bounce-at-edge?
                                   (or (and (< (- new-x radius) min-x) (neg? (:x-vel obj)))
                                       (and (> (+ new-x radius) max-x) (pos? (:x-vel obj)))))
                            -1
                            1))
                :y-vel (* (:y-vel obj)
                          (if (and bounce-at-edge?
                                   (or (and (< (- new-y radius) min-y) (neg? (:y-vel obj)))
                                       (and (> (+ new-y radius) max-y) (pos? (:y-vel obj)))))
                            -1
                            1))
                :x        new-x
                :y        new-y)])

    ; More than 1 object?  Calculate their mutual forces on one other, update their velocities accordingly, then update their positions.
    (let [pairwise-accelerations (pmapcat step-simul-pair (comb/combinations objs 2))
          accelerations-per-obj  (group-by :obj pairwise-accelerations)
          net-accelerations      (e/pmap* #(assoc % ::x-accel (sum (map (fn [x] (first  (::accel x))) (get accelerations-per-obj %)))
                                                 ::y-accel (sum (map (fn [x] (second (::accel x))) (get accelerations-per-obj %))))
                                          (keys accelerations-per-obj))]
      (e/pmap* #(let [x-vel     (get % :x-vel 0)
                      y-vel     (get % :y-vel 0)
                      new-x     (+ (:x %) x-vel)
                      new-y     (+ (:y %) y-vel)
                      new-x-vel (+ x-vel (::x-accel %))
                      new-y-vel (+ y-vel (::y-accel %))
                      radius    (radius %)]
                  (assoc % :x     new-x
                           :y     new-y
                           :x-vel (* new-x-vel
                                     (if (and bounce-at-edge?
                                              (or (and (< (- new-x radius) min-x) (neg? new-x-vel))
                                                  (and (> (+ new-x radius) max-x) (pos? new-x-vel))))
                                       -1
                                       1))
                           :y-vel (* new-y-vel
                                     (if (and bounce-at-edge?
                                              (or (and (< (- new-y radius) min-y) (neg? new-y-vel))
                                                  (and (> (+ new-y radius) max-y) (pos? new-y-vel))))
                                       -1
                                       1))))
               net-accelerations))))

(defn- collided?
  "Have the two objects collided?"
  [o1 o2]
  (< (square-distance o1 o2) (sq (+ (radius o1) (radius o2)))))

(defn- find-next-collision-group
  "Returns all objects that have collided with (first objs), including (first objs) itself."
  [objs]
  (let [f (first objs)
        r (rest objs)]
    (into [f] (filter (partial collided? f) r))))

(defn- merge-objects
  "Merges the given objects, by adding their masses, weighted-averaging their location and velocity, and retaining the colour of the most massive object."
  [objs]
  ; Note: don't use scaled masses here!
  (let [primary      (last (sort-by :mass objs))
        total-mass   (sum (map :mass objs))]
    (assoc primary
           :mass   total-mass
           :x      (/ (sum (map #(* (:x %) (:mass %)) objs)) total-mass)
           :y      (/ (sum (map #(* (:y %) (:mass %)) objs)) total-mass)
           :x-vel  (/ (sum (map #(* (:x-vel %) (:mass %)) objs)) total-mass)
           :y-vel  (/ (sum (map #(* (:y-vel %) (:mass %)) objs)) total-mass))))

(defn- merge-collided-objects
  "Finds and merges all objects that have collided."
  [objs]
  (loop [remaining-objs objs
         groups         []]
    (if (seq remaining-objs)
      (let [new-group (find-next-collision-group remaining-objs)]
        (recur (remove (set new-group) remaining-objs)
               (conj groups new-group)))
      (e/pmap* merge-objects groups))))

(defn step-simul
  "Produces a new set of objects, based on the gravitational force the input set of objects apply on each other.
   Note: this is not a physically accurate algorithm; it's simply fun to play with."
  ([objs] (step-simul objs false false nil nil nil nil))
  ([objs merge-collided-objects? bounce-at-edge? min-x min-y max-x max-y]
     (let [new-objs (next-locs-and-vels objs bounce-at-edge? min-x min-y max-x max-y)]
       (if merge-collided-objects?
         (merge-collided-objects new-objs)
         new-objs))))

