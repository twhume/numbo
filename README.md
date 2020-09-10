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

- added 20 + 20 = 40 to pnet which ought to help with 41 5,16,22,25,1 (it did: 6 --> 63%)
- played with pnet deactivation: 0.1, 0.2, 0.9 for nneighbor, neighbor and activated node, decay 0.02
(basically, make highly activated nodes nonlinearly more likely - focus on a single idea more readily)
(also cyto nodes start less attractive, are pumped 0.7, decay more slowly) (worse)
- dropped iterations to 2000, it's more important for us to test fast than be exhaustive. 
- looked into some solutions, really struggled with 31. Tried modifying rand-syntactic-comparison
- we have a ton of wasted iterations where there's nothing in the coderack. Started logging these, they're a smell...
- ok, rewrote get-temperature, started running random operations based on temperature at the same frequency. Coderack more rarely empty. So how important are individual random operations?
- (2020.08.22-12.19.07) disabled rand-block: avg solutions=1, avg time=1213, % solved=48, % blank=30
- (2020.08.22-12.40.49) nothing disabled: avg solutions=2.7, avg time=1351, % solved=43, % blank=15
- (2020.08.22-13.21.54) disabled seek-facsimile: avg solutions=3.1, avg time=1573, % solved=27, % blank=34
- (2020.08.22-13.47.00) disabled rand-target-match: avg solutions=3, avg time=1368, % solved=40, % blank=28
- (2020.08.22-14.21.03) disabled rand-syntactic-comparison: avg solutions=2.8, avg time=976, % solved=, 68, % blank=10
- Conclusions: seek-facsimile seems important for solving. rand-block ensures more exploration at the cost of dead ends. syntactic-comparison is harmful. target-match useful occasionally.
- Next steps: make syntactic-comparison + target-match v rare (1/10?), keep rand-block as-is, seek facsimile as-is
- (2020.08.22-15.00.28) avg solutions=2.8, avg time=1056, % solved=, 63, % blank=29
- Hmm. make rand-syntactic even less frequent? Try again with same config to get a feel for variance
- (2020.08.22-15.47.23) avg solutions=2.8, avg time=1111, % solved=, 61, % blank=30
- (diversion) - can't understand why 81 , (9 7 2 25 18) isnt getting 9 * (7+2) and added a pnet line to try and make this connection. If there's a pnet calculation with the result of a target, we should try and make it no?
- (???) changed seek-facsimile and rand-block to be considered every cycle (rather than every 2): avg solutions=2.6, avg time=1250, % solved=54, , % blank=3.55 - so good for blanks but that's it
- forced a seek-facsimile whenever we regularly pump the pnet+cyto, to be a bit more "intentional" - "go look here"
- (2020.08.23-19.01.00) avg solutions=3.1, avg time=1072, % solved=61, , % blank=29
- tried with both the seek-facsimile when pumping and a random one
- (2020.08.23-20.28.34) little difference, avg solutions=2.5, avg time=1124, %solved=61, % blank=9
- will we just randomly find answers by spending a long time? did 100 runs to 25k iterations each on 31 , (3 5 24 3 14) - no answers anywhere. SO WE NEED NEW CODELETS yay
- OK, made it so rand-block can use sub-blocks
- (2020.09.08-13.07.57) Did a full test. A bit better, avg solutions=3.1, time=989, %age solved=64%, % blank = 19% (TAGGED to v1.06)

v 1.06
- Updating to use samplers
- Turned down FREQ_PUMP_TARGET to 20 to avoid overfocusing on a solution
- (2020.09.09-10.23.47) avg solutions=1.2, avg time=1136, % solved=52, % blank=59
- Turned FREQ_PUMP_TARGET back to 10 - that %age blank is hideous
- (2020.09.10-07.28.07) avg solutions=1.2, avg time=1032, % solved=58, % blank=36

So something about sampling must be broken....

