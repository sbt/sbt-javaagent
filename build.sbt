/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

// dependencies
val packagerVersion = "1.11.7"
val packager19xVersion = "1.9.16"

val scala212 = "2.12.21"
val scala3 = "3.8.4"

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

// pom-only BOM pinning the maxwell agent version, used to test wildcard resolution
lazy val maxwellBom = project
  .in(file("maxwell-bom"))
  .settings(
    name := "maxwell-bom",
    organization := "sbt.javaagent.test",
    autoScalaLibrary := false,
    crossPaths := false,
    Compile / packageBin / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false,
    Compile / packageDoc / publishArtifact := false,
    makePom / publishArtifact := true,
    makePom := {
      val pomFile = target.value / s"${name.value}-${version.value}.pom"
      IO.write(
        pomFile,
        s"""<?xml version="1.0" encoding="UTF-8"?>
           |<project xmlns="http://maven.apache.org/POM/4.0.0">
           |  <modelVersion>4.0.0</modelVersion>
           |  <groupId>${organization.value}</groupId>
           |  <artifactId>${name.value}</artifactId>
           |  <version>${version.value}</version>
           |  <packaging>pom</packaging>
           |  <dependencyManagement>
           |    <dependencies>
           |      <dependency>
           |        <groupId>${(maxwell / organization).value}</groupId>
           |        <artifactId>${(maxwell / name).value}</artifactId>
           |        <version>${(maxwell / version).value}</version>
           |      </dependency>
           |    </dependencies>
           |  </dependencyManagement>
           |</project>
           |""".stripMargin
      )
      pomFile
    },
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
        case "2.12" => Seq("-Xsource:3", "-release:8")
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
      // published to the local Maven repository (~/.m2) too: the BOM/wildcard scripted
      // test resolves through it since coursier only reads dependencyManagement/BOM
      // constraints off of poms it fetches from a genuine Maven repository, not off of
      // ivy2-local's ivy.xml descriptor.
      (maxwell / publishM2).value
      (maxwellBom / publishM2).value
      publishLocal.value
    },
    (pluginCrossBuild / sbtVersion) := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.11.6"
        case _      => "2.0.0"
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
