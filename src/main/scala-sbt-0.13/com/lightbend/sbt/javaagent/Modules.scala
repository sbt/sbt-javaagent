/*
 * Copyright © 2016-2017 Lightbend, Inc. <http://www.lightbend.com>
 */

package com.lightbend.sbt.javaagent

import sbt.{ ModuleFilter, ModuleID }

object Modules {
  def withConfigurations(module: ModuleID, configurations: Option[String]): ModuleID =
    module.copy(configurations = configurations)

  def exactFilter(module: ModuleID) = new ModuleFilter {
    def apply(m: ModuleID) = {
      m.organization == module.organization &&
      m.name         == module.name &&
      m.revision     == module.revision
    }
  }
}
