# numbo

Goal: recreate Hofstadter’s Numbo in Clojure
See Fluid Concepts & Creative Analogies, p.131 onwards (Chapter 3)

## Usage

lein run - kicks off and starts a run

lein run -- -t 114 -b 1,6,7,11,20 -i 1000 -c 10
-t : target value
-b : bricks to use to make this value
-i : number of iterations to run a single calculation before giving up
-c : number of times to run this target/bricks combo

lein test - unit tests
lein repl - loads all code, run (-main) to trigger the GUI and leave a REPL open

Dump CSV output at the end of the Raw data tab of
https://docs.google.com/spreadsheets/d/1WZ5VMy2MDzJz80Jh93ApQpbrfS-tuu16n_Xqfur-Egk/edit#gid=0

## Tags

v1.01 first fully running version with automated tests

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


* I notice that when I change the target from 114 to 97, no solution is found - but (7 * 11) + 20 would work
* - actually, a solution is found at iteration 17036(!) - but there's an issue here for sure.
* Next step: make 10 problems, benchmark them 100 times and work from here


* Make the current iteration an input field which you can put a value into

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

