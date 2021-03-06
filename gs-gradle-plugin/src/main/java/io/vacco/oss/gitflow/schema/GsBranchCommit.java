package io.vacco.oss.gitflow.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

/**
 * This class captures a minimal subset of field from the Github event JSON model.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GsBranchCommit {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Repository {
    public String default_branch;
    public String full_name;
    public String name;
    public String url;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Author {
    public String email;
    public String name;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class HeadCommit {
    public Author author;
    public Author committer;
    public String message;
    public Date timestamp;
    public String url;
  }

  public String after;
  public String base_ref;
  public String before;
  public String compare;
  public HeadCommit head_commit;
  public String ref;
  public Repository repository;

  // Additional runtime build attributes added by Github Actions
  public GsBuildTarget buildTarget;

  // Dynamic attributes generated by this plugin
  public String utcNow;
  public String shaTagTxt;

}
