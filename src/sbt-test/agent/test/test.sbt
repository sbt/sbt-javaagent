TaskKey[Unit]("check") := {
  assert(
    (fork in Test).value,
    "fork in Test is not enabled"
  )

  assert(
    (javaOptions in Test).value exists (s => s.contains("-javaagent:") && s.contains("maxwell")),
    "javaOptions in Test do not contain 'maxwell' agent"
  )

  assert(
    !((fullClasspath in Test).value exists (f => f.data.name.contains("maxwell"))),
    "maxwell test agent is available on the test run class path"
  )

  val testLog = IO.read(BuiltinCommands.lastLogFile(state.value).get)

  assert(
    testLog contains "Agent 86",
    "test log does not include 'Agent 86'"
  )
}
