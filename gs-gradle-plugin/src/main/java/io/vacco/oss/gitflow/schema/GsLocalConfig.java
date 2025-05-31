package io.vacco.oss.gitflow.schema;

/**
 * Organization configuration for local development.
 */
public class GsLocalConfig {

  /**
   * A securely accessible URL to load the organization's build configuration.
   * This URL will be refreshed every 48 hours. (required)
   */
  public String orgConfigUrl;

  /**
   * A username to access this organization's internal artifact repository (required)
   */
  public String internalRepoUser;

  /**
   * A password to access this organization's internal artifact repository (required)
   */
  public String internalRepoPassword;

}
