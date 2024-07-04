package io.vacco.oss.gitflow.schema;

public class GsOrgRepo {

  /**
   * Repository ID (required)
   */
  public String id;

  /**
   * Repository URL (required)
   */
  public String url;

  /**
   * Repository username
   */
  public String username;

  /**
   * Repository password
   */
  public String password;

  /**
   * Environment variable for repository username
   */
  public String usernameEnvProperty;

  /**
   * Environment variable for repository password
   */
  public String passwordEnvProperty;

}
