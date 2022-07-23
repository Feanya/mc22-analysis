# Introduction

This software component is a toolbox written to conduct analyses on jars in Maven Central.
It is derived from https://github.com/sse-labs/fachproject-metrics-framework
and builds heavily on OPAL version 4.0.0: https://github.com/opalj/OPAL.

The toolbox is open source under MIT License.

# Build
You can build the project using sbt assembly:
```sbt assembly```

or even better:
```sbt clean update assembly```


The resulting jar can be found in `target/scala-2.12/`.



# Run
You can run the jar like this:
```java -jar target/scala-2.12/mc22-analysis-assembly-0.2.0.jar [ARGS] ```

There are three main tasks, you can run one of them or several of them:
  1. `--import` Import (not implemented yet)
  2. `--run-analysis` Runs the implemented analyses (atm just DeprecationAnalysis)
  3. `--evaluate` Runs queries against a database with analysis results and writes result csvs to `results/`.


TODO: More examples

## Run multiple instances parallel (experimental!)
`run.sh` gives an example for how to run multiple threads with the analysis software.
Note that this is experimental and not written in the most elegant way.
