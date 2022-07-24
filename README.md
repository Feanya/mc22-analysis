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

# Dependencies
You will need:
  * a PostgreSQL database (tested on PostgreSQL 14)
  * Scala: version 2.12

other dependencies are specified in `build.sbt`:
  * org.sellmerfud:optparse 
  * org.slf4j:slf4j-api 
  * org.slf4j:slf4j-simple
  * org.slf4j:slf4j-ext
  * org.scalatest:scalatest
  * io.kevinlee:just-semver

Opal: 
  * Opal version 4.0.0
  * de.opal-project:common_2.12
  * de.opal-project:framework_2.12

For postgres-interaction and persistance:
  * com.typesafe.slick:slick
  * org.postgresql:postgresql
  * com.opencsv:opencsv

For downloading of artifacts:
  * com.typesafe.akka:akka-actor 
  * com.typesafe.akka:akka-slf4j
  * com.typesafe.akka:akka-http
  * com.typesafe.akka:akka-stream



# Connection to database
You need to provide database credentials in one of two forms:
  1. via ENV-variables
  2. via config-file 

## via ENV
Add the flag `--env` to your call and provide three environment variables as follows:
  * `POSTGRES_URL=jdbc:postgresql://<host>:<port>/<databasename>;`
  * `POSTGRES_USERNAME`
  * `POSTGRES_PASSWORD`


## via config-file

TODO add instructions and example


# Run
You can run the jar like this:
```java -jar target/scala-2.12/mc22-analysis-assembly-0.2.0.jar [ARGS] ```

There are three main tasks, you can run one of them or several of them:
  1. `--import` Import (not implemented yet)
  2. `--run-analysis` Runs the implemented analyses (atm just DeprecationAnalysis)
  3. `--evaluate` Runs queries against a database with analysis results and writes result csvs to `results/`.

Note the several CLI arguments:

  * `-c, --clean` Remove old results before starting analysis.
  * `-d, --dryrun` Do not download jars and conduct analyses
  * `-e, --env` Read environment variables for postgres-credentials.
  * `-v, --verbose` Write more things to stderr
  * `-g <ga-coordinate>, --library` Enter specific library/GA to analyse in the form of <G:A> 
  * `-o <offset>, --offset` Enter offset of libraries/GA.
  * `-l <limit>, --limit` Enter limit of libraries/GA to pull.
  * `-s <chunksize>, --size` Enter limit of libraries/GA to work through in one chunk.


## Analyze one component/library
A typical call for testing purposes looks like this:
```java -jar target/…/mc22-analysis-assembly-0.2.0.jar --run-analysis -g org.mockito:mockito-core```

TODO: More examples

## Run multiple instances parallel (experimental!)
`run.sh` gives an example for how to run multiple threads with the analysis software.
Note that this is experimental and not written in the most elegant way.
