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

; Handy function for simulation
(defn sim
  "Open a new simulation window intitialised with a random assortment of between
  `min-objs` and `max-objs` objects, and with the optional `opts` (as for
  [gravity.gui/simulate]). Returns `nil`."
  ([min-objs max-objs] (sim min-objs max-objs nil))
  ([min-objs max-objs opts]
   (gg/simulate width height (gu/gen-random-objs min-objs max-objs width height) opts)
   nil))

(println "\nℹ️  Look for the Java GUI window, bring it into focus, and after 5 seconds an initial random simulation will start.  Press any key to close the window.")
(flush)

; Run a simulation with between 50 and 200 randomly placed objects
(sim 50 200)

(println "\nℹ️  To run a simulation with between X and Y random objects, run:\n")
(println "    (sim X Y)\n")
(println "  To show trails:\n")
(println "    (sim X Y {:trails? true})\n")
(flush)

; To regenerate the demo gif
;(sim 30 30)
