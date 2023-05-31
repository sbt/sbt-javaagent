# sbt-javaagent

This sbt plugin adds Java agents to projects in a general way. It can enable agents in [sbt-native-packager] dists, as compile-time dependencies, in forked run, or in forked tests.

## Plugin dependency

Add the plugin to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.github.sbt" % "sbt-javaagent" % versionNumber)
```

See [sbt-javaagent releases] for a list of released versions.

[sbt-javaagent releases]: https://github.com/sbt/sbt-javaagent/releases

## Java agent

To add a Java agent to an [sbt-native-packager] distribution, enable the `JavaAgent` plugin on a project that also has `JavaAppPackaging` enabled, and then add the agent dependency using the `javaAgents` setting. For example:

```scala
lazy val distProject = project
  .in(file("somewhere"))
  .enablePlugins(JavaAgent, JavaAppPackaging)
  .settings(
    javaAgents += "com.example" % "agent" % "1.2.3"
  )
```

This will automatically resolve the agent module, bundle the agent artifact in the distribution, and add a `-javaagent` option to the start script.

> **Note**: sbt-javaagent has a dynamic dependency on [sbt-native-packager]. You need to add sbt-native-packager separately.

## Scopes

By default, sbt-javaagent will only add an agent to distributions. Agents can be optionally enabled for compile, run, or test.

The following scopes are supported:

   * **dist** — bundle the agent in production distributions and add a `-javaagent` option to start scripts
   * **compile** — add the agent as a `provided` dependency so that it's available on the compile classpath
   * **runtime** — automatically fork the run and add a `-javaagent` option
   * **test** — automatically fork tests and add a `-javaagent` option

The plugin can derive these scopes from module configurations.

For example, to add an agent to *compile*, to build against an API provided by an agent, use the `compile` or `provided` configuration:

```scala
javaAgents += "com.example" % "agent" % "1.2.3" % "compile"
```

Marking a dependency for *compile* will also automatically enable the agent for *run* as well.

To enable for run or tests, use the `runtime` or `test` configurations.

Multiple configurations can be specified. For example, the following will enable both *compile* and *test* (and implicitly *run*):

```scala
javaAgents += "com.example" % "agent" % "1.2.3" % "compile;test"
```

Note that in this case, the agent dependency is actually added under the `provided` configuration, so that a project can compile against the agent and then have the agent provided at runtime using a `-javaagent` option.

If the *compile* scope is not enabled, then the agent dependency is put under a special `javaagent` configuration so that it doesn't appear as a regular library dependency or on build classpaths.

## Agent arguments

A Java agent can have an extra argument string added to it that is provided to the `premain` method in the agent. To add an argument string simply provide it to the `JavaAgent` constructor.

```scala
javaAgents += JavaAgent("com.example" % "agent" % "1.2.3" % "compile;test", arguments = "java_agent_argument_string")
```

[sbt-native-packager]: https://github.com/sbt/sbt-native-packager
