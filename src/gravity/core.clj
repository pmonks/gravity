;
; Copyright © 2019 Peter Monks Some Rights Reserved
;
; This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
; To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/ or send a
; letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
;

(ns gravity.core
  (:require [clojure.math.combinatorics :as comb]))

(def G           1.5)     ; Our version of the gravitational constant
(def mass-factor 1.25)    ; Exponent for mass, to help keep the simulation compact

(defn sq
  "The square of x."
  [x]
  (* x x))

(defn square-distance
  "The distance, squared, between o1 and o2."
  [o1 o2]
  (+ (sq (- (:x o2) (:x o1)))
     (sq (- (:y o2) (:y o1)))))

(def π Math/PI)

(defn rads-to-degs
  "The angle in degrees of rad (radians)."
  [rad]
  (* rad (/ 180 π)))

(defn pow
  [x y]
  (Math/pow x y))
(def pow-mem (memoize pow))

(defn atan2
  [x y]
  (Math/atan2 x y))

(defn cos
  [x]
  (Math/cos x))

(defn sin
  [x]
  (Math/sin x))

(defn scaled-mass
  [o]
  (let [mass (get o :mass 1)]
    (pow-mem mass mass-factor)))

(defn g-force-polar
  "Returns gravitational force between two 'objects' as a polar vector of 2 elements:
   1. magnitude of gravitational force
   2. direction of gravitational force (radians)"
  [o1 o2]
  [(* G (/ (* (scaled-mass o1) (scaled-mass o2)) (square-distance o1 o2)))
   (atan2 (- (:y o2) (:y o1)) (- (:x o2) (:x o1)))])

(defn g-force-rect
  "Returns gravitational force between two 'objects' as a rectilinear vector of 2 elements:
   1. magnitude of force in horizontal (X) plane
   2. magnitude of force in vertical (Y) plane"
  [o1 o2]
  (let [gf  (g-force-polar o1 o2)
        mag (first  gf)
        dir (second gf)]
    [(* mag (cos dir)) (* mag (sin dir))]))

(defn accel-rect
  "The acceleration of o1 due to o2 as a rectilinear vector of 2 elements:
   1. acceleration in horizontal (X) plane
   2. acceleration in vertical (Y) plane"
  [o1 o2]
  (let [gf (g-force-rect o1 o2)]
    [(/ (first  gf) (scaled-mass o1))
     (/ (second gf) (scaled-mass o1))]))

(defn step-simul-pair
  "Returns the accelerations obj1 and obj2 have on each other, as a vector of 2 elements,
  each of which is a map containing the obj (:obj) and the acceleration (as per accel-rect) it is experiencing (:accel)."
  [[obj1 obj2]]
  [{:obj   obj1
    ::accel (accel-rect obj1 obj2)}
   {:obj   obj2
    ::accel (accel-rect obj2 obj1)}])

(defn pmapcat
  [f batches]
  (->> batches
       (pmap f)
       (apply concat)
       doall))

(def sum (partial apply +))

(defn avg
  [nums]
  (when (> (count nums) 0)
    (/ (sum nums) (count nums))))

(defn radius
  "The radius of the given object based on its mass, rounded down to the nearest integer."
  [o]
  (int (max 1 (pow-mem (:mass o) 0.6))))    ; Real value for stars is ~0.8

(defn- next-locs-and-vels
  "Calculates the next locations and velocities of the given objects."
  [objs bounce-at-edge? min-x min-y max-x max-y]
  (let [pairwise-accelerations (pmapcat step-simul-pair (comb/combinations objs 2))
        accelerations-per-obj  (group-by :obj pairwise-accelerations)
        net-accelerations      (pmap #(assoc % ::x-accel (sum (map (fn [x] (first  (::accel x))) (get accelerations-per-obj %)))
                                               ::y-accel (sum (map (fn [x] (second (::accel x))) (get accelerations-per-obj %))))
                                     (keys accelerations-per-obj))]
    (pmap #(let [x-vel     (get % :x-vel 0)
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
          net-accelerations)))

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
  (let [highest-mass (apply max (map :mass objs))
        primary      (first (filter #(= highest-mass (:mass %)) objs))
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
      (pmap merge-objects groups))))

(defn step-simul
  "Produces a new set of objects, based on the gravitational force the input set of objects apply on each other.
   Note: this is not a physically accurate algorithm; it's simply fun to play with."
  ([objs] (step-simul objs false false nil nil nil nil))
  ([objs merge-collided-objects? bounce-at-edge? min-x min-y max-x max-y]
     (let [new-objs (next-locs-and-vels objs bounce-at-edge? min-x min-y max-x max-y)]
       (if merge-collided-objects?
         (merge-collided-objects new-objs)
         new-objs))))
