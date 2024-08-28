ThisBuild / organization := "io.integralla"
ThisBuild / organizationName := "Integralla LLC"
ThisBuild / organizationHomepage := Some(url("https://integralla.com/"))

ThisBuild / developers := List(
  Developer(
    id = "integralla",
    name = "Andrew Kirk",
    email = "integralla-github.imaging568@passmail.net",
    url = url("https://x.com/IntegrallaIO")
  )
)

ThisBuild / homepage := Some(url("https://github.com/integralla/xapi-scala"))
ThisBuild / licenses := List(
  "Apache-2.0" -> new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")
)
ThisBuild / publishTo := sonatypeCentralPublishToBundle.value
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/integralla/xapi-scala"),
    "scm:git@github.com:integralla/xapi-scala.git"
  )
)
ThisBuild / startYear := Some(2024)
ThisBuild / version := "1.0.0"
ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / scalaVersion := "3.4.2"
ThisBuild / crossScalaVersions := List("2.13.14", scalaVersion.value)

ThisBuild / scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _))            => compilerOptions.common ++ compilerOptions.scala3
    case Some((2, n)) if n == 13 => compilerOptions.common ++ compilerOptions.scala213
    case _                       => compilerOptions.common
  }
}

/* PROJECTS */

lazy val root = project
  .in(file("."))
  .settings(
    name := "xapi-scala",
    description := "Experience API (xAPI) data model",
    libraryDependencies ++= projectDependencies
  )

/* DEPENDENCIES */

lazy val versions = new {
  val circe = "0.14.9"
}

lazy val projectDependencies = Seq(
  "io.circe" %% "circe-core" % versions.circe,
  "io.circe" %% "circe-generic" % versions.circe,
  "io.circe" %% "circe-parser" % versions.circe,
  "io.lemonlabs" %% "scala-uri" % "4.0.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.23.1" % "test",
  "org.slf4j" % "slf4j-api" % "2.0.12" % "test",
  "org.scalactic" %% "scalactic" % "3.2.19" % "test",
  "org.scalatest" %% "scalatest" % "3.2.19" % "test",
  "net.time4j" % "time4j-base" % "5.9.4"
)

/* COMPILER OPTIONS */

lazy val compilerOptions = new {
  val common = Seq(
    "-encoding",
    "utf8",
    "-unchecked",
    "-deprecation",
    "-Wvalue-discard"
  )

  val scala3 = Seq(
    "-old-syntax",
    "-explain-types",
    "-Wunused:all"
  )

  val scala213 = Seq(
    "-explaintypes",
    "-Wunused"
  )
}
