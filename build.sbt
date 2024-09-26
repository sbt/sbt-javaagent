/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

// sbt cross build
crossSbtVersions := Seq("1.2.8")

// dependencies
val packagerVersion = "1.10.4"
val packager19xVersion = "1.9.16"

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
lazy val `sbt-javaagent` = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-javaagent",
    organization := "com.github.sbt",
    scriptedBufferLog := false,
    scriptedLaunchOpts ++= Seq(
      "-Dproject.version=" + version.value,
      "-Dpackager.version=" + packagerVersion,
      "-Dpackager.19x.version=" + packager19xVersion
    ),
    scriptedDependencies := {
      (maxwell / publishLocal).value
      publishLocal.value
    }
  )

// publish settings
publishMavenStyle := true
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
publishTo := sonatypePublishTo.value

// release settings
import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("^ scripted"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("^ publishSigned"),
  releaseStepCommand("sonatypeReleaseAll"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
