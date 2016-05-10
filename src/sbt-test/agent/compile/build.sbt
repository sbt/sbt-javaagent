lazy val agentCompile = project in file(".") enablePlugins (JavaAgent, JavaAppPackaging)

javaAgents += "sbt.javaagent.test" % "maxwell" % sys.props("project.version") % "compile"
