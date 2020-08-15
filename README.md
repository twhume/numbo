# numbo

Goal: recreate FARG's Numbo in Clojure

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

(I'm using this spreadsheet to track and analyze runs - the two "Run Analysis" tabs let me do 
side-by-side comparisons)

## Tags and experiments

I'm tagging a new version wherever things get noticeably better, and documenting experiments here.
I'm keeping output of experiments in the runs/ directory

v1.01 first fully running version with automated tests
v1.02
- played w/frequencies: dismantler 5->10, pump-target 5->10 pump-brick 5->10; didn't make much difference to speed
- played w/frequencies: dismantler 5->2, pump-target 5->2 pump-brick 5->2; didn't make much difference to speed
- made the random-creator 2 -> 1, everything else back to 5. Lots of failures in the first test
- (2020.07.30-21.34.19) made the random-creator 1 -> 3, everything else on 5 - not much difference, maybe slightly worse
- (2020.07.31-08.11.08) Slower decay, less pumping: cy/DEFAULT_ATTRACTION_DEC from 0.02 -> 0.01, pn/DEFAULT_DECAY 0.1 -> 0.05 core/brick and pump frequency 5->10 . Slows av # iterations but leads to more solutions and an uplift for 87	(8 3 9 10 7)
- (2020.07.31-14.46.21) Allowed secondary targets to be fulfilled by single bricks, and slowed the decay/pumped less often - GREAT, went from solving 5 to 7 puzzles, run-times were up a little tho. MARKED v1.03

v1.03
- tried lots of runs for "lein run -- -t 116 -b 20,2,16,14,6 -i 10000 -c 10000" - do we ever go for depth 3?
No, we always find something sooner - e.g. ((6 * 20) - (20 - 16)) not (20 * 6)- (2 + (16 - 14)) (see 116-target-10k.csv). 3% failed, 0.5% found ((6 * 20)= (20 - 16)) or equivalent, rest were ((6 * 16) + 20) or equivalent
- random-target2 now samples by attraction instead of pure randomness
- (2020.08.01-08.44.10) 1000 attempts on 81 , (9 7 2 25 18), it never gets there.
- (2020.08.03-22.00.58 v1.03-2-g29b4f1e) updated the worthiness calculator so if there's an activated node near the eval'd value we use it; this lengthens the calculation a lot but gets us 8/10  ( whilst degrading the solution %age for a few). I suspect that pnet activation and overly empty coderacks are a problem now (1/5 of our iterations for this run were nil, many must be just 1 codelet in the rack, so in practice we're never selecting between alternatives). MARKED 1.04

v1.04
- Upped the frequency of events going into the coderack and implemented a decay there so it never has >32 items in it. IT RAN SO SLOW IT HURT - no debugging output at all, tho I could see stuff go past in the logs
- Rewound a bit after getting poor result and started playing with the frequency of doing random stuff
- (2020.08.09-09.03.26) do random stuff every iteration - 8/10 solved, but %ages solved low (1 @ 100%)
- (2020.08.08-12.26.48) every 2 iterations, 8/10 solved and %ages higher (2 @ 100%, 1 @ 95, 1 @ 99)
- (2020.08.09-11.42.53) every 3 iterations, 8/10 solved and still higher (3 @ 100%, 1 @ 99, many others in 80s) - also getting faster
- (2020.08.09-19.38.38) every 5 iterations, 8/10 solved (2 @ 100, 5 in 90s, 1 at 89)
- (2020.08.09-20.27.03) every 10 iterations, 8/10 solved but quality of a few seems to drop off. So it seems like the best is in between
- (2020.08.09-20.59.37) every 7, a few drop, a few gain, but overall slower
- (2020.08.10-07.15.19) seems best balance (MOVED to v1.05)

v1.05


Next: get to something optimal here
- Break out constants into a separate file (DONE)
- codelet urgencies in their own map (DONE)
- fix unit tests (DONE)
- Before this, quickly play with sampling - is it faster for 30 items, say?
- Vary config across runs
- Do a large number of runs playing with parameters, over days
- Also measure impact of disabling logging


## Bugs

* If "3" is a secondary target in block A, but also a component of block B, when B is dismantled "3" is never returned to the bricks (because we assume it's a secondary target right). Solution: when dismantling, count how many times a brick is used in *all* blocks, not the current one?

## Improvements

Strategies:
* if things decay slower we don't need to pump them so often, so can reduce the activation-refresh and focus on calculations?
* counterargument: if things decay faster we'll get rid of them quicker so can try new theories out faster!
* break out the different things in random-creator and run them at separate timings
* Break out the noise. If a thing makes less difference, just do it less anyway. (e.g. reduce frequencies as far as poss? map impact of reduction over many runs?)
* 81 , (9 7 2 25 18) never gets (9 * 7) + 18 - same problem? - or ((18 * (7 - 2)) - 9) (WOULD ADDING 7 * 9 HELP?)
* 31	(3 5 24 3 14) never gets (((5 * 3) * 3) - 14) - complex tho
* 116 , (20 2 16 14 6) never gets (20 * 6)- (2 + (16 - 14)) - but has simpler solutions
* 127	(7 6 4 22 25) never gets ((25 - (6 + 4)) * 7) + 22)
* Start measuring the # of iterations where there's nothing on the coderack - this is a smell that we're underpopulating it/running too cool, and are effectively linear
* Activation is linear, make it nonlinear - i.e important things should be way more likely to get sampled

Viz:
* Make the current iteration an input field which you can put a value into
* work out how to make scroll bars appear on canvas
* input target and initial bricks in the visualizer

PNet:
* Why does activation of :times not spread? Because it's got no pnet links. Seems bad
* In pn/activate-node, take into account the weights of nodes

Cyto:
* add more rules to rand-syntactic-comparison
* pretty sure the temperature calculator could be better
* drop-off in activation
* see also p234 of paper for more. I think we could do more target comparisons (B)
* we have 2 cy/random-target2 methods

Codelets:
* cl/rand-op can multiply by 1, which doesn't seem so useful - never do this
* Lots of boilerplate code here, replace with macro?

General:
* Abstract out the concept of sampling from a distribution more - it's used in cyto, pnet, coderack
and currently implemented via a couple of macros in misc/

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


