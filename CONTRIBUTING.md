# Contributing

## Release Procedure

If necessary, fix the version number in `version.sbt` and commit and push the change. It is fine to have `-SNAPSHOT` suffix, it will be discarded during the release process.

Open the [Release workflow](https://github.com/sbt/sbt-javaagent/actions/workflows/release.yml) and click the “Run workflow” button. This will publish a release to Sonatype, create a Git tag (based on the version in `version.sbt` without its qualifier), and update the version number in `version.sbt` with the next release version.
