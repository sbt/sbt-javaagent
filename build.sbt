/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

// dependencies
val packagerVersion = "1.11.3"
val packager19xVersion = "1.9.16"

val scala212 = "2.12.20"
val scala3 = "3.7.2"

addSbtPlugin(
  "com.github.sbt" % "sbt-native-packager" % packagerVersion % "provided"
)

// compile settings
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature"
)
javacOptions ++= Seq("-encoding", "UTF-8")

// test agent
lazy val maxwell = project
  .in(file("maxwell"))
  .settings(
    name := "maxwell",
    organization := "sbt.javaagent.test",
    autoScalaLibrary := false,
    crossPaths := false,
    packageOptions += Package
      .ManifestAttributes("Premain-Class" -> "maxwell.Maxwell"),
    publish := {}
  )

// plugin module
lazy val `sbt-javaagent` = (project.in(file(".")))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-javaagent",
    organization := "com.github.sbt",
    crossScalaVersions := Seq(scala212, scala3),
    scalacOptions ++= {
      scalaBinaryVersion.value match {
        case "2.12" => Seq("-Xsource:3")
        case _      => Nil
      }
    },
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq(
      "-Dproject.version=" + version.value,
      "-Dpackager.version=" + packagerVersion,
      "-Dpackager.19x.version=" + packager19xVersion
    ),
    scriptedDependencies := {
      (maxwell / publishLocal).value
      publishLocal.value
    },
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.11.6"
        case _      => "2.0.0-RC4"
      }
    },
    scriptedSbt := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.11.6"
        case _      => (pluginCrossBuild / sbtVersion).value
      }
    }
  )

// publish settings
licenses += "Apache-2.0" -> url(
  "http://www.apache.org/licenses/LICENSE-2.0.html"
)
scmInfo := Some(
  ScmInfo(
    url("https://github.com/sbt/sbt-javaagent"),
    "scm:git:git@github.com:sbt/sbt-javaagent.git"
  )
)
homepage := scmInfo.value.map(_.browseUrl)
developers := List(
  Developer(
    "contributors",
    "Contributors",
    "https://github.com/sbt/sbt-javaagent/discussions",
    url("https://github.com/sbt/sbt-javaagent/graphs/contributors")
  )
)
