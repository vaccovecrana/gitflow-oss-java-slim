# Overview

![icon-light](icon-light.svg)

`gitflow-oss-java-slim` is a minimal, opinionated java Continuous Integration framework which uses Gradle and Github Actions to support a simplified version of the Gitflow development model.

It is primarily targeted towards Open source Java developers and organizations that need to manage the complexity of building, versioning and releasing binary code artifacts.

> The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL
> NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED",  "MAY", and
> "OPTIONAL" in this document are to be interpreted as described in
> [RFC 2119](https://datatracker.ietf.org/doc/html/rfc2119).


## Is this framework for me?

In order to make the best use of your time (and mine), reflect for a moment.

If you:

- Develop and manage multiple Java libraries on a daily basis.
- Do not have dedicated CI infrastructure servers or cloud services.
- Find yourself copy/pasting boilerplate publication configuration code for each code base that you manage.
- Struggle to assign and manage development/release versions for your artifacts.
- Publish and make use of both internal and external artifacts regularly.
- Share artifact dependencies amongst your projects, and need to know when a particular project's internal dependencies are lagging behind.
- Do all of the above using internal and external Maven distribution repositories.

If at least 4 of the 7 points above apply to you, this framework may be of meaningful use.

Otherwise you can stop here (and perhaps share some insight on how you tackle the 7 problems above).
