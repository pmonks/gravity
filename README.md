
# gravity

A micro codebase that plays around with some mathematically dubious riffs on Newton's law of universal gravitation,
in order to produce some pretty (though unrealistic) animations.

## Trying it Out
Clone the repo, then run:

```shell
$ clj -i test.clj -r
```

Look for the Java GUI window, bring it into focus, and after 5 seconds an initial random simulation will start.  Press
any key to close the window and be dropped into a REPL.

To run another random simulation, with between 3 and 25 randomly located objects:

```clojure
(gg/simulate width height (gen-random-objs 3 25))
```

You may redefine `width` and/or `height` (both integers) to obtain a larger window (the default is 800x600).

## Contributor Information

[GitHub project](https://github.com/pmonks/gravity)

[Bug Tracker](https://github.com/pmonks/gravity/issues)

## License

Copyright © 2019 Peter Monks Some Rights Reserved

[![Creative Commons License](https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png)](http://creativecommons.org/licenses/by-nc-sa/4.0/)

This work is licensed under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-nc-sa/4.0/).

SPDX-License-Identifier: [CC-BY-NC-SA-4.0](https://spdx.org/licenses/CC-BY-NC-SA-4.0.html)
