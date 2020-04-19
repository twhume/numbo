# numbo

Goal: recreate Hofstadter’s Numbo in Clojure
See Fluid Concepts & Creative Analogies, p.131 onwards (Chapter 3)

## Usage

FIXME

## License

Copyright © 2020 FIXME

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

* Finish implementing cl/rand-op - the creation of a new brick in wm
* Create a GUI for all this, it's unfathomable.  Write a visualizer for the WM (highlight nodes, connections, additions), PNet (highlight activations); and Coderack (just a table). Let it step through a run (i.e record state at each step and refresh fwd/back); Means recording that history (log WM, CR, PN and the selected codelet each time) and mutating each of the displayable entries into a UI element

Necessary

* In cl/load-brick, TODO set initial attractiveness, function of numerical value (e.g. mod 5, 10 == 0)

Tidy-ups/small improvements

* In pn/activate-node, take into account the weights of nodes
* Rationale wm/new-node and cl/new-codelet - are they eerily similar?
