name := "Streams"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.2-RC2",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.2-RC2",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.2-RC2",
  "org.typelevel" %% "cats" % "0.4.1"
)
