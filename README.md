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



Big next tasks

GOAL THIS WEEK: have codelets removing as well as adding to WM

* more codelets to create/try stuff, I get into a local maxima
* add a test-block after a random-block?
* add a dismantler codelet to dismantle a low-used block (step 7, p233 of paper)
* add a compare-to-target codelet which creates secondary targets (if close). activate-node on the secondary target (step 6, p232 of paper)
* add a tie-block-to-secondary-target codelet which takes a block and connects it to the secondary target?
* add a check-target-achieved codelet

see also p234 of paper for more. I think we could do more target comparisons (B)

* start making stuff to eval and remove blocks from the WM
* add more tests to rand-syntactic-comparison
* HOW DO I REPRESENT SECONDARY TARGETS
* cl/rand-op can in theory pick the same brick twice, which would be bad but could just result in the created block being considered invalid later. To fix this, I think I need a version of the random sampler which returns a sequence instead of a single value. This is bad because an active brick will likely get picked twice.

Tidy-ups/small improvements

* remove redundant (do) per https://www.quora.com/Why-and-when-should-I-use-the-do-expression-in-Clojure-instead-of-just-writing-multiple-expressions-sequentially
* Allow provision of a random seed to make entire runs reproducible
* work out how to make scroll bars appear on canvas
* In pn/activate-node, take into account the weights of nodes
* Rationale wm/new-node and cl/new-codelet - are they eerily similar?
* cl/rand-op can multiply by 1, which doesn't seem so useful
- input target and initial bricks in the visualizer
- write function to kick off a run then display it
- Why does activation of :times not spread? Because it's got no pnet links

