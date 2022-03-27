// Build info
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.14"

/// Build with assembly
mainClass in assembly := Some("EntryPoint")

assemblyMergeStrategy := {
  case x: String if x.toLowerCase.contains("manifest.mf") => MergeStrategy.discard
  case x: String if x.toLowerCase.contains("module-info.class") => MergeStrategy.discard
  case x: String if x.toLowerCase.endsWith(".conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}

lazy val root = (project in file("."))
  .settings(
    name := "mc22-analysis"
  )

// cli argument parsing
libraryDependencies += "org.sellmerfud" %% "optparse" % "2.2"

// Logging
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.36"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.36"
libraryDependencies += "org.slf4j" % "slf4j-ext" % "1.7.36"

// Opal: Code analysis
val opalVersion = "4.0.0"
libraryDependencies ++= Seq(
  "de.opal-project" % "common_2.12" % opalVersion,
  "de.opal-project" % "framework_2.12" % opalVersion
)

// Version parsing according to semver-standard
libraryDependencies += "io.kevinlee" %% "just-semver" % "0.3.0"

// Testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"


// Communication with Postgres server
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.3.3"


// Http requests
val akkaVersion = "2.6.18"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.2.8",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
)


