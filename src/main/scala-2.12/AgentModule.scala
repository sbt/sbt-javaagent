package com.lightbend.sbt.javaagent

import sbt.*
import scala.language.implicitConversions

case class AgentScope(compile: Boolean = false, test: Boolean = false, run: Boolean = false, dist: Boolean = true)

case class AgentModule(name: String, module: ModuleID, scope: AgentScope, arguments: String)

case class ResolvedAgent(agent: AgentModule, artifact: File)

object AgentModule {
  implicit def moduleToAgentModule(module: ModuleID): AgentModule = JavaAgent(module)
}
