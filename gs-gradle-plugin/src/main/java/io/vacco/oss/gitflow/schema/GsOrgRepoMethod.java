package io.vacco.oss.gitflow.schema;

public enum GsOrgRepoMethod {

  /**
   * PUT Username/password authentication.
   * Individual files.
   * */
  MavenClassic,

  /**
   * POST Portal publisher API bearer token.
   * Zip bundle. Manual release.
   */
  PortalPublisherApiManual,

  /**
   * POST Portal publisher API bearer token.
   * Zip bundle. Automatic release.
   */
  PortalPublisherApiAutomatic

}
