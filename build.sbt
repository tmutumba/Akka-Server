name := "EchoServer_Akka"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.21",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.21",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
