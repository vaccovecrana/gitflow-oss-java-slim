package io.vacco.oss.gitflow.impl;

import com.google.gson.Gson;
import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.logging.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;

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

  public static GsOrgConfig loadRemote(Gson g, String remoteConfigUrl) throws Exception {
    var rcUrl = new URI(remoteConfigUrl).toURL();
    try (var ir = new InputStreamReader(rcUrl.openStream())) {
      return g.fromJson(ir, GsOrgConfig.class);
    }
  }

  public static GsOrgConfig loadLocal(Gson g, File localConfigFile, GsLocalConfig localConfig, long updateDeltaMs) throws Exception {
    var confHash = Integer.toHexString(localConfig.orgConfigUrl.hashCode());
    var confName = format(GsConstants.GS_LOCAL_ORG_CONFIG_FMT, confHash);
    var orgConf = new File(localConfigFile.getParentFile(), confName);
    var ocUrl = URI.create(localConfig.orgConfigUrl).toURL();
    sync(ocUrl, orgConf, updateDeltaMs);
    log.info("Executing local build.");
    return g.fromJson(new FileReader(orgConf), GsOrgConfig.class);
  }

  public static GsOrgConfig process(GsOrgConfig orgConfig, GsLocalConfig localConfig) {
    var repos = new GsOrgRepo[] { orgConfig.internalRepo, orgConfig.snapshotsRepo, orgConfig.releasesRepo };
    for (var repo : repos) {
      if (repo != null) {
        if (repo.usernameEnvProperty != null && repo.passwordEnvProperty != null) {
          repo.username = System.getenv(repo.usernameEnvProperty);
          repo.password = System.getenv(repo.passwordEnvProperty);
        }
      }
    }
    if (orgConfig.internalRepo != null && localConfig != null) {
      orgConfig.internalRepo.username = localConfig.internalRepoUser;
      orgConfig.internalRepo.password = localConfig.internalRepoPassword;
    }
    for (var repo : repos) {
      if (repo != null && (isEmpty(repo.username) || isEmpty(repo.password))) {
        log.warn(
          "Missing credentials for repository [{}] [{}/{}]",
          repo.id, repo.usernameEnvProperty, repo.passwordEnvProperty
        );
      }
    }
    return orgConfig;
  }

  public static GsOrgConfig loadOrgConfig(Gson g, File localConfigFile, String remoteConfigUrl, long updateDeltaMs) {
    try {
      if (remoteConfigUrl != null) {
        return process(loadRemote(g, remoteConfigUrl), null);
      } else if (localConfigFile.exists()) {
        var localConf = g.fromJson(new FileReader(localConfigFile), GsLocalConfig.class);
        return process(loadLocal(g, localConfigFile, localConf, updateDeltaMs), localConf);
      } else throw new IllegalStateException(join("\n",
        "No CI org config found, and no local org config found.",
        "If this is a local code checkout, please define a minimal local org configuration"
      ));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

}
