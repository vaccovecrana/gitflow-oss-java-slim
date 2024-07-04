package io.vacco.oss.gitflow.impl;

import com.google.gson.Gson;
import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.logging.*;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.*;

import static io.vacco.oss.gitflow.schema.GsConstants.*;
import static java.lang.String.*;

public class GsOrgConfigs {

  private static final Logger log = Logging.getLogger(GsOrgConfigs.class);

  private static boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }

  public static void sync(URL src, File dst, long lastModifiedMaxDelta) {
    try {
      long lastModifiedDelta = dst.exists() ? System.currentTimeMillis() - dst.lastModified() : Long.MAX_VALUE;
      if (lastModifiedDelta > lastModifiedMaxDelta) {
        log.warn("Updating file: [{}]", dst.getAbsolutePath());
        log.warn("Fetching [{}]", src.toString());
        var rbc = Channels.newChannel(src.openStream());
        try (var fos = new FileOutputStream(dst)) {
          fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
      } else {
        log.info("Skipping synced file: [{}]", dst.getAbsolutePath());
      }
    } catch (Exception e) {
      throw new IllegalStateException(format("Unable to load Org configuration from %s", src), e);
    }
  }

  public static String loadRemoteConfigUrl() {
    return System.getenv(GITHUB_INPUT_ORGCONFIG) != null
      ? System.getenv(GITHUB_INPUT_ORGCONFIG)
      : System.getenv(PLUGIN_ORGCONFIG);
  }

  public static GsOrgConfig loadOrgConfig(Gson g, File localConfig, String remoteConfigUrl, long updateDeltaMs) {
    try {
      if (remoteConfigUrl != null) {
        try (var ir = new InputStreamReader(new URL(remoteConfigUrl).openStream())) {
          var orgConfig = g.fromJson(ir, GsOrgConfig.class);
          var repos = new GsOrgRepo[] { orgConfig.internalRepo, orgConfig.snapshotsRepo, orgConfig.releasesRepo };
          Arrays.stream(repos).filter(Objects::nonNull).forEach(repo -> {
            if (repo.usernameEnvProperty != null && repo.passwordEnvProperty != null) {
              repo.username = System.getenv(repo.usernameEnvProperty);
              repo.password = System.getenv(repo.passwordEnvProperty);
            }
            if (isEmpty(repo.username) || isEmpty(repo.password)) {
              log.warn("Missing credentials for repository [{}]", repo.id);
            }
          });
          return orgConfig;
        }
      }
      else if (localConfig.exists()) {
        var localConf = g.fromJson(new FileReader(localConfig), GsLocalConfig.class);
        var orgConf = new File(localConfig.getParentFile(), format(GsConstants.GS_LOCAL_ORG_CONFIG_FMT, localConf.orgId));

        sync(new URL(localConf.orgConfigUrl), orgConf, updateDeltaMs);
        log.info("Executing local build.");

        var orgConfig = g.fromJson(new FileReader(orgConf), GsOrgConfig.class);
        if (orgConfig.internalRepo != null) {
          orgConfig.internalRepo.username = localConf.internalRepoUser;
          orgConfig.internalRepo.password = localConf.internalRepoPassword;
        }
        return orgConfig;
      } else throw new IllegalStateException(join("\n",
        "No CI org config found, and no local org config found.",
        "If this is a local code checkout, please define a minimal local org configuration"
      ));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
