TaskKey[Unit]("check") := {
  assert(
    (Test / fork).value,
    "Test / fork is not enabled"
  )

  assert(
    (Test / javaOptions).value exists (s =>
      s.contains("-javaagent:") && s.contains("maxwell")
    ),
    "Test / javaOptions do not contain 'maxwell' agent"
  )

  assert(
    !((Test / fullClasspath).value exists (f =>
      f.data.name.contains("maxwell")
    )),
    "maxwell test agent is available on the test run class path"
  )

  val testLog = IO.read(BuiltinCommands.lastLogFile(state.value).get)

  assert(
    testLog contains "Agent 86",
    "test log does not include 'Agent 86'"
  )
}
