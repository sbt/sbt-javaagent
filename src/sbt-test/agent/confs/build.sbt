lazy val agentConfs = project in file(".") enablePlugins JavaAgent

javaAgents += "sbt.javaagent.test" % "maxwell" % sys.props("project.version") % "compile;test"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"

// send test stdout to log — works but should be easier?
// outputStrategy is a setting, so it can't access tasks like streams
testGrouping in Test := {
  (testGrouping in Test).value map {
    case group @ Tests.Group(_, _, Tests.SubProcess(forkOptions)) =>
      group.copy(runPolicy = Tests.SubProcess(copyForkOptions(forkOptions, LoggedOutput(streams.value.log))))
    case group => group
  }
}

// copy manually to be compatible with both sbt 0.13 (case class) and sbt 1.0 (contraband)
def copyForkOptions(o: ForkOptions, newOutputStrategy: OutputStrategy): ForkOptions = {
  ForkOptions(o.javaHome, Some(newOutputStrategy), o.bootJars, o.workingDirectory, o.runJVMOptions, o.connectInput, o.envVars)
}