- In seek-facsimile, 20+20=40 maps to 22+22, which cannot be satisfied
- Error is in (map (comp cy/closest-node misc/int-k) (pn/filter-links-for (:links calc) :param))
- This allows the same parameter to be pulled twice - cy/closest-node needs to take multiple parameters
- Rewrote and fixed! 
- (2020.09.10-09.37.51) avg solutions=2.6, avg time=844, % solved=70, % blank=15 - MUCH BETTER
- shuffle inputs to closest-nodes
- (2020.09.10-10.03.20) avg solutions=3.1, avg time=891, % solved=69, % blank=15
- Pinned misc.normalized minimal value to 0.01 so that there's always a chance of getting 0-activated items and when we have nothing but zeros something can be chosen. Got an NPE when running this but can't repro it, seems rare...
- OCCASIONAL NULLPOINTEREXCEPTION. Added some debugging to see what's going on, repro's v occasionally
- (2020.09.10-10.52.25) avg solutions=3.2, avg time=910, % solved=68, % blank=16 - pinning didn't do much
- We were allowing some poor solutions (9 7 2 25 18 --> 9 * 9). Fixed the get-solutions so it checks against
a second count of actual bricks, stored in :original-bricks
- (2020.09.10-12.47.50) avg solutions=2.7, avg time=964, % solved=66, % blank=16 - pretty good

Problems to look at next:
41	(5 16 22 25 1) - tiny success rate. Why is 22+25 not started early and then 5+1 as a target2?
81	(9 7 2 25 18) - never gets 9 * (7+2). Why is 9 * 9 not tried early, 9 as a target2?


No solutions for:
127	(7 6 4 22 25)
31	(3 5 24 3 14)

- Next: move sampler code into its own file



----------



What I'm noticing now: Numbo gets fixated on a possible, reasonable path and doesn't try others
e.g. when trying to get to 31, 3 * 14 = 42 comes up again and again. Just 11 different! But we can't make 11.

Solution: introduce a memory:
1. Whenever you dismantle, add the block you're dismantling to memory (count=1) or up its count
2. Before creating a block, if it exists in memory. If it does, it has a count% (capped at 95) chance of being silently dropped.

## Next

We currently don't solve 31 3 5 24 3 14
5 * 3 * 3 = 45
45 - 14 = 31


Chain of thought
31 is nearly 30, 3 * 5 = half of thirty so try that
3 * 5 = 15
so we have 15,24,3,14 to make 31
15 is less than half of 31 and there's no immediate secondary target, so don't make one

15 * 3 = 45 
so we have 45, 24, 14 to make 31
secondary target = 14 which we have!


seek-facsimile should include not just bricks, but blocks, as inputs
e.g. once we have 3 * 5 = 15
seek-facsimile 3 * 10 = 30
could get to (3 * 5) * 5 = 45


Specific changes:
- seek-facsimile to consider blocks as well as bricks
-- It should be doing this already (via cy/closest-node) but the subsequent check of brick-free? may kill these opportunities.

## Next 2

We need to consider bricks as starting points to calculations, not just blocks, to cover the 9 * (7 + 2) = 81 case
If seek-facsimile gets a brick 9, it should make a block 9 * 9, with that second 9 being a secondary target. This should stimulate a 7+2 to occur.
To do this

- seek-facsimile --> make-block (brick1 op)
- make-block sees if we can make a block using brick1 and op, sets it up, adds brick2 as 
a secondary target

##


******

- LINE 297 onwards in codelet.clj is breaking everything and I don't know why
- just make a new set of codelets to fire

- update this section to call probe-target2 with 

******

- Get solutions for the real nasties that evade us - work out how to solve these even if slowly
* 31	(3 5 24 3 14) never gets (((5 * 3) * 3) - 14) - complex tho
- path could be: 3 is like 31, so pump. 3 * 5 = 15, which is half 30, so pump. 15 * 3 = 45 which is near 31, so keep it. Should seek-facsimile be working on blocks, so once we have 15 it's simple?
* 127	(7 6 4 22 25) never gets ((25 - (6 + 4)) * 7) + 22)


- let single bricks be blocks that seek secondary targets, if they're within 50% of the solution
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


