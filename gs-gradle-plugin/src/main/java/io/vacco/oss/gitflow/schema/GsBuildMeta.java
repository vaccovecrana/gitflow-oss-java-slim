package io.vacco.oss.gitflow.schema;

/**
 * Core build metadata, provided by each build system (i.e. GitHub Actions, Drone, etc.).
 */
public class GsBuildMeta {

  /**
   * Git source branch, commit hash, build time and build tag
   */
  public String branch, hash, utc, tag;

  /**
   * Build target
   */
  public GsBuildTarget target;

  @Override
  public String toString() {
    return String.format(
      "branch: %s, hash: %s, utc: %s, tag: %s",
      branch, hash, utc, tag
    );
  }

}
