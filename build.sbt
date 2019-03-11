/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

lazy val `sbt-javaagent` = project in file(".")

sbtPlugin := true

name := "sbt-javaagent"
organization := "com.lightbend.sbt"

// sbt cross build
crossSbtVersions := Seq("0.13.18", "1.2.8")

// dependencies
val packagerVersion = "1.3.19"
val packager10xVersion = "1.0.6"
val packager11xVersion = "1.1.6"
val packager12xVersion = "1.2.2"
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % packagerVersion % "provided")

// compile settings
scalacOptions ++= Seq("-encoding", "UTF-8", "-unchecked", "-deprecation", "-feature") ++
  { if ((sbtBinaryVersion in pluginCrossBuild).value == "0.13") Seq("-target:jvm-1.6") else Seq.empty }
javacOptions ++= Seq("-encoding", "UTF-8") ++
  { if ((sbtBinaryVersion in pluginCrossBuild).value == "0.13") Seq("-source", "1.6", "-target", "1.6") else Seq.empty }

// test agent
lazy val maxwell = project
  .in(file("maxwell"))
  .settings(
    name := "maxwell",
    organization := "sbt.javaagent.test",
    autoScalaLibrary := false,
    crossPaths := false,
    packageOptions += Package.ManifestAttributes("Premain-Class" -> "maxwell.Maxwell"),
    publish := ()
  )

// test settings
scriptedSettings
scriptedLaunchOpts ++= Seq(
  "-Dproject.version=" + version.value,
  "-Dpackager.version=" + packagerVersion,
  "-Dpackager.10x.version=" + packager10xVersion,
  "-Dpackager.11x.version=" + packager11xVersion,
  "-Dpackager.12x.version=" + packager12xVersion
)
scriptedDependencies := {
  (publishLocal in maxwell).value
  publishLocal.value
}
test in Test := {
  (test in Test).value
  ScriptedPlugin.scripted.toTask("").value
}

// cross-sbt scripted tests
resourceDirectory in scriptedTests := sourceDirectory.value / "sbt-test"
resourceDirectories in scriptedTests := Seq((resourceDirectory in scriptedTests).value)
resourceDirectories in scriptedTests += sourceDirectory.value / ("sbt-test-" + (sbtBinaryVersion in pluginCrossBuild).value)
includeFilter in scriptedTests := AllPassFilter
excludeFilter in scriptedTests := HiddenFileFilter
resources in scriptedTests := Defaults.collectFiles(resourceDirectories in scriptedTests, includeFilter in scriptedTests, excludeFilter in scriptedTests).value
target in scriptedTests := crossTarget.value / "sbt-test"
copyResources in scriptedTests := {
  val testResources = (resources in scriptedTests).value
  val testDirectories = (resourceDirectories in scriptedTests).value
  val testTarget = (target in scriptedTests).value
  val cacheFile = streams.value.cacheDirectory / "copy-sbt-test"
  val mappings = (testResources --- testDirectories) pair (rebase(testDirectories, testTarget) | flat(testTarget))
  Sync(cacheFile)(mappings)
  mappings
}
sbtTestDirectory := (target in scriptedTests).value
scriptedDependencies := {
  scriptedDependencies.value
  (copyResources in scriptedTests).value
}

// publish settings
publishMavenStyle := false
bintrayOrganization := Some("sbt")
bintrayRepository := "sbt-plugin-releases"
bintrayPackage := "sbt-javaagent"
bintrayReleaseOnPublish := false
licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")

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
  releaseStepCommandAndRemaining("^ publish"),
  releaseStepTask(bintrayRelease),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
