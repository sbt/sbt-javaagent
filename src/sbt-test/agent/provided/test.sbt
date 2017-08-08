TaskKey[Unit]("check") := {
  def expect(name: String, contents: String, expected: String): Unit =
    assert(contents contains expected, s"$name should contain '$expected'")

  val lastLog = IO.read(BuiltinCommands.lastLogFile(state.value).get)

  expect("run log", lastLog, "Agent 86")
  expect("run log", lastLog, "class maxwell.Maxwell")

  import scala.sys.process._
  val output = ((stagingDirectory in Universal).value / "bin" / packageName.value).absolutePath.!!

  expect("dist run", output, "Agent 86")
  expect("dist run", output, "class maxwell.Maxwell")
}
