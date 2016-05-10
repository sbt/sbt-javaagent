lazy val agentTest = project in file(".") enablePlugins JavaAgent

javaAgents += "sbt.javaagent.test" % "maxwell" % sys.props("project.version") % "test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

// send test stdout to log — works but should be easier?
// outputStrategy is a setting, so it can't access tasks like streams
testGrouping in Test := {
  (testGrouping in Test).value map {
    case group @ Tests.Group(_, _, Tests.SubProcess(forkOptions)) =>
      group.copy(runPolicy = Tests.SubProcess(forkOptions.copy(outputStrategy = Some(LoggedOutput(streams.value.log)))))
    case group => group
  }
}
