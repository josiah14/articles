name := "newsreader"

organization := "com.dstsystems.aag"

version := "0.2"

scalaVersion := "2.11.7"


scalacOptions := Seq("-deprecation", "-feature", "-Yno-adapted-args", "-Ywarn-dead-code", "-Ywarn-infer-any", "-Ywarn-unused-import")

scalacOptions in (Compile, console) := Seq("-deprecation", "-feature", "-Yno-adapted-args", "-Ywarn-dead-code", "-Ywarn-infer-any")

// Scala XML parsing (now an additional module and not part of the standard runtime)
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.5"


// scopt for argument parsing
libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"

resolvers += Resolver.sonatypeRepo("public")


// Akka actors and patterns
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.0"

libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % "2.4.0"


// Testing dependencies
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % Test

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % Test

libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test"


// Typesafe Config
libraryDependencies += "com.typesafe" % "config" % "1.3.0"


// Goose article scraper
libraryDependencies += "com.syncthemall" % "goose" % "2.1.25"


// Elastic Client
libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-core" % "1.7.3"


// json4s - used for more fine-grained control over serialization into elastic
libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0"


// Stanford CoreNLP
libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2"

libraryDependencies += "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2" classifier "models"

// Redis client
libraryDependencies ++= Seq(
    "net.debasishg" %% "redisclient" % "3.0"
)

// HTTP client
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.1"

