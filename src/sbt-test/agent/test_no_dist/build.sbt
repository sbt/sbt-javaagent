lazy val agentTest = project.in(file(".")).enablePlugins(JavaAgent, JavaAppPackaging)

javaAgents += "sbt.javaagent.test" % "maxwell" % sys.props(
  "project.version"
) % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
