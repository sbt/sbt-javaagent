/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

val packagerVersion = "1.3.19"
val packager10xVersion = "1.0.6"
val packager11xVersion = "1.1.6"
val packager12xVersion = "1.2.2"

lazy val `sbt-javaagent` =
  project.in(file("."))
    .enablePlugins(SbtPlugin)
    .settings(
      name := "sbt-javaagent",
      organization := "com.github.sbt",

      // sbt cross build
      crossSbtVersions := Seq("0.13.18", "1.2.8"),

      // dependencies
      addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % packagerVersion % "provided"),

      // compile settings
      scalacOptions ++= Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature") ++ {
        if ((pluginCrossBuild / sbtBinaryVersion).value == "0.13") Seq("-target:jvm-1.6") else Seq.empty
      },
      javacOptions ++= Seq("-encoding", "UTF-8") ++ {
        if ((pluginCrossBuild / sbtBinaryVersion).value == "0.13") Seq("-source", "1.6", "-target", "1.6") else Seq.empty
      },

      // test settings
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
      // cross-sbt scripted tests
      scriptedTests / resourceDirectory := sourceDirectory.value / "sbt-test",
      scriptedTests / resourceDirectories := Seq((scriptedTests / resourceDirectory).value),
      scriptedTests / resourceDirectories += sourceDirectory.value / ("sbt-test-" + (pluginCrossBuild / sbtBinaryVersion).value),
      scriptedTests / includeFilter := AllPassFilter,
      scriptedTests / excludeFilter := HiddenFileFilter,
      scriptedTests / resources := Defaults.collectFiles(scriptedTests / resourceDirectories, scriptedTests / includeFilter, scriptedTests / excludeFilter).value,
      scriptedTests / target := crossTarget.value / "sbt-test",
      scriptedTests / copyResources := {
        val testResources = (scriptedTests / resources).value
        val testDirectories = (scriptedTests / resourceDirectories).value
        val testTarget = (scriptedTests / target).value
        val cacheFile = streams.value.cacheDirectory / "copy-sbt-test"
        val mappings = (testResources --- testDirectories) pair (Path.rebase(testDirectories, testTarget) | Path.flat(testTarget))
        Sync.sync(util.CacheStore.file(cacheFile))(mappings)
        mappings
      },
      sbtTestDirectory := (scriptedTests / target).value,
      scriptedDependencies := {
        scriptedDependencies.value
        (scriptedTests / copyResources).value
      },
      // publish settings
      publishMavenStyle := true,
      licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")

)

// test agent
lazy val maxwell = project
  .in(file("maxwell"))
  .settings(
    name := "maxwell",
    organization := "sbt.javaagent.test",
    autoScalaLibrary := false,
    crossPaths := false,
    packageOptions += Package.ManifestAttributes("Premain-Class" -> "maxwell.Maxwell"),
    publish / skip := true
  )
