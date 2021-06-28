;
; Copyright © 2019 Peter Monks Some Rights Reserved
;
; This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
; To view a copy of this license, visit https://creativecommons.org/licenses/by-nc-sa/4.0/ or send a
; letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
;

(require '[clojure2d.core])
(require '[gravity.gui  :as gg] :reload-all)

(defn rand-int-in-range
  "Returns a random integer between mini and maxi (inclusive - note that this is *not* how clojure.core/rand-int works)."
  [mini maxi]
  (+ mini (rand-int (inc (- maxi mini)))))

; Graphics stuff
(def width  (- (clojure2d.core/screen-width) 50))
(def height (- (clojure2d.core/screen-height) 50))
;(def colours clojure2d.color/named-colors-list)
(def colours [:red       :orange      :yellow      :green     :blue       :indigo :violet
              :darkred   :darkorange  :brown       :darkgreen :darkblue           :dark-violet
              :indianred :lightsalmon :lightyellow :lightgreen :lightblue         :pink])  ; See https://clojure2d.github.io/clojure2d/docs/static/colors.html for a full list of what's available

(defn gen-random-objs
  "Generates between mini and maxi random objects."
  ([mini maxi] (gen-random-objs mini maxi width height))
  ([ mini maxi width height]
   (let [border                  (/ (min width height) 6)
         min-starting-mass       1
         max-starting-mass       10
         max-starting-vel        2.5
         double-max-starting-vel (* max-starting-vel 2)]
     (for [i (range (rand-int-in-range mini maxi))]
       {
        :colour (nth colours (mod i (count colours)))
        :mass   (rand-int-in-range min-starting-mass max-starting-mass)
        :x      (rand-int-in-range border (- width  border))
        :y      (rand-int-in-range border (- height border))
        :x-vel  (- (rand double-max-starting-vel) max-starting-vel)
        :y-vel  (- (rand double-max-starting-vel) max-starting-vel)}))))

(println "\nℹ️  Look for the Java GUI window, bring it into focus, and after 5 seconds an initial random simulation will start.  Press any key to close the window.")
(flush)

; Run a simulation with a random number of randomly placed objects
(gg/simulate width height (gen-random-objs 50 200))

(println "\nℹ️  To run another simulations of between X and Y random objects, run:\n")
(println "    (gg/simulate width height (gen-random-objs X Y))\n")
(println "  or, to show trails:\n")
(println "    (gg/simulate width height (gen-random-objs X Y) :trails true)\n")
(flush)

; To regenerate the demo gif
;(gg/simulate 800 600 (gen-random-objs 30 30 800 600))


; Co-orbiters
(comment
(gg/simulate width height
  [{:colour :yellow       ; ☀️
    :mass   50
    :x      (/ width 2)
    :y      (/ height 2)
    :x-vel  -0.06
    :y-vel  0.0}
   {:colour :blue   ; 🌏
    :mass   5
    :x      (/ width 2)
    :y      (- (/ height 2) 200)
    :x-vel  1.0
    :y-vel  0.0}])
)