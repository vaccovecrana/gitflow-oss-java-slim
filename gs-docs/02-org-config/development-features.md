# Development Features Configuration

The last section of the Org config file is defined as:

```json
"devConfig": {
  "versions": {
    "j8Spec": "io.github.j8spec:j8spec:3.0.1"
  }
}
```

The remaining configuration options are configured here, but get actually activated by the `build.gradle.kts` file in your source tree, depending on which features you decide to use, and are further explained in later sections.

In other words, all builds will apply the Gradle versions plugin by default, and will report on which outdated dependencies your project has. 

Lastly, the `versions` block allows you to configure:

- `gradle` - The Gradle distribution version to execute this build. It is specified in `X.Y.Z` format and is downloaded from `services.gradle.org` to execute a build under Github actions. It does not affect which Gradle distribution you use for local development.
- `j8spec` - The version of `jspec` to use. It is defined in Maven dependency notation format.
