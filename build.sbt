name := "structurizr-dsl"

version := "0.1"

scalaVersion := "2.12.4"

// https://mvnrepository.com/artifact/com.structurizr/structurizr-core
libraryDependencies += "com.structurizr" % "structurizr-core" % "1.0.0-RC4"
libraryDependencies += "com.structurizr" % "structurizr-dot" % "1.0.0-RC4"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6", //just specified to eliminate sbt warnings
  "org.slf4j" % "slf4j-nop" % "1.7.25" % Test,
  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test
)

mainClass in (Compile, run) := Some("PlainStructurizr")