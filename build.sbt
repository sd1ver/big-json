
ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "ru.github.sd1ver"
ThisBuild / organizationName := "sd1ver"

lazy val http4sVersion = "0.21.7"

lazy val root = (project in file("."))
  .settings(
    name := "big-json",
    libraryDependencies ++= Seq("org.json4s" %% "json4s-jackson" % "3.6.9")
  )