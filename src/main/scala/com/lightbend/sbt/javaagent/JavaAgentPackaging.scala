/*
 * Copyright © 2016 Lightbend, Inc. <http://www.lightbend.com>
 */

package com.lightbend.sbt.javaagent

import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging.autoImport.bashScriptExtraDefines
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import java.io.File

/**
 * Plugin for adding Java agents to `sbt-native-packager` distributions.
 */
object JavaAgentPackaging extends AutoPlugin {
  import JavaAgent.JavaAgentKeys._

  override def trigger = allRequirements

  override def requires = JavaAgent && PluginRef("com.typesafe.sbt.packager.archetypes.JavaAppPackaging")

  override def projectSettings = Seq(
    mappings in Universal ++= agentMappings.value,
    bashScriptExtraDefines ++= agentScriptOptions.value
  )

  private def agentMappings = Def.task[Seq[(File, String)]] {
    resolvedJavaAgents.value filter (_.agent.scope.dist) map { resolved =>
      resolved.artifact -> (Project.normalizeModuleID(resolved.agent.name) + File.separator + resolved.artifact.name)
    }
  }

  private def agentScriptOptions = Def.task[Seq[String]] {
    agentMappings.value map {
      case (_, path) => s"""addJava "-javaagent:$${app_home}/../${normalizePath(path)}" """
    }
  }

  private def normalizePath(path: String, separator: Char = File.separatorChar): String = {
    if (separator == '/') path else path.replace(separator, '/')
  }

  /**
   * Reflectively load an auto plugin if it's on the classpath, otherwise
   * return an empty plugin that won't trigger all-requirements loading.
   * See also: https://github.com/sbt/sbt/issues/2538
   */
  private def PluginRef(className: String): AutoPlugin = {
    try {
      val name = if (className.endsWith("$")) className else (className + "$")
      Class.forName(name).asInstanceOf[Class[AutoPlugin]]
        .getDeclaredField("MODULE$").get(null) match {
          case plugin: AutoPlugin => plugin
          case _ => throw new RuntimeException(s"[$name] is not an AutoPlugin object")
        }
    } catch {
      case _: Throwable => NoPlugin
    }
  }

  object NoPlugin extends AutoPlugin

}
