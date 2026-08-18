TaskKey[Unit]("check") := {
  val resolved = (JavaAgent.autoImport.resolvedJavaAgents).value
  val report = update.value

  val maxwellAgent = resolved
    .find(r => r.agent.module.organization == "sbt.javaagent.test" && r.agent.module.name == "maxwell")
    .getOrElse(sys.error(s"expected maxwell agent to resolve, got: $resolved"))

  assert(
    maxwellAgent.agent.module.revision == "*",
    "expected the javaAgents wildcard revision to be preserved on the (unresolved) agent module"
  )

  assert(
    maxwellAgent.artifact.exists(),
    s"resolved agent artifact does not exist: ${maxwellAgent.artifact}"
  )

  // The wildcard agent module itself keeps its "*" revision (asserted above): the only way to
  // check which concrete version was actually picked is to look at what `update` resolved.
  // This proves the version came from maxwell-bom's dependencyManagement (project.version),
  // and not the literal "*" or some other, unintended version.
  val expectedVersion = sys.props("project.version")
  val actualResolvedVersion = report.allModules
    .find(m => m.organization == "sbt.javaagent.test" && m.name == "maxwell")
    .map(_.revision)
    .getOrElse(sys.error(s"expected maxwell to be present in the update report, got: ${report.allModules}"))

  assert(
    actualResolvedVersion == expectedVersion,
    s"expected the wildcard agent to resolve to version '$expectedVersion' via the maxwell-bom BOM, " +
      s"but resolved version was '$actualResolvedVersion'"
  )
}
