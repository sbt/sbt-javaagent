/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")
addSbtPlugin("me.lessis"         % "bintray-sbt" % "0.3.0")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
