lazy val agentConfs = project.in(file(".")).enablePlugins(JavaAgent)

javaAgents += "sbt.javaagent.test" % "maxwell" % sys.props("project.version") % "compile;test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
