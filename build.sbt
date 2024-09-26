/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

// sbt cross build
crossSbtVersions := Seq("1.2.8")

// dependencies
val packagerVersion = "1.10.4"
val packager10xVersion = "1.0.6"
val packager11xVersion = "1.1.6"
val packager12xVersion = "1.2.2"
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
    publish / skip := true
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
      "-Dpackager.10x.version=" + packager10xVersion,
      "-Dpackager.11x.version=" + packager11xVersion,
      "-Dpackager.12x.version=" + packager12xVersion
    ),
    scriptedDependencies := {
      (maxwell / publishLocal).value
      publishLocal.value
    },
    Test / test := {
      (Test / test).value
      scripted.toTask("").value
    },
    scriptedTests / resourceDirectory := sourceDirectory.value / "sbt-test",
    scriptedTests / resourceDirectories := Seq(
      (scriptedTests / resourceDirectory).value
    ),
    scriptedTests / resourceDirectories += sourceDirectory.value / ("sbt-test-" + (pluginCrossBuild / sbtBinaryVersion).value),
    scriptedTests / includeFilter := AllPassFilter,
    scriptedTests / excludeFilter := HiddenFileFilter,
    scriptedTests / resources := Defaults
      .collectFiles(
        scriptedTests / resourceDirectories,
        scriptedTests / includeFilter,
        scriptedTests / excludeFilter
      )
      .value,
    scriptedTests / target := crossTarget.value / "sbt-test",
    scriptedTests / copyResources := {
      val testResources = (scriptedTests / resources).value
      val testDirectories = (scriptedTests / resourceDirectories).value
      val testTarget = (scriptedTests / target).value
      val cacheFile = streams.value.cacheDirectory / "copy-sbt-test"
      val mappings = (testResources --- testDirectories) pair (Path.rebase(
        testDirectories,
        testTarget
      ) | Path.flat(testTarget))
      Sync.sync(sbt.util.CacheStore(cacheFile))(mappings)
      mappings
    },
    sbtTestDirectory := (scriptedTests / target).value
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
