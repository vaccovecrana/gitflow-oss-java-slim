package io.vacco.oss.gitflow.schema;

public enum GsBuildTarget {

  SNAPSHOT, MILESTONE, RELEASE, PRE_RELEASE;

  public boolean isSnapshot()     { return this == SNAPSHOT; }
  public boolean isMilestone()    { return this == MILESTONE; }
  public boolean isPublication()  { return this != PRE_RELEASE; }
  public boolean isReleaseGated() { return this == RELEASE || this == PRE_RELEASE; }

}
