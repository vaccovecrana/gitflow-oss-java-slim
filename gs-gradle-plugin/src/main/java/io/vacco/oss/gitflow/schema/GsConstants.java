package io.vacco.oss.gitflow.schema;

public class GsConstants {

  // Environment variables
  public static final String GS_PMD_XML_FMT = "pmd-%s.xml";
  public static final String GS_LOCAL_ORG_CONFIG_FMT = ".gsOrgConfig.%s.json";
  public static final String GS_LOCAL_CONFIG_FILE = ".gsOrgConfig.json";
  public static final String GS_CONFIG_URL = "GS_CONFIG_URL";
  public static final String GS_GH_EVENT = "GS_GH_EVENT";

  public static final long localConfigUpdateDeltaMs = 172_800_000; // local org config gets refreshed every 48hrs.

  // Tasks
  public static final String build = "build", classes = "classes", publish = "publish";
  public static final String checkClasspath = "checkClasspath", googleJavaFormat = "googleJavaFormat";

  // Source sets
  public static final String main = "main";

  // Dependency configurations
  public static final String runtimeClasspath = "runtimeClasspath";
  public static final String testImplementation = "testImplementation";
}
