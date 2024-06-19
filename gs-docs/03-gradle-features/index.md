# Gradle Features

In this section we'll discuss how to configure a Gradle project to make use of this framework. The key idea is that this framework SHOULD be minimally intrusive to your Gradle project, and SHOULD allow you to detach from it at any moment.

Start by defining publication information in your `gradle.properties` file:

```ini
libGitUrl=https://github.com/my-org/my-project.git
libDesc=This is a brief description of what my library does
libLicense=Apache License, Version 2.0 (choose a license)
libLicenseUrl=https://opensource.org/licenses/Apache-2.0 (a link to your license text)
```

Now include and configure the Gradle plugin in your source tree in `build.gradle.kts`:

```kotlin
plugins { id("io.vacco.oss.gitflow") version "{{gsVersion}}" }

group = "com.myorg.mylibrary" // your project's target maven coordinates.
version = "0.1.0" // or whichever version you have

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  // add other configuration features here
  sharedLibrary(true, true)
  addJ8Spec()
  addClasspathHell()
}
```

The currently supported set of optional features are:

`sharedLibrary(boolean publish, boolean internal)` configures the Gradle project (or sub-project) to produce a shared Java library. When `publish` is false, the java libraries produced will NOT be published to target Maven repositories. For example if you have test support libraries (like `my-library-test-assets`) that are not intended to be used as part of a library's main binaries.

The `internal` parameter will only have effect when `publish` is true, and will determine if the compiled artifacts will get published to your organization's internal Maven repository, or any repositories you configured for public `SNAPSHOT` and `RELEASE` access (like Sonatype's OSS servers).

`addJ8Spec()` will add [j8spec](https://j8spec.github.io/) to your `testImplementation` class path.

`addClasspathHell()` will apply the [Class path Hell Gradle plugin](https://github.com/vaccovecrana/classpath-hell-gradle-plugin).

`addGoogleJavaFormat()` will apply the [Google Java format Gradle plugin](https://github.com/sherter/google-java-format-gradle-plugin) to the build, automatically formatting sources during a build.

> Note: when applying these optional features, all of them can still be customized by their respective declarative configuration blocks. Thus your source project is still free to customize each optional feature as it deems fit.
