# Build System Overview

In a nutshell, this build system brings together a few concepts which are common across many code-bases within an organization, scoped down to the problem of managing Java code as shared Maven artifacts. It's possible to implement functionality to perform more advanced tasks like Docker image builds or build status reporting, but let's keep the scope short and simple for now.

![build-system-00](build-system-00.svg)

The diagram above illustrates the flow of release management when you work on a code base using this Gradle plugin/Github action combination. Depending on the type of release artifact being built by Github actions, the artifacts themselves will be routed to different target Maven repositories, if any.

The advantage of this approach is that you can have multiple code bases compiled under a single organizational build configuration.

Release artifact versions are defined as:

- `SNAPSHOT`: A version that you're experimenting or prototyping on, and SHOULD only be consumed by other development team members who understand that this code can break and change very quickly (and thus break their builds as well).
- `MILESTONE`: A version that you've tested to a satisfactory level, and should be consumed by others, with the knowledge that minor changes MAY break their build.
- `RELEASE`: A version that you've tested thoroughly and can be consumed by other developers with a high expectation of stability and functionality.

> Note: These intermediate version types are meant to serve as a complement to [Semantic Versioning](https://semver.org/) conventions, which are highly recommended by this project, and help your project manage the way in which software versions break downstream consumer builds of your shared artifacts.

Consider a Gradle project with Kotlin syntax defining an artifact called, `my-library`:

```kotlin
group = "io.vacco.mylibrary"
version = "0.1.0"
```

During development, the following are examples of intermediate artifact type versions added by this framework:


| Artifact Type | Version label                              |
| :------------ | ------------------------:                  |
| `SNAPSHOT`    | `my-library-0.1.0-SNAPSHOT`                |
| `MILESTONE`   | `my-library-0.1.0-MILESTONE-202104081246` |
| `RELEASE`     | `my-library-0.1.0`                          |

Notice that the `MILESTONE` version convention also appends a timestamp component in the form of `YYYYMMDDHHMM`, so as to give the intermediate version a unique number. The reason for this is that most Maven repositories only allow re-reploying `SNAPSHOT` versions, and MAY reject versions which already exist inside the repository.

Some further improvement and customization options for this naming convention may include code or configuration level overrides to allow for simple number counters or some other form of uniqueness convention. In practice, timestamps seem to be a good solution to track information about a particular artifact `MILESTONE` version.

At this point you may be asking yourself: _"why is there a distinction between a `SNAPSHOT` and a `MILESTONE` version?"_ This is explained in the following section, where the relationship between a Git development branch name and the artifact version type it produces is established.