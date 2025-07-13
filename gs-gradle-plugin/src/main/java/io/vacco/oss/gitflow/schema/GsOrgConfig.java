package io.vacco.oss.gitflow.schema;

/**
 * Organization configuration for Continuous Integration.
 */
public class GsOrgConfig {

  /**
   * Organization ID (required)
   */
  public String orgId;
  public GsOrgRepo internalRepo;
  public GsOrgRepo snapshotsRepo;
  public GsOrgRepo releasesRepo;

  /**
   * (required)
   */
  public GsOrgPublication publishing;

  /**
   * (required)
   */
  public GsDevConfig devConfig;

  @Override public String toString() {
    return orgId;
  }

}
