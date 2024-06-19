# Configuration Evolution

It is possible (and likely) that you may maintain different source projects which require slight variations amongst each other. For example:

- Create a `java8.json` configuration if you maintain old projects which mandate version 8 of the JDK.
- Create a `java11.json` configuration for your stable projects.
- Create a `java15.json` configuration for research and prototyping projects.

```
https://my-bucket.s3.us-east-2.amazonaws.com/my-org-config-java8.json
https://my-bucket.s3.us-east-2.amazonaws.com/my-org-config-java11.json
https://my-bucket.s3.us-east-2.amazonaws.com/my-org-config-java15.json
```

This can help you maintain control on how quickly do you wish to move your projects to new versions of the JDK, Gradle itself, etc.
