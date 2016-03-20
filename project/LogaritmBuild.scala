import java.util.Date

import sbt.Keys._
import sbt.Package.ManifestAttributes
import sbt._

object Truerssbuild extends Build {
  import Libs._

  val setting = Seq(
    resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    resolvers += "JCenter" at "http://jcenter.bintray.com/",
    resolvers += "karussell_releases" at "https://github.com/karussell/mvnrepo",
    resolvers += Resolver.bintrayRepo("truerss", "maven"),
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      //"-Ytyper-debug",
      "-encoding", "UTF-8",
      "-feature",
      "-Xlog-free-terms",
      "-Yshow-trees",
//      "-Xprint-types",
//      "-Xprint:typer",
//      "-Ymacro-debug-lite",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-language:reflectiveCalls",
      "-unchecked",
      "-Xcheckinit",
      "-Xverify",
      "-Xfuture"
    )
  )

  lazy val mainProject = Project(
    id = "logarithm",
    base = file("."),
    settings = setting ++ Seq(
      parallelExecution in Test := false,
      libraryDependencies ++= deps,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
  ) dependsOn subMacro

  lazy val subMacro = Project(
    "macro",
    file("macro"),
    settings = setting ++ Seq(
      libraryDependencies ++= deps,
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
  )


}