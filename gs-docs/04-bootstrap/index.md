# Org Config / Project bootstrap cheat-sheet

The previous sections outlined the concepts and structure behind this framework.

The following is a quick walk-through on how to create an Org config from scratch, and configure a single Gradle project to adhere to it. In this example, PMD is disabled and only external OSS publication support is enabled.

Start by creating an Org config template, filling in required parameters:

```json
{
  "orgId": "my-org-id",
  "snapshotsRepo": {
    "id": "SonatypeOSSSnapshots",
    "url": "https://oss.sonatype.org/content/repositories/snapshots/",
    "usernameEnvProperty": "SONATYPE_USER",
    "passwordEnvProperty": "SONATYPE_PASSWORD"
  },
  "releasesRepo": {
    "id": "SonatypeOSSStaging",
    "url": "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
    "usernameEnvProperty": "SONATYPE_USER",
    "passwordEnvProperty": "SONATYPE_PASSWORD"
  },
  "publishing": {
    "id": "an Org id (for example yoyodyne)",
    "devId": "a dev id (for example jhacker)",
    "devContact": "an Org name (for example Yoyodyne, Inc. or James Hacker)",
    "devEmail": "jhacker@yoyodyne.com",
    "mavenSigningKeyEnvProperty": "MAVEN_SIGNING_PRV"
  },
  "devConfig": {
    "gradleVersion": "7.0",
    "gradleDistribution": "https://services.gradle.org/distributions/gradle-7.0-bin.zip",
    "jdkDistribution": "https://api.adoptopenjdk.net/v3/binary/latest/11/ga/linux/x64/jdk/hotspot/normal/adoptopenjdk",
    "versions": {
      "j8Spec": "io.github.j8spec:j8spec:3.0.0"
    }
  }
}
```

Next, go to the [Github Secrets](https://docs.github.com/en/actions/reference/encrypted-secrets) section of your repository, and configure the following values:

- `SONATYPE_USER` - your Sonatype OSS repository username.
- `SONATYPE_PASSWORD` - your Sonatype OSS account password.
- `MAVEN_SIGNING_PRV` - An ASCII armoured version of you Maven PGP signin key.

You can export the PGP private key intended to sign releases with the following command:

```
gpg --output private.pgp --armor --export-secret-key jhacker@yoyodyne.com
```

> Note: In general, it should be safe to export subkeys derived from a PGP master private key. By consequence, it is never recommended that you store a master private key as a Github Secret to sign Maven artifacts.

Set the values required, and upload this to an S3 bucket, Dropbox or a [Github Gist](https://gist.github.com/). Keep the URL where you uploaded this configuration.

## Local development configuration

On your local development machine, create a file called `.gsOrgConfig.json` inside your home folder. Include the following contents:

```json
{
  "orgId": "my-org-id",
  "orgConfigUrl": "https://<Location where you uploaded the Org Config file>"
}
```

## Gradle Project setup

Next, switch over to the project you'd like to manage with this framework, and set:

`.github/workflows/main.yml`

```yaml
name: Gradle Build
on: {push: {tags: null}}
jobs:
  build:
    runs-on: ubuntu-18.04
    steps:
      - uses: actions/checkout@v2
      - uses: vaccovecrana/gitflow-oss-java-slim@0.9.7
        with:
          orgConfig: https://<Location where you uploaded the Org Config file>
        env:
          SONATYPE_USER: ${{secrets.MY_ORGS_SONATYPE_USER}}
          SONATYPE_PASSWORD: ${{secrets.MY_ORGS_SONATYPE_PASSWORD}}
          MAVEN_SIGNING_PRV: ${{secrets.MY_ORGS_MAVEN_SIGNING_KEY}}
```

`gradle.properties`:

```ini
libGitUrl=https://github.com/my-org/my-project.git
libDesc=This is a brief description of what my library does
libLicense=Apache License, Version 2.0 (choose a license)
libLicenseUrl=https://opensource.org/licenses/Apache-2.0 (a link to your license text)
```

Finally, include and configure the Gradle plugin in your source tree in `build.gradle.kts`:

```kotlin
plugins { id("io.vacco.oss.gitflow") version "0.9.7" }

group = "com.myorg.mylibrary" // your project's target maven coordinates.
version = "0.1.0" // or whichever version you have

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  sharedLibrary(false, true) // external library with publication support
  addJ8Spec()
  addPmd()
  addClasspathHell()
}
```

Once this is done, the following managed builds will take place:

- Committing a `feature/XYZ` branch will produce and upload `SNAPSHOT` artifact into the Sonatype OSS snapshots repository. One such branch could be named `feature/performance-improvements`.
- Committing into the `develop` branch will build and upload a `MILESTONE` artifact into the Sonatype OSS snapshots repository.
- Creating a tag out from `master` or `main` will build and upload a `RELEASE` artifact into the Sonatype OSS releases repository. Once you close and release the temporary repository, the artifact will be available in Maven Central.
