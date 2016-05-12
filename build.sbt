/*
 * Copyright © 2016 Lightbend, Inc. <http://www.lightbend.com>
 */

lazy val `sbt-javaagent` = project in file(".")

sbtPlugin := true

name := "sbt-javaagent"
organization := "com.lightbend.sbt"

// dependencies
val packagerVersion = "1.0.6"
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % packagerVersion % "provided")

// compile settings
scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-unchecked", "-deprecation", "-feature")
javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")

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
  "-Dpackager.version=" + packagerVersion
)
scriptedDependencies := {
  (publishLocal in maxwell).value
  publishLocal.value
}
test in Test := {
  (test in Test).value
  ScriptedPlugin.scripted.toTask("").value
}

// publish settings
publishMavenStyle := false
bintrayOrganization := Some("sbt")
bintrayRepository := "sbt-plugin-releases"
bintrayPackage := "sbt-javaagent"
bintrayReleaseOnPublish := false
licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")
