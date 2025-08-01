package io.vacco.oss.gitflow.impl;

import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.logging.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static io.vacco.oss.gitflow.schema.GsConstants.*;
import static io.vacco.oss.gitflow.schema.GsBuildTarget.*;
import static java.lang.String.format;

public class GsBuildMetas {

  private static final Logger log = Logging.getLogger(GsPluginUtil.class);

  private static String utcNow() {
    var utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
    return utcNow.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
  }

  public static GsBuildMeta loadBuildMeta() {
    var meta = new GsBuildMeta();

    if (System.getenv(GITHUB_SHA) != null) {
      meta.branch = System.getenv(GITHUB_REF);
      meta.hash = System.getenv(GITHUB_SHA);
    } else if (System.getenv(CI_COMMIT_SHA) != null) {
      meta.branch = System.getenv(CI_COMMIT_BRANCH);
      meta.hash = System.getenv(CI_COMMIT_SHA);
    }

    if (meta.hash == null) {
      meta.hash = "0000000";
    }
    if (meta.branch == null || meta.branch.contains("feature/")) {
      meta.target = SNAPSHOT;
    } else if (meta.branch.contains("develop")) {
      meta.target = MILESTONE;
    } else if (meta.branch.contains("refs/tags")) {
      meta.target = RELEASE;
    } else if (meta.branch.contains("master") || meta.branch.contains("main")) {
      meta.target = PRE_RELEASE;
    } else {
      log.warn("Unknown branch [{}], defaulting to {}", meta.branch, PRE_RELEASE);
      meta.target = PRE_RELEASE;
    }
    meta.utc = utcNow();
    meta.tag = format("%s-%s", meta.utc, meta.hash.substring(0, 7));
    return meta;
  }

}
