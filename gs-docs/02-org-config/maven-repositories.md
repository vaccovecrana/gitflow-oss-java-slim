# Maven Publication Repositories

## Maven repository definitions

```json
"internalRepo": {
  "id": "GithubPackages",
  "url": "https://maven.pkg.github.com/my-github-org/common-packages/",
  "usernameEnvProperty": "MY_ORGS_CI_USER",
  "passwordEnvProperty": "MY_ORGS_CI_TOKEN",
  "method": "MavenClassic"
}
```

These configuration blocks should be pretty self-explanatory:

- `internalRepo` refers to a Maven repository where you intend to store artifacts for private use inside your organization. Note that this repository MUST allow for both `SNAPSHOT` and `RELEASE` artifacts in the same location for practical purposes. With enough community interest, this could change in the future.

- `snapshotsRepo` and `releasesRepo` refer to remote Maven repositories which intend to store publicly accessible `SNAPSHOT` and `RELEASE` artifacts respectively.

- `method` refers to the artifact upload strategy. I'm hoping this is a transitional attribute, the reason being that Gradle's Maven publishing plugin currently does not support the new Sonatype Central [artifact upload API](https://central.sonatype.com/api-doc) semantics. So for each repository defined, artifacts will be uploaded as:
  - `MavenClassic` - standard `PUT` request with username and password.
  - `PortalPublisherApiManual` - new Sonatype publishing API. You release manually.
  - `PortalPublisherApiAutomatic` - new Sonatype publishing API. The deployment bundle gets released automatically.

Each configuration block can source an access username password by reading values from Environment variable names which you designate, or by reading direct username and password values at runtime inside the Org config file itself. These variable names and values SHOULD be stored as Github secrets.

> Note: if you publish libraries to Sonatype's OSS repositories (Maven Central), you would configure your Sonatype deployment username and password and Github secrets.

Direct credentials input SHOULD NOT be used for CI builds, and is provided only for local development scenarios where you checkout a source code tree and work on it in your laptop or workstation. Specific details on this are discussed in the next section.

Lastly, to determine exactly which repository definitions/combinations should you be using depends on your particular development practices. For example, if you develop pure OSS components, it may not make much sense to define an `internalRepo` block inside the Org config file, since Sonatype's staging and release repositories (Maven Central) may be enough to fit your use case.

On the other hand, if you also develop software for commercial purposes, then it may make sense to align your internal development code bases to source artifacts from a private Maven repository (such as Nexus, Artifactory, Strongbox, Github Packages, etc.).

## Publication Metadata

The `publishing` attribute configures OSS publication metadata that is required when you publish artifacts to Sonatype's OSS repositories (i.e. developer ids, contact and license information that you would include in a classic Maven project's `pom.xml` file).

```json
"publishing": {
  "id": "an Org id (for example yoyodyne)",
  "devId": "a dev id (for example jhacker)",
  "devContact": "an Org name (for example Yoyodyne, Inc. or James Hacker)",
  "devEmail": "jhacker@yoyodyne.com",
  "mavenSigningKeyEnvProperty": "MAVEN_SIGNING_PRV"
}
```

## Maven PGP Signing key

The last configuration is an environment variable which SHOULD point to an environment variable containing an ASCII armoured, password-less PGP private key used to sign release artifacts. The reason for extracting raw key material in this form is that [Github Secrets](https://docs.github.com/en/actions/reference/encrypted-secrets) already offers a good protection mechanism for storing they key material without having to go through the management hassles of PGP key rings, and it works well and safely in practice.

The key material should look similar to this:

```
-----BEGIN PGP PRIVATE KEY BLOCK-----
...a lot more characters...
-----END PGP PRIVATE KEY BLOCK-----
```

The key material will be passed on to the [Gradle Signing plugin](https://docs.gradle.org/current/userguide/signing_plugin.html).

Given enough community interest, it should be possible to extend this part of the framework to ask for a key's passphrase, also stored as a Github secret.

The next section discusses the rest of the Org config file format.
