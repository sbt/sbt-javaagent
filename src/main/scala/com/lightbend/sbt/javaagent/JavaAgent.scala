/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

package com.lightbend.sbt.javaagent

import sbt._
import sbt.Keys._

/**
 * Plugin for adding Java agents to projects in a general way.
 * Supports agents as compile-time dependencies, in forked tests, in forked run,
 * and in `sbt-native-packager` dists through the `JavaAgentPackaging` plugin.
 */
object JavaAgent extends JavaAgent {
  val autoImport = JavaAgentKeys

  object JavaAgentKeys {
    val javaAgents = settingKey[Seq[AgentModule]]("Java agent modules enabled for this project.")
    val resolvedJavaAgents = taskKey[Seq[ResolvedAgent]]("Java agent modules with resolved artifacts.")
  }

  val AgentConfig = config("javaagent").hide

  case class AgentScope(compile: Boolean = false, test: Boolean = false, run: Boolean = false, dist: Boolean = true)

  case class AgentModule(name: String, module: ModuleID, scope: AgentScope, arguments: String)

  case class ResolvedAgent(agent: AgentModule, artifact: File)

  object AgentModule {
    import scala.language.implicitConversions
    implicit def moduleToAgentModule(module: ModuleID): AgentModule = JavaAgent(module)
  }

  /**
   * Create an agent module from a module dependency.
   * Scope is also derived from the given module configuration.
   */
  def apply(module: ModuleID, name: String = null, scope: AgentScope = AgentScope(), arguments: String = null): AgentModule = {
    val agentName = Option(name).getOrElse(module.name)
    val agentArguments = Option(arguments).map("=" + _).getOrElse("")
    val confs = module.configurations.toSeq.flatMap(_.split(";"))
    val inCompile = scope.compile || confs.contains(Compile.name) || confs.contains(Provided.name)
    val inRun = scope.run || inCompile || confs.contains(Runtime.name)
    val inTest = scope.test || confs.contains(Test.name)
    val inDist = scope.dist
    val configuration = if (inCompile) Provided else AgentConfig
    val reconfiguredModule = Modules.withConfigurations(module, Some(configuration.name))
    val configuredScope = AgentScope(compile = inCompile, test = inTest, run = inRun, dist = inDist)
    AgentModule(agentName, reconfiguredModule, configuredScope, agentArguments)
  }
}

class JavaAgent extends AutoPlugin {
  import JavaAgent._
  import JavaAgent.JavaAgentKeys._

  override def requires: Plugins = plugins.JvmPlugin

  override def projectSettings = Seq(
    javaAgents := Seq.empty,
    ivyConfigurations += AgentConfig,
    libraryDependencies ++= javaAgents.value.map(_.module),
    resolvedJavaAgents := resolveAgents.value,
    run/fork := enableFork(run/fork, _.scope.run).value,
    run/connectInput := enableFork(run/fork, _.scope.run).value,
    Test/fork := enableFork(Test/fork, _.scope.test).value,
    run/javaOptions ++= agentOptions(_.agent.scope.run).value,
    Test/javaOptions ++= agentOptions(_.agent.scope.test).value,
    Test/fullClasspath := filterAgents((Test/fullClasspath).value, resolvedJavaAgents.value)
  )

  private def resolveAgents = Def.task[Seq[ResolvedAgent]] {
    val missingAgentDependencies = javaAgents.value.map(_.module).filterNot(libraryDependencies.value.contains)
    if (missingAgentDependencies.nonEmpty) {
      sys.error(
        s"Some agents missing from libraryDependencies. " +
          s"It might mean you override libraryDependencies with := instead of adding new ones with ++=. " +
          s"Missing agents: $missingAgentDependencies"
      )
    }
    val resolvedAgents = javaAgents.value flatMap { agent =>
      update.value.matching(Modules.exactFilter(agent.module)).headOption map {
        jar => ResolvedAgent(agent, jar)
      }
    }
    val unresolvedAgents = javaAgents.value.filterNot(agent => resolvedAgents.map(_.agent.module).contains(agent.module))
    if (unresolvedAgents.nonEmpty)
      sys.error(s"Unable to resolve agents, missing agents: $unresolvedAgents")
    else resolvedAgents
  }

  private def enableFork(forkKey: SettingKey[Boolean], enabled: AgentModule => Boolean) = Def.setting[Boolean] {
    forkKey.value || javaAgents.value.exists(enabled)
  }

  private def agentOptions(enabled: ResolvedAgent => Boolean) = Def.task[Seq[String]] {
    resolvedJavaAgents.value filter enabled map { resolved =>
      "-javaagent:" + resolved.artifact.absolutePath + resolved.agent.arguments
    }
  }

  def filterAgents(classpath: Classpath, resolvedAgents: Seq[ResolvedAgent]): Classpath = {
    val agents = resolvedAgents.map(resolved => resolved.artifact.absolutePath)
    classpath.filter(aFile => !agents.contains(aFile.data.getAbsolutePath))
  }
}
