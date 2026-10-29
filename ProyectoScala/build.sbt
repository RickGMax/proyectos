ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.2"

lazy val root = (project in file("."))
  .settings(
    name := "cc3002-project",
    idePackagePrefix := Some("cl.uchile.dcc")
  )

libraryDependencies += "org.scalameta" %% "munit" % "1.3.5" % Test
