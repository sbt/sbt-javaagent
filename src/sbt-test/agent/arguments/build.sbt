lazy val agentOptions =
  project.in(file(".")).enablePlugins(JavaAgent, JavaAppPackaging)

javaAgents += JavaAgent(
  module = "sbt.javaagent.test" % "maxwell" % sys.props(
    "project.version"
  ) % "dist;test",
  arguments = "Get_Smart;Agent_99"
)
