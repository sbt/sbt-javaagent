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
    (Test / javaOptions).value exists (s =>
      s.contains("-javaagent:") && s.contains("maxwell") && s.contains(
        "=Get_Smart;Agent_99"
      )
    ),
    "Test / javaOptions do not contain 'Get_Smart;Agent_99' agent arguments"
  )

  assert(
    (Universal / mappings).value exists { case (file, path) =>
      path == "maxwell/maxwell.jar"
    },
    "dist mappings do not include 'maxwell/maxwell.jar'"
  )

  import scala.sys.process._
  val output =
    ((Universal / stagingDirectory).value / "bin" / packageName.value).absolutePath.!!

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
