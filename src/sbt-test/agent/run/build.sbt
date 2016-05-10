lazy val agentRun = project in file(".") enablePlugins JavaAgent

javaAgents += "sbt.javaagent.test" % "maxwell" % sys.props("project.version") % "runtime"
