
# gravity

A micro codebase that plays around with some mathematically dubious riffs on Newton's law of universal gravitation,
in order to produce some pretty (though unrealistic) animations:

![Demo](demo.gif?raw=true "Demo")

Note: actual animation is smoother than what's shown in this screenshot.

## Trying it Out
Clone the repo, then run:

```shell
$ clj -i init.clj -r
```

Look for the Java GUI window and bring it into focus; an initial random simulation will be running in it.  Press
any key to close the window.

To run another random simulation, with between 3 and 25 randomly located objects:

```clojure
(gg/simulate width height (gen-random-objs 3 25))
```

## Code Structure

The code is divided into 2 primary namespaces: `gravity.core` and `gravity.gui`.

### gravity.core

This ns contains pure logic for calculating the pseudo-gravitational effects a sequence of 'objects' has on one
another.  Objects are simple maps with these keys (all of which are unitless numerical values):

  * `:x` - x location
  * `:y` - y location
  * `:x-vel` - velocity in the horizontal dimension (optional, defaults to 0)
  * `:y-vel` - velocity in the vertical dimension (optional, defaults to 0)
  * `:mass` - mass of the object (optional, defaults to 1)

To step the objects, based on the (fudged) forces they exert on one another, the function `gravity.core/step-simul` may
be called.  The first, mandatory, parameter is the sequence of objects to use as input, and it returns an equivalent
sequence with all of the objects positions and velocities updated.

The remaining, optional, parameters allow the caller to specify that the objects "bounce" at the edge of a given bounding
box.  These parameters are:

  2. `bounce-at-edge?` - boolean indicating whether edge bouncing is enabled or not
  3. `min-x` - the minimum value of the horizontal dimension of the bounding box
  4. `min-y` - the minimum value of the vertical dimension of the bounding box
  5. `max-x` - the maximum value of the horizontal dimension of the bounding box
  6. `max-y` - the maximum value of the vertical dimension of the bounding box

### gravity.gui

This ns contains the logic related to displaying a simulation in a GUI window, currently via the
[`clojure2d` library](https://github.com/Clojure2D/clojure2d).  The primary function here is `gravity.gui/simulate`,
which takes these parameters:

  1. `width` - the width of the window, in pixels
  2. `height` - the height of the window, in pixels
  3. `objs` - the sequence of objects to use in the simulation

The window runs independently of the calling thread, and terminates when the window is closed via OS mechanisms, or
when any key is pressed when the window has focus.

Note that in theory this means you can have multiple simulation windows open concurrently, though this hasn't been
tested.  YMMV!

### init.clj

The `init.clj` script includes one function that might come in handy: `gen-random-objs`.  As the name suggests, this
creates a random number of randomly located objects, with random velocities.  It takes two parameters:

  1. `mini` - the minimum number of objects to create (inclusive)
  2. `maxi` - the maximum number of objects to create (inclusive)

Note that currently this function uses global vars that are defined earlier in the `init.clj` script, so it won't
function unless that script has been used to initialise your REPL (yes this is lame, yes it should be fixed...).

## Contributor Information

[GitHub project](https://github.com/pmonks/gravity)

[Bug Tracker](https://github.com/pmonks/gravity/issues)

## License

Copyright © 2019 Peter Monks Some Rights Reserved

[![Creative Commons License](https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png)](http://creativecommons.org/licenses/by-nc-sa/4.0/)

This work is licensed under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-nc-sa/4.0/).

SPDX-License-Identifier: [CC-BY-NC-SA-4.0](https://spdx.org/licenses/CC-BY-NC-SA-4.0.html)
