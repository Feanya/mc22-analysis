# Introduction

This software component is a toolbox written to conduct analyses on jars in Maven Central.
It is derived from
and builds heavily on OPAL.

The toolbox is licensed under CC-BY-NC 4.0.

# Build
You can build the project using sbt assembly:
```sbt assembly```

The resulting jar can be found in `target/scala-2.12/`

or even better:
```sbt clean update assembly```


# Run
You can run the jar like this:
```java -jar target/scala-2.12/mc22-analysis-assembly-0.2.0.jar [ARGS] ```

There are three main tasks, you can run one of them or several of them:
  1. `--import` Import (not implemented yet)
  2. `--run-analysis` Runs the implemented analyses (atm just DeprecationAnalysis)
  3. `--evaluate` Runs queries against a database with analysis results and writes result csvs to `results/`.


## Run multiple instances parallel (experimental!)
`run.sh` gives an example for how to run multiple threads with the analysis software.
Note that this is experimental and not written in the most elegant way.
