#!/bin/sh

# Script to run the set of canonical problems from the Numbo paper

ITERATIONS=5000
COUNT=100


current_time=$(date "+%Y.%m.%d-%H.%M.%S")
TAG=`git describe --tags`
ARGS="-i $ITERATIONS -c $COUNT"
OUT="runs/run-$TAG-$current_time.csv"
CMD="lein run --"
PRE="s/^/$current_time,$TAG,/"

# main example of the paper
$CMD -t 114 -b 11,20,7,1,6 $ARGS | sed $PRE >> $OUT
$CMD -t 87 -b 8,3,9,10,7 $ARGS | sed $PRE >> $OUT
$CMD -t 31 -b 3,5,24,3,14 $ARGS | sed $PRE >> $OUT
$CMD -t 25 -b 8,5,5,11,2 $ARGS | sed $PRE >> $OUT
$CMD -t 81 -b 9,7,2,25,18 $ARGS | sed $PRE >> $OUT
$CMD -t 6 -b 3,3,17,11,22 $ARGS | sed $PRE >> $OUT
$CMD -t 11 -b 2,5,1,25,23 $ARGS | sed $PRE >> $OUT
$CMD -t 116 -b 20,2,16,14,6 $ARGS | sed $PRE >> $OUT
$CMD -t 127 -b 7,6,4,22,25 $ARGS | sed $PRE >> $OUT
$CMD -t 41 -b 5,16,22,25,1 $ARGS | sed $PRE >> $OUT


