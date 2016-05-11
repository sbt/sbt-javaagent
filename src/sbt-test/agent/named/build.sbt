lazy val agentNamed = project in file(".") enablePlugins (JavaAgent, JavaAppPackaging)

javaAgents += JavaAgent(name = "Get Smart", module = "sbt.javaagent.test" % "maxwell" % sys.props("project.version"))
