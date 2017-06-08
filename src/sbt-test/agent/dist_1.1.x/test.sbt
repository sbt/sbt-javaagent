TaskKey[Unit]("check") := {
  assert(
    (mappings in Universal).value exists { case (file, path) => path == "maxwell/maxwell.jar" },
    "dist mappings do not include 'maxwell/maxwell.jar'"
  )

  val output = ((stagingDirectory in Universal).value / "bin" / packageName.value).absolutePath.!!

  assert(
    output contains "Agent 86",
    "output does not include 'Agent 86'"
  )
}
