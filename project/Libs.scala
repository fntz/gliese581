import sbt._

object Libs {
  object versions {
    val scalaVersion = "2.11.7"
    val shapelessVersion = "2.1.0"
    val akkaVersion  = "2.3.9"
    val logbackVersion = "1.1.2"
    val log4j2Version = "1.0.0"
    val rethinkVersion = "2.2-beta-1"
  }

  import versions._

  val db = Seq(
    "com.rethinkdb" % "rethinkdb-driver" % rethinkVersion
  )

  val scalaLibs = Seq(
    "org.scala-lang" % "scala-library" % scalaVersion,
    "org.scala-lang" % "scala-reflect" % scalaVersion
  )

  val logs = Seq(
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion
  )

  val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val shapeless = "com.chuusai" %% "shapeless" % "2.2.0"

  val specs = Seq("org.specs2" %% "specs2-core" % "3.7.2" % "test")

  val deps = db ++ logs ++
    Seq(akka, shapeless) ++ scalaLibs ++ specs

}
