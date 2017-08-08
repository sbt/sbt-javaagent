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
    (javaOptions in Test).value exists (s => s.contains("-javaagent:") && s.contains("maxwell") && s.contains("=Get_Smart;Agent_99")),
    "javaOptions in Test do not contain 'Get_Smart;Agent_99' agent arguments"
  )

  assert(
    (mappings in Universal).value exists { case (file, path) => path == "maxwell/maxwell.jar" },
    "dist mappings do not include 'maxwell/maxwell.jar'"
  )

  import scala.sys.process._
  val output = ((stagingDirectory in Universal).value / "bin" / packageName.value).absolutePath.!!

  assert(
    !(output contains "Agent 86"),
    "output includes 'Agent 86'"
  )
  assert(
    !(output contains ";"),
    "output includes ';'"
  )
  assert(
    output contains "Get_Smart",
    "output does not include 'Get_Smart'"
  )
  assert(
    output contains "Agent_99",
    "output does not include 'Agent_99'"
  )
}
