name := """tgamebot"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
	"com.github.mukel" %% "telegrambot4s" % "v1.2.1",
	"com.typesafe" % "config" % "1.3.0",
  "com.typesafe.akka" %% "akka-stream" % "2.4.8"
)

mainClass in Compile := Some("avelier.tgamebot.Main")
