import com.integralla.sbt.CompilerOptions

ThisBuild / organization := "io.integralla"
ThisBuild / startYear := Some(2022)

ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / versionScheme := Some("semver-spec")

lazy val scala213 = "2.13.12"
lazy val scala3 = "3.3.1"
lazy val supportedScalaVersions = List(scala213, scala3)

ThisBuild / scalaVersion := scala3

/** PROJECTS */

lazy val root = (project in file("."))
  .enablePlugins(IntegrallaNexus)
  .settings(
    name := "integralla-lrs-models",
    description := "Data model for Experience API (xAPI) resources",
    libraryDependencies ++= loggingDependencies ++ testDependencies ++ projectDependencies,
    crossScalaVersions := supportedScalaVersions,
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _))            => CompilerOptions.common ++ CompilerOptions.scala3OldSyntax
        case Some((2, n)) if n == 13 => CompilerOptions.common ++ CompilerOptions.scala213
        case _                       => CompilerOptions.common
      }
    },
//    Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)
  )

/** DEPENDENCIES */

/** Dependency Versions */
lazy val versions = new {
  val circe = "0.14.5"
}

/** Project Dependencies */
lazy val projectDependencies = Seq(
  "io.circe" %% "circe-core" % versions.circe,
  "io.circe" %% "circe-generic" % versions.circe,
  "io.circe" %% "circe-parser" % versions.circe,
  "io.lemonlabs" %% "scala-uri" % "4.0.3",
  "net.time4j" % "time4j-base" % "5.9.2"
)

/* Logging Dependencies */
lazy val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.22.1" % "test",
  "org.slf4j" % "slf4j-api" % "2.0.11" % "test"
)

/* Testing Dependencies */
lazy val testDependencies = Seq(
  "io.integralla" %% "integralla-test" % "1.0.0-SNAPSHOT" % "test"
)
