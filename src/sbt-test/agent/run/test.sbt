TaskKey[Unit]("check") := {
  assert(
    libraryDependencies.value contains ("sbt.javaagent.test" % "maxwell" % sys
      .props("project.version") % "javaagent"),
    "maxwell test agent is not in libraryDependencies under 'javaagent'"
  )

  assert(
    (run / fork).value,
    "run / fork is not enabled"
  )

  assert(
    (run / javaOptions).value exists (s =>
      s.contains("-javaagent:") && s.contains("maxwell")
    ),
    "run / javaOptions do not contain 'maxwell' agent"
  )

  val runLog = IO.read(file("run.log"))

  assert(
    runLog contains "Agent 86",
    "run log does not include 'Agent 86'"
  )
}
