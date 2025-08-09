TaskKey[Unit]("check") := {

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

  // Check that the agent is not included in dist

  assert(
    (Universal / mappings).value forall { case (file, path) =>
      path != "maxwell/maxwell.jar"
    },
    "dist mappings include 'maxwell/maxwell.jar'"
  )

  import scala.sys.process._
  val output =
    ((Universal / com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stagingDirectory).value / "bin" / packageName.value).absolutePath.!!

  assert(
    !(output contains "Agent 86"),
    "output include 'Agent 86'"
  )

}
