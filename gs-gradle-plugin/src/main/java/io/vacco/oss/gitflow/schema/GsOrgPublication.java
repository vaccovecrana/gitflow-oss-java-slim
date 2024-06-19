package io.vacco.oss.gitflow.schema;

public class GsOrgPublication {

  /**
   * Publication ID (required)
   */
  public String id;

  /**
   * Developer Organization ID (required)
   */
  public String devId;

  /**
   * Developer contact name (e.g. your organization's name) (required)
   */
  public String devContact;

  /**
   * Developer contact email (required)
   */
  public String devEmail;

  /**
   * Environment variable containing an unencrypted,
   * ASCII armoured Maven PGP signing key (required).
   */
  public String mavenSigningKeyEnvProperty;

}
