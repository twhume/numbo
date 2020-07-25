# numbo

Goal: recreate Hofstadter’s Numbo in Clojure
See Fluid Concepts & Creative Analogies, p.131 onwards (Chapter 3)

## Usage

lein run - kicks off and starts the GUI
lein test - unit tests
lein repl - loads all code, run (-main) to trigger the GUI and leave a REPL open

## License

Copyright © 2020 Tom Hume

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.

* Make the current iteration an input field which you can put a value into

* At iteration 302 we have a main block, needing a 6, and get a 6 from 4-1=3
* 1. Our pnet activation rules activate along every axis - i.e. activate 6, then minus and *every* minus
* calc gets activated. We could be more selective.
* 2. Now we have a 6 and a block needing 6. what happens? probe-target2 walks past it at 304... we should be completing the block here!

- add more rules to rand-syntactic-comparison
* UPDATE THE temperature calculator
* cy/-format-block can't handle nested blocks
* drop-off in activation


See debugging notes


see also p234 of paper for more. I think we could do more target comparisons (B)


Tidy-ups/small improvements

* work out how to make scroll bars appear on canvas
* In pn/activate-node, take into account the weights of nodes
* Rationale wm/new-node and cl/new-codelet - are they eerily similar?
* cl/rand-op can multiply by 1, which doesn't seem so useful
- input target and initial bricks in the visualizer
- Why does activation of :times not spread? Because it's got no pnet links

