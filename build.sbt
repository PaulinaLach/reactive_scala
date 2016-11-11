name := """akka-scala-seed"""

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  // Change this to another test framework if you prefer
  "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test",
  // Akka
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.11",
  //"com.typesafe.akka" %% "akka-remote" % "2.3.5",
  "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.11" % "test",
  "com.typesafe.akka" %% "akka-persistence" % "2.4.12",
  "org.iq80.leveldb"            % "leveldb"          % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8"
)
