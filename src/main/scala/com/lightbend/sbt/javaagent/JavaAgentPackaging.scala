/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

package com.lightbend.sbt.javaagent

import sbt._
import sbt.Keys._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.Universal
import java.io.File

/**
 * Plugin for adding Java agents to `sbt-native-packager` distributions.
 */
object JavaAgentPackaging extends AutoPlugin {
  import JavaAgent.JavaAgentKeys._

  override def trigger = allRequirements

  override def requires = JavaAgent && PluginRef("com.typesafe.sbt.packager.archetypes.JavaAppPackaging")

  override def projectSettings = {
    import com.typesafe.sbt.packager.{ Keys => PackagerKeys }
    Seq(
      mappings in Universal ++= agentMappings.value.map(m => m._1 -> m._2),
      PackagerKeys.bashScriptExtraDefines ++= agentBashScriptOptions.value,
      PackagerKeys.batScriptExtraDefines ++= agentBatScriptOptions.value
    )
  }

  private def agentMappings = Def.task[Seq[(File, String, String)]] {
    resolvedJavaAgents.value filter (_.agent.scope.dist) map { resolved =>
      (resolved.artifact,
        Project.normalizeModuleID(resolved.agent.name) + File.separator + resolved.artifact.name,
        resolved.agent.arguments)
    }
  }

  private def agentBashScriptOptions = Def.task[Seq[String]] {
    agentMappings.value map {
      case (_, path, arguments) => s"""addJava "-javaagent:$${app_home}/../${normalizeBashPath(path)}$arguments" """
    }
  }

  private def normalizeBashPath(path: String, separator: Char = File.separatorChar): String =
    normalizePath(path, '/')

  private def agentBatScriptOptions = Def.task[Seq[String]] {
    agentMappings.value map {
      case (_, path, arguments) => s"""set _JAVA_OPTS=-javaagent:%@@APP_ENV_NAME@@_HOME%\\${normalizeBatPath(path)}$arguments %_JAVA_OPTS%"""
    }
  }

  private def normalizeBatPath(path: String, separator: Char = File.separatorChar): String =
    normalizePath(path, '\\')

  private def normalizePath(path: String, expected: Char, actual: Char = File.separatorChar): String = {
    if (actual == expected) path else path.replace(actual, expected)
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
