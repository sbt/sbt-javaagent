TaskKey[Unit]("check-log") := {
  val log = IO.read(BuiltinCommands.lastLogFile(state.value).get)

  def expect(expected: String): Unit = {
    assert(log contains expected, s"log should contain '$expected'")
  }

  expect("Agent 86")
  expect("class maxwell.Maxwell")
}

TaskKey[Unit]("clear-log") := {
  IO.write(BuiltinCommands.lastLogFile(state.value).get, "")
}
