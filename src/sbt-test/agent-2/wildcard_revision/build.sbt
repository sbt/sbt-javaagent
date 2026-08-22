lazy val agentTest = project.in(file(".")).enablePlugins(JavaAgent)

// The fake "maxwell-bom" is published locally to ~/.m2 (see `maxwellBom / publishM2` in the
// root build), so it can only be resolved as a real Maven BOM through the local Maven
// repository, not through the default ivy2-local one.
resolvers += Resolver.mavenLocal

// Depending on a BOM without pinning any version ourselves is the whole point of this test:
// the "maxwell" agent below must get its version exclusively from this BOM's dependencyManagement.
libraryDependencies += ("sbt.javaagent.test" % "maxwell-bom" % sys.props("project.version")).pomOnly()

javaAgents += "sbt.javaagent.test" % "maxwell" % "*"
