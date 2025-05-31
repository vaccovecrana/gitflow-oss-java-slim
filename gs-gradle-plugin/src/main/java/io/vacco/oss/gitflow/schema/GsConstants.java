package io.vacco.oss.gitflow.schema;

public class GsConstants {

  // GitHub environment variables
  public static final String GITHUB_SHA = "GITHUB_SHA";
  public static final String GITHUB_REF = "GITHUB_REF";
  public static final String GITHUB_INPUT_ORGCONFIG = "INPUT_ORGCONFIG";

  // Drone environment variables
  public static final String CI_COMMIT_BRANCH = "CI_COMMIT_BRANCH";
  public static final String CI_COMMIT_SHA = "CI_COMMIT_SHA";
  public static final String PLUGIN_ORGCONFIG = "PLUGIN_ORGCONFIG";

  // Common variables
  public static final String GS_LOCAL_ORG_CONFIG_FMT = ".gsOrgConfig.%s.json";
  public static final String GS_LOCAL_CONFIG_FILE = ".gsOrgConfig.json";

  public static final long localConfigUpdateDeltaMs = 172_800_000; // local org config gets refreshed every 48hrs.

  public static final String kLibDesc = "libDesc";
  public static final String kLibGitUrl = "libGitUrl";

  public static final String libs = "libs";

  // Tasks/groups
  public static final String java = "java";
  public static final String build = "build", classes = "classes", check = "check", test = "test";
  public static final String publish = "publish", publishing = "publishing", publications = "publications", portalPublish = "portalPublish";
  public static final String checkClasspath = "checkClasspath", googleJavaFormat = "googleJavaFormat";

  // Dependency configurations
  public static final String runtimeClasspath = "runtimeClasspath";
  public static final String testImplementation = "testImplementation";

}
