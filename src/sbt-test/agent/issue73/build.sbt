val maxwell = "sbt.javaagent.test" % "maxwell" % sys.props("project.version") % Test

libraryDependencies += maxwell

libraryDependencies += "org.scalatest" %% "scalatest-funspec" % "3.2.20" % Test

enablePlugins(JavaAgent)

javaAgents += maxwell
