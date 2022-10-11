ThisBuild / organization := "io.integralla"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / startYear := Some(2022)
ThisBuild / versionScheme := Some("semver-spec")

lazy val scala2_13 = "2.13.9"
lazy val scala3 = "3.2.0"
lazy val supportedScalaVersions = List(scala2_13, scala3)

ThisBuild / scalaVersion := scala3

lazy val root = (project in file("."))
  .settings(
    name := "LRS Model",
    normalizedName := "integralla-lrs-model",
    libraryDependencies ++= loggingDependencies ++ testDependencies ++ Seq(
      dependencies.circeCore,
      dependencies.circeGeneric,
      dependencies.circeParser,
      dependencies.scalaUri
    ),
    crossScalaVersions := supportedScalaVersions,
    scalacOptions ++= {
      Seq(
        "-encoding",
        "UTF-8",
        "-feature",
        "-language:implicitConversions",
//        "-Xfatal-warnings"
      ) ++
        (CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((3, _)) => Seq(
            "-unchecked",
            "-source:3.0-migration"
//            "-new-syntax"
          )
          case _ => Seq(
            "-deprecation",
            "-Xfatal-warnings",
            "-Wunused:imports,privates,locals",
//            "-Wvalue-discard"
          )
        })
    }
  )

/**
 * DEPENDENCIES
 */

lazy val versions = new {
  val circe = "0.14.3"
  val logback = "1.4.3"
  val scalactic = "3.2.12"
  val scalaLogging = "3.9.5"
  val scalatest = "3.2.12"
  val scalaUri = "4.0.2"

  val integrallaTest = "0.1.0-SNAPSHOT"
}

lazy val dependencies = new {
  val circeCore = "io.circe" %% "circe-core" % versions.circe
  val circeGeneric = "io.circe" %% "circe-generic" % versions.circe
  val circeParser = "io.circe" %% "circe-parser" % versions.circe
  val logbackClassic = "ch.qos.logback" % "logback-classic" % versions.logback
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % versions.scalaLogging
  val scalaUri = "io.lemonlabs" %% "scala-uri" % versions.scalaUri

  val integrallaTest = "io.integralla" %% "integralla-test" % versions.integrallaTest
}

/* Logging Dependencies */
lazy val loggingDependencies = Seq(
  dependencies.scalaLogging,
  dependencies.logbackClassic
)

/* Testing Dependencies */
lazy val testDependencies = Seq(
  dependencies.integrallaTest
).map(_ % "test")