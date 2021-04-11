# Org Config Specification

Having defined the underlying concepts needed for this framework, we can now define an Organization's common build configuration.

It is a JSON document which consists of a few sections backed by a [JSON schema](https://github.com/vaccovecrana/gitflow-oss-java-slim/blob/main/gs-gradle-plugin/src/main/resources/json/gs-org-config.json)

TODO change the above to a tag

```json
{
  "orgId": "my-org-id",
  "internalRepo": {...},
  "snapshotsRepo": {...},
  "releasesRepo": {...},
  "publishing": {...},
  "devConfig": {...}
}
```

`orgId` is a simple `String` identifier for your organization (usually a Github username), and is necessary to store temporary files while working with local code bases.

The `devConfig` attribute specifies default versions of optional tools that you could use in your code bases. In the current version of this framework, the set of recommended tools are [PMD](https://docs.gradle.org/current/userguide/pmd_plugin.html) and [j8spec](https://j8spec.github.io/). Note that these are completely optional and you are not obligated to use them along with this framework.

The remaining three `*Repo` attributes define the three components we saw in the architecture diagram in previous chapters. They establish where your Maven repositories (if any) are located and what credentials they require.

These, along with the `publishing` attribute will be discussed in the next section.
