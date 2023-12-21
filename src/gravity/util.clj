;
; Copyright © 2021 Peter Monks
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

(ns gravity.util)

;(def colours clojure2d.color/named-colors-list)
(def colours [:red       :orange      :yellow      :green     :blue       :indigo :violet
              :darkred   :darkorange  :brown       :darkgreen :darkblue           :dark-violet
              :indianred :lightsalmon :lightyellow :lightgreen :lightblue         :pink])  ; See https://clojure2d.github.io/clojure2d/docs/static/colors.html for a full list of what's available

(defn rand-int-in-range
  "Returns a random integer between mini and maxi (inclusive - note that this is *not* how clojure.core/rand-int works)."
  [mini maxi]
  (+ mini (rand-int (inc (- maxi mini)))))

(defn gen-random-objs
  "Generates between mini and maxi random objects, randomly located between (0,0) and (width,height)."
  [mini maxi width height]
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
       :y-vel  (- (rand double-max-starting-vel) max-starting-vel)})))

