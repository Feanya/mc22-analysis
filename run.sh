#!/bin/bash

java -jar mc22-analysis-assembly-0.1.0-SNAPSHOT.jar -o 0  -l 20 2> p1.log &
java -jar mc22-analysis-assembly-0.1.0-SNAPSHOT.jar -o 20 -l 20 2> p2.log &
java -jar mc22-analysis-assembly-0.1.0-SNAPSHOT.jar -o 40 -l 20 2> p3.log &
java -jar mc22-analysis-assembly-0.1.0-SNAPSHOT.jar -o 60 -l 20 2> p4.log &

wait $(jobs -p)
