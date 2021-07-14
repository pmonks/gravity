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

(require '[clojure2d.core])
(require '[gravity.util :as gu] :reload-all)
(require '[gravity.gui  :as gg] :reload-all)

; Graphics stuff
(def width  (- (clojure2d.core/screen-width) 50))
(def height (- (clojure2d.core/screen-height) 50))

(println "\nℹ️  Look for the Java GUI window, bring it into focus, and after 5 seconds an initial random simulation will start.  Press any key to close the window.")
(flush)

; Run a simulation with a random number of randomly placed objects
(gg/simulate width height (gu/gen-random-objs 50 200 width height))

(println "\nℹ️  To run another simulations of between X and Y random objects, run:\n")
(println "    (gg/simulate width height (gu/gen-random-objs X Y width height))\n")
(println "  or, to show trails:\n")
(println "    (gg/simulate width height (gu/gen-random-objs X Y width height) :trails true)\n")
(flush)

; To regenerate the demo gif
;(gg/simulate 800 600 (gu/gen-random-objs 30 30 800 600))
