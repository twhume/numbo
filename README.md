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
v1.02
- played w/frequencies: dismantler 5->10, pump-target 5->10 pump-brick 5->10; didn't make much difference to speed
- played w/frequencies: dismantler 5->2, pump-target 5->2 pump-brick 5->2; didn't make much difference to speed
- made the random-creator 2 -> 1, everything else back to 5. Lots of failures in the first test
- (2020.07.30-21.34.19) made the random-creator 1 -> 3, everything else on 5 - not much difference, maybe slightly worse
- (2020.07.31-08.11.08) Slower decay, less pumping: cy/DEFAULT_ATTRACTION_DEC from 0.02 -> 0.01, pn/DEFAULT_DECAY 0.1 -> 0.05 core/brick and pump frequency 5->10 . Slows av # iterations but leads to more solutions and an uplift for 87	(8 3 9 10 7)
- Allowed secondary targets to be fulfilled by single bricks, and slowed the decay/pumped less often



Ideas:
- if things decay slower we don't need to pump them so often, so can reduce the activation-refresh and focus on calculations?
- if things decay faster we'll get rid of them quicker so can try new theories out faster!
- break out the different things in random-creator and run them at separate timings
- Break out the noise. If a thing makes less difference, just do it less anyway. (e.g. reduce frequencies as far as poss? map impact of reduction over many runs?)
-  87 , (8 3 9 10 7) never gets ((8 * 10) + 7), always gets (7 + 10) + (8 + 9) - are we not finding single-brick secondary targets?
- 81 , (9 7 2 25 18) never gets (9 * 7) + 18 - same problem? - or ((18 * (7 - 2)) - 9)
-  116 , (20 2 16 14 6) never gets (20 * 6)- (2 + (16 - 14)) - this is a tough one
- 11 , (2 5 1 25 23) never gets (5 * 2) + 1 - another single-brick secondary target
- Start measuring the # of iterations where there's nothing on the coderack - this is a smell that we're underpopulating it/running too cool
- Activation is linear, make it nonlinear.

BUGS
- If "3" is a secondary target in block A, but also a component of block B, when B is dismantled "3" is never returned to the bricks (because we assume it's a secondary target right). ARGH

Solution: when dismantling, count how many times a brick is used in *all* blocks, not the current one


- we have 2 cy/random-target2 methods

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

