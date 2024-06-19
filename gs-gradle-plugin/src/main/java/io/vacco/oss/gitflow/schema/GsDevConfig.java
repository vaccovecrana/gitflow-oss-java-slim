package io.vacco.oss.gitflow.schema;

public class GsDevConfig {

    /** URL to grab a Gradle distribution in zip format. Used only in GitHub actions. */
    public String gradleDistribution;

    /** Gradle's distribution version in X.Y.Z format (required) */
    public String gradleVersion;

    /** URL to grab a JDK distribution in tar.gz format. Used only in GitHub actions (required) */
    public String jdkDistribution;

    /** Default versions for core organization dependencies */
    public GsVersionsConfig versions;

}
