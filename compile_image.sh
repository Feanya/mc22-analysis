#!/bin/bash

mv target/scala-2.12/mc22-analysis-assembly-0.1.0-SNAPSHOT.jar ./
tar czf mc-analysis.tar Dockerfile mc22-analysis-assembly-0.1.0-SNAPSHOT.jar run.sh
