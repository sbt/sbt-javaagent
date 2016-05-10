TaskKey[Unit]("check") := {
  assert(
    libraryDependencies.value contains ("sbt.javaagent.test" % "maxwell" % sys.props("project.version") % "javaagent"),
    "maxwell test agent is not in libraryDependencies under 'javaagent'"
  )

  assert(
    (fork in run).value,
    "fork in run is not enabled"
  )

  assert(
    (javaOptions in run).value exists (s => s.contains("-javaagent:") && s.contains("maxwell")),
    "javaOptions in run do not contain 'maxwell' agent"
  )

  val runLog = IO.read(BuiltinCommands.lastLogFile(state.value).get)

  assert(
    runLog contains "Agent 86",
    "run log does not include 'Agent 86'"
  )
}
