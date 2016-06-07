TaskKey[Unit]("check-dependency") := {
  assert(
    libraryDependencies.value contains ("sbt.javaagent.test" % "maxwell" % sys.props("project.version") % "provided"),
    "maxwell test agent is not in libraryDependencies under 'provided'"
  )
}

def expect(name: String, contents: String, expected: String): Unit =
  assert(contents contains expected, s"$name should contain '$expected'")

TaskKey[Unit]("check-log") := {
  val log = IO.read(BuiltinCommands.lastLogFile(state.value).get)
  expect("run log", log, "Agent 86")
  expect("run log", log, "class maxwell.Maxwell")
}

TaskKey[Unit]("check-dist") := {
  val output = ((stagingDirectory in Universal).value / "bin" / packageName.value).absolutePath.!!
  expect("dist run", output, "Agent 86")
  expect("dist run", output, "class maxwell.Maxwell")
}

TaskKey[Unit]("check-test-and-run-paths") := {
  assert(
    !((dependencyClasspath in Runtime).value exists (f => f.data.name.contains("maxwell"))),
    "maxwell test agent is available on the runtime class path"
  )
  assert(
    (dependencyClasspath in Test).value exists (f => f.data.name.contains("maxwell")),
    "maxwell test agent is not available on the test compile class path"
  )
  assert(
    !((fullClasspath in Test).value exists (f => f.data.name.contains("maxwell"))),
    "maxwell test agent is available on the test run class path"
  )
}
