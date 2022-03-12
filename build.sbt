
ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.14"


assembly / mainClass := Some("EntryPoint")

lazy val root = (project in file("."))
  .settings(
    name := "mc22-analysis"
  )

libraryDependencies += "com.opencsv" % "opencsv" % "5.6"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.36"

val opalVersion = "4.0.0"
libraryDependencies ++= Seq(
  "de.opal-project" % "common_2.12" % opalVersion,
  "de.opal-project" % "framework_2.12" % opalVersion,
  "de.opal-project" % "hermes_2.12" % opalVersion
)


libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.3.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.3.3"

val akkaVersion = "2.6.18"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % "10.2.8",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
)


