TaskKey[Unit]("checkRunLog") := checkLog("run.log")
TaskKey[Unit]("checkTestLog") := checkLog("test.log")

def checkLog(logFile: String): Unit = {
  val log = IO.read(file(logFile))

  def expect(expected: String): Unit = {
    assert(log contains expected, s"log should contain '$expected'")
  }

  expect("Agent 86")
  expect("class maxwell.Maxwell")
}
