lazy val agentDist = project in file(".") enablePlugins (JavaAgent, JavaAppPackaging)

javaAgents += "sbt.javaagent.test" % "maxwell" % sys.props("project.version")
