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


* WHY IS NOTHING GETTING ACTIVATED
* PNET CONTAINS NO - CALCULATIONS


Is activation an issue?
* Complete create-secondary-target - add a new block, then activate the secondary bit
-- ARGH how do we ensure efforts to generate that secondary target?
-- 1. Pump the Pnet for this target2 immediately, and for secondary targets in repeatedly, to generate ops which might lead to them
* I can't see a secondary target ever getting pumped - are they not lasting long enough? Tried lengthening decays

See debugging notes


-- 2. Have a codelet which tries to match a new block to a target2
-- 3. have the syntactic test also look at target2s


Q: can secondary nodes themselves have secondary nodes?

* add a test-block after a random-block?
* add a tie-block-to-secondary-target codelet which takes a block and connects it to the secondary target?
* add a check-target-achieved codelet

see also p234 of paper for more. I think we could do more target comparisons (B)

* skipping through steps in the vizualizer is super slow. Have an accelerator to go to the end, also am I repainting too often? - need to keep track of the current tab and only repaint that?
* start making stuff to eval and remove blocks from the WM
* add more tests to rand-syntactic-comparison
* HOW DO I REPRESENT SECONDARY TARGETS
* cl/rand-op can in theory pick the same brick twice, which would be bad but could just result in the created block being considered invalid later. To fix this, I think I need a version of the random sampler which returns a sequence instead of a single value. This is bad because an active brick will likely get picked twice.

Tidy-ups/small improvements

* remove redundant (do) per https://www.quora.com/Why-and-when-should-I-use-the-do-expression-in-Clojure-instead-of-just-writing-multiple-expressions-sequentially
* work out how to make scroll bars appear on canvas
* In pn/activate-node, take into account the weights of nodes
* Rationale wm/new-node and cl/new-codelet - are they eerily similar?
* cl/rand-op can multiply by 1, which doesn't seem so useful
- input target and initial bricks in the visualizer
- write function to kick off a run then display it
- Why does activation of :times not spread? Because it's got no pnet links

