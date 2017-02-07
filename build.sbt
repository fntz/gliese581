
name := "gliese581"

version := "0.0.1"



val options = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-Yshow-trees",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-language:reflectiveCalls",
  "-unchecked",
  "-Xcheckinit",
  "-Xverify",
  "-Xfuture"
)



val rethinkDriver = "com.rethinkdb" % "rethinkdb-driver" % "2.3.3"

val meta = "org.scalameta" %% "scalameta" % "1.4.0"

lazy val macroSettings: Seq[Def.Setting[_]] = Seq(
  resolvers += Resolver.sonatypeRepo("releases"),
  resolvers += "scalameta" at "https://dl.bintray.com/scalameta/maven",
  addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-beta4" cross CrossVersion.full),
  scalacOptions += "-Xplugin-require:macroparadise",
  scalacOptions in (Compile, console) := Seq(),
  sources in (Compile, doc) := Nil
)


lazy val macro_level = (project in file("macro")).settings(
  macroSettings,
  scalaVersion := "2.11.8",
  scalacOptions ++= options,
  libraryDependencies ++= Seq(rethinkDriver, meta)
)

lazy val core = (project in file(".")).settings(
  macroSettings,
  scalaVersion := "2.11.8",
  scalacOptions ++= options,
  libraryDependencies += rethinkDriver
) dependsOn macro_level


