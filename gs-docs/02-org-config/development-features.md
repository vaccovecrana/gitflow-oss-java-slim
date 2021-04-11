# Development Features Configuration

The last section of the Org config file is defined as:

```json
"devConfig": {
  "jdkDistribution": "https://api.adoptopenjdk.net/v3/binary/latest/11/ga/linux/x64/jdk/hotspot/normal/adoptopenjdk",
  "pmdRulesUrl": "https://vacco-oss.s3.us-east-2.amazonaws.com/vacco-pmd.xml",
  "dependencyExcludedGroups": ["net.sourceforge.pmd"],
  "versions": {
    "gradle": "gradle-6.8.3",
    "j8Spec": "io.github.j8spec:j8spec:3.0.0",
    "pmd": "6.27.0"
  }
}
```

`jdkDistribution` requires a URL to download a specific version of the JDK that will only be used by Github actions to execute a build. This will not affect your local development environment, so you're free to choose what JDK you use to build locally. In the example above, the JDK gets downloaded from `api.adoptopenjdk.net`, and it MUST point to a plain tar `.tar` or compressed `.tar.gz` file which, upon de-compression, will extract the JDK's contents into a single output directory.

> Note: in practice, most JDK distribution bundles follow this format, but you SHOULD verify with any custom JDKs you decide to use. For example, you could copy the JDK distribution file from the web site above into your own private S3 bucket (or some other cloud or internal organization storage location) so that your Github Actions builds do not depend on the availability of other websites.

The remaining configuration options are configured here, but get actually activated by the `build.gradle.kts` file in your source tree, depending on which features you decide to use, and are further explained in later sections.

`pmdRulesUrl` is optional. It is only required if you decide to make use of the [Gradle PMD plugin](https://docs.gradle.org/current/userguide/pmd_plugin.html). It points to a location where a PMD xml rule-set can be downloaded and applied to the current build.

`dependencyExcludedGroups` defines a set of Maven groups which will be excluded when the [Gradle Versions plugin](https://plugins.gradle.org/plugin/com.github.ben-manes.versions) gets applied to the build.

In other words, all builds will apply the Gradle versions plugin by default, and will report on which outdated dependencies your project has. In the example above, I am deciding to ignore any outdated versions under the `net.sourceforge.pmd` Maven group since I am certain that they are not relevant to any of my Github projects.

Lastly, the `versions` block allows you to configure:

- `gradle` - The Gradle distribution version to execute this build. It is specified in `X.Y.Z` format and is downloaded from `services.gradle.org` to execute a build under Github actions. It does not affect which Gradle distribution you use for local development.
- `j8spec` - The version of `jspec` to use. It is defined in Maven dependency notation format.
- `pmd` - The version of the PMD tool that the Gradle plugin will use (when configured) in a build.
