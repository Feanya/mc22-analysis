#!/bin/bash

sbt clean update assembly

echo "Last compile:"
date -r target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar "+%F %R"

OFFSET=0
MAX=$((100000+OFFSET))
STEP=100
SIZE=10
DIFF=$((STEP/2))

#for ((i=OFFSET;i<MAX;i=i+STEP))
#do
    #java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $i --limit $STEP --size $SIZE  2> p.log  &
#    j=$((i+DIFF))
#    java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $i --limit $DIFF --size $SIZE  2> p1.log  &
#    java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $j --limit $DIFF --size $SIZE  2> p2.log  &
#wait $(jobs -p)
#date "+%F %R"
#echo $i "/" $MAX
#done

OFFSET=100000
MAX=$((100000+OFFSET))

for ((i=OFFSET;i<MAX;i=i+STEP))
do
    #java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $i --limit $STEP --size $SIZE  2> p.log  &
    j=$((i+DIFF))
    java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $i --limit $DIFF --size $SIZE  2> p1.log  &
    java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $j --limit $DIFF --size $SIZE  2> p2.log  &
wait $(jobs -p)
date "+%F %R"
echo $i "/" $MAX
done

OFFSET=200000
MAX=$((100000+OFFSET))

for ((i=OFFSET;i<MAX;i=i+STEP))
do
    #java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $i --limit $STEP --size $SIZE  2> p.log  &
    j=$((i+DIFF))
    java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $i --limit $DIFF --size $SIZE  2> p1.log  &
    java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $j --limit $DIFF --size $SIZE  2> p2.log  &
wait $(jobs -p)
date "+%F %R"
echo $i "/" $MAX
done

OFFSET=300000
MAX=$((100000+OFFSET))

for ((i=OFFSET;i<MAX;i=i+STEP))
do
    #java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $i --limit $STEP --size $SIZE  2> p.log  &
    j=$((i+DIFF))
    java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $i --limit $DIFF --size $SIZE  2> p1.log  &
    java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar  --env --offset $j --limit $DIFF --size $SIZE  2> p2.log  &
wait $(jobs -p)
date "+%F %R"
echo $i "/" $MAX
done





##java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar --env -o 2000 -l 1000 -s 100  2> p2.log  &
##java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar --env -o 4000 -l 1000 -s 100 2> p3.log  &
##java -jar target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar --env -o 6000 -l 1000 -s 100 2> p4.log  &

wait $(jobs -p)
echo "✨ Done! Shutting down in 10 minutes. ✨"
sleep 600
