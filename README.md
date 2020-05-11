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



TODO list


Big next tasks

* add temperature calculation
* HOW DO I REPRESENT SECONDARY TARGETS
* start using target 114, blocks 11 20 7 1 6 as per example
* Add in more of the PNet
* --- THE ABOVE TAKES US TO STEP 4 OF THE SAMPLE RUN ON P142 WOOOO ---
* start making stuff to eval and remove blocks from the WM

Tidy-ups/small improvements

* In pn/activate-node, take into account the weights of nodes
* Rationale wm/new-node and cl/new-codelet - are they eerily similar?
* cl/rand-op can in theory pick the same brick twice, which would be bad but could just result in the created block being considered invalid later. To fix this, I think I need a version of the random sampler which returns a sequence instead of a single value
- input target and initial bricks in the visualizer
- write function to kick off a run then display it
- Why does activation of :times not spread? Because it's got no pnet links

