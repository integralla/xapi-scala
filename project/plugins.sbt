resolvers += "Sonatype Nexus Repository Manager" at "https://nexus.devops.integralla.io/repository/maven-public/"

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("com.integralla" % "sbt-integralla-nexus" % "1.0.2")

libraryDependencies += "com.integralla" %% "sbt-integralla" % "1.0.2"