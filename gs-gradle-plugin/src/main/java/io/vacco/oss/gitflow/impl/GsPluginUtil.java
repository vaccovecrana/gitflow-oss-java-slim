package io.vacco.oss.gitflow.impl;

import com.google.gson.Gson;
import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.logging.*;

import java.io.*;
import java.net.URL;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.String.*;
import static io.vacco.oss.gitflow.schema.GsBuildTarget.*;

public class GsPluginUtil {

  private static final Logger log = Logging.getLogger(GsPluginUtil.class);

  public static String labelFor(ModuleVersionSelector mvs) {
    return format("%s:%s:%s", mvs.getGroup(), mvs.getName(), mvs.getVersion());
  }

  public static String labelForVersion(String currentVersion, GsBuildMeta meta) {
    var dt = meta.target;
    if (dt.isMilestone() && !currentVersion.contains(MILESTONE.name())) {
      return format("%s-%s-%s", currentVersion, MILESTONE, meta.utc);
    } else if (dt.isSnapshot() && !currentVersion.contains(SNAPSHOT.name())) {
      return format("%s-%s", currentVersion, SNAPSHOT);
    }
    return currentVersion;
  }

  public static void setVersionFor(Project project, String rawVersion, GsBuildMeta meta) {
    // ugh... https://github.com/gradle/gradle/issues/11299
    project.allprojects(p -> p.afterEvaluate(p0 -> {
      p0.setVersion(labelForVersion(rawVersion != null ? rawVersion : p0.getVersion().toString(), meta));
      log.warn("Project build version: [{}, {}]", p0.getName(), p0.getVersion());
    }));
  }

  public static File fileAtHomeDir(String name) {
    return new File(System.getProperty("user.home"), name);
  }

  public static void sync(URL src, File dst, long lastModifiedMaxDelta) throws IOException {
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
  }

  private static boolean isEmpty(String s) {
    return s == null || s.isEmpty();
  }

  public static GsOrgConfig loadOrgConfig(Gson g, File localConfig, String remoteConfigUrl, long lastModifiedMaxDelta) {
    try {
      if (remoteConfigUrl != null) {
        try (var ir = new InputStreamReader(new URL(remoteConfigUrl).openStream())) {
          var orgConfig = g.fromJson(ir, GsOrgConfig.class);
          var repos = new GsOrgRepo[] { orgConfig.internalRepo, orgConfig.snapshotsRepo, orgConfig.releasesRepo };
          Arrays.stream(repos).filter(Objects::nonNull).forEach(repo -> {
            repo.username = System.getenv(repo.usernameEnvProperty);
            repo.password = System.getenv(repo.passwordEnvProperty);
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

        sync(new URL(localConf.orgConfigUrl), orgConf, lastModifiedMaxDelta);
        log.warn("Executing unmanaged build.");

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

  public static void configure(RepositoryHandler rh, GsOrgRepo orgRepo,
                               Function<GsOrgRepo, String> urlTransform) {
    if (orgRepo == null) {
      return;
    }
    var targetUrl = urlTransform == null ? orgRepo.url : urlTransform.apply(orgRepo);
    log.warn("Adding repository URL: [{}]", targetUrl);
    try {
      rh.maven(repo -> {
        repo.setName(orgRepo.id);
        repo.setUrl(targetUrl);
        if (orgRepo.username != null && orgRepo.password != null) {
          repo.credentials(crd -> {
            crd.setUsername(orgRepo.username);
            crd.setPassword(orgRepo.password);
          });
        }
      });
    } catch (Exception e) {
      var msg = format(
        join(" ",
          "No credentials for repository [%s].",
          "Verify environment variables: [%s, %s]",
          "or provide org config values for username/password"
        ),
        orgRepo.url, orgRepo.usernameEnvProperty, orgRepo.passwordEnvProperty
      );
      throw new IllegalStateException(msg, e);
    }
  }

}
