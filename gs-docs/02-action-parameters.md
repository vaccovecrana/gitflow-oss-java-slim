# Github action parameters

In this framework both Github Actions and Gradle work together to provide development methods for different circumstances. Namely:

- Un-managed builds - These are the builds that you perform in your local development machine, i.e. `gradle clean build`. They produce `SNAPSHOT` artifacts that you can deploy to your local Maven repository.
- Managed builds - These are builds running under Github Actions, and any produced artifacts get routed to a target Maven repository.

Here is the minimal amount of configuration required under `.github/workflows/main.yml`:

```
name: Gradle Build
on: {push: {tags: null}}
jobs:
  build:
    runs-on: ubuntu-18.04
    steps:
      - uses: actions/checkout@v2
      - uses: vaccovecrana/gitflow-oss-java-slim@{{gsVersion}}
        with:
          orgConfig: https://my-bucket.s3.us-east-2.amazonaws.com/my-org-config.json
        env:
          SONATYPE_USER: <secrets.MY_ORGS_SONATYPE_USER>
          SONATYPE_PASSWORD: <secrets.MY_ORGS_SONATYPE_PASSWORD>
          MAVEN_SIGNING_PRV: <secrets.MY_ORGS_MAVEN_SIGNING_KEY>
```

There are two key parameters that are needed by this framework:

`orgConfig` is the core piece of this framework which defines common build conventions which will drive the build process for a wide set of projects in your organization. It is defined as a JSON document and it MAY be validated with a JSON schema provided by this framework. It gets sourced from a URL that users in your organization can securely access.

How you implement said access is up to you.

> In the example above, I am providing a URL pointing to a publicly accessible S3 bucket location, since I am sure that no sensitive information is contained inside the document. Another option could be to store your organization's common build configuration in a Github Gist. And as a final example, you could also lock an HTTP server containing the JSON document behind a private VPN, which would impose an extra step on your developers when they need to work with your organization's code bases.

Notice that the Github action's `env` property defines a set of keys/values that the organization's common build configuration MAY require to contact the Maven repositories defined in the configuration, along with credentials to sign artifacts when building `RELEASE` versions of a code base. Names for these keys and values are not mandated by this framework. They can be whatever keys/values you want them to. However you must make sure that the variable references inside the Org configuration document reference the right variable names.

> Warning: you SHOULD be mindful with the secret values that you are sharing with this framework. In general, you SHOULD follow security practices which grant the minimal amount of credentials that will grant this action with the privileges to execute and deploy your build. A good set of sane recommendations can be found in the [Configure AWS Credentials](https://github.com/aws-actions/configure-aws-credentials#credentials) Github Action.

The syntax and purpose of the Organization's common build configuration is explained in the next section.
