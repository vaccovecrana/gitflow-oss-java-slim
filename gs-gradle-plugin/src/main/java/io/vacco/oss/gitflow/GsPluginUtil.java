package io.vacco.oss.gitflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.logging.*;

import java.io.*;
import java.net.URL;
import java.nio.channels.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static java.lang.String.format;
import static io.vacco.oss.gitflow.schema.GsBuildTarget.*;

public class GsPluginUtil {

  private static final Logger log = Logging.getLogger(GsPluginUtil.class);

  public static String labelFor(ModuleVersionSelector mvs) {
    return format("%s:%s:%s", mvs.getGroup(), mvs.getName(), mvs.getVersion());
  }

  private static String utcNow() {
    ZonedDateTime utcNow = ZonedDateTime.now(ZoneId.of("UTC"));
    return utcNow.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
  }

  public static String labelForVersion(String currentVersion, GsBranchCommit commit) {
    GsBuildTarget dt = commit.buildTarget;
    if (dt.isMilestone() && !currentVersion.contains(MILESTONE.name())) {
      return format("%s-%s-%s", currentVersion, MILESTONE, commit.utcNow);
    } else if (dt.isSnapshot() && !currentVersion.contains(SNAPSHOT.name())) {
      return format("%s-%s", currentVersion, SNAPSHOT);
    }
    return currentVersion;
  }

  public static void setVersionFor(Project project, String rawVersion, GsBranchCommit commit) {
    // ugh... https://github.com/gradle/gradle/issues/11299
    project.allprojects(p -> p.afterEvaluate(p0 -> {
      p0.setVersion(labelForVersion(rawVersion != null ? rawVersion : p0.getVersion().toString(), commit));
      log.warn("Project build version: [{}, {}]", p0.getName(), p0.getVersion());
    }));
  }

  public static File fileAtHomeDir(String name) {
    return new File(System.getProperty("user.home"), name);
  }

  public static File fileAtTempDir(String name) { return new File(System.getProperty("java.io.tmpdir"), name); }

  public static void sync(URL src, File dst, long lastModifiedMaxDelta) throws IOException {
    long lastModifiedDelta = dst.exists() ? System.currentTimeMillis() - dst.lastModified() : Long.MAX_VALUE;
    if (lastModifiedDelta > lastModifiedMaxDelta) {
      log.info("Syncing missing or outdated file: [{}]", dst.getAbsolutePath());
      log.info("Fetching [{}]", src.toString());
      ReadableByteChannel readableByteChannel = Channels.newChannel(src.openStream());
      FileOutputStream fileOutputStream = new FileOutputStream(dst);
      fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    } else {
      log.info("Skipping synced file: [{}]", dst.getAbsolutePath());
    }
  }

  private static boolean isEmpty(String s) {
    return s == null || s.length() == 0;
  }

  public static GsOrgConfig loadOrgConfig(ObjectMapper om, File localConfig, String remoteConfigUrl, long lastModifiedMaxDelta) {
    try {
      if (remoteConfigUrl != null) {
        GsOrgConfig orgConfig = om.readValue(new URL(remoteConfigUrl), GsOrgConfig.class);
        GsOrgRepo[] repos = { orgConfig.internalRepo, orgConfig.snapshotsRepo, orgConfig.releasesRepo };
        Arrays.stream(repos).filter(Objects::nonNull).forEach(repo -> {
          repo.username = System.getenv(repo.usernameEnvProperty);
          repo.password = System.getenv(repo.passwordEnvProperty);
          if (isEmpty(repo.username) || isEmpty(repo.password)) {
            log.warn("Missing credentials for repository [{}]", repo.id);
          }
        });
        return orgConfig;
      }
      else if (localConfig.exists()) {
        GsLocalConfig localConf = om.readValue(localConfig, GsLocalConfig.class);
        File orgConf = new File(localConfig.getParentFile(), format(GsConstants.GS_LOCAL_ORG_CONFIG_FMT, localConf.orgId));

        sync(new URL(localConf.orgConfigUrl), orgConf, lastModifiedMaxDelta);
        log.warn("Executing unmanaged build.");

        GsOrgConfig orgConfig = om.readValue(orgConf, GsOrgConfig.class);
        if (orgConfig.internalRepo != null) {
          orgConfig.internalRepo.username = localConf.internalRepoUser;
          orgConfig.internalRepo.password = localConf.internalRepoPassword;
        }
        return orgConfig;
      } else throw new IllegalStateException(String.join("\n",
          "No CI org config found, and no local org config found.",
          "If this is a local code checkout, please define a minimal local org configuration",
          "as specified in https://github.com/vaccovecrana/gitflow-oss-java-slim/blob/main/gs-gradle-plugin/src/main/resources/json/gs-local-config.json"
      ));
    } catch (Exception e) { throw new IllegalStateException(e); }
  }

  public static GsBranchCommit loadBuildCommit(GsOrgConfig config, ObjectMapper om) {
    try {
      GsBranchCommit commit = new GsBranchCommit();
      String githubActionsJson = System.getenv(GsConstants.GS_GH_EVENT);
      if (githubActionsJson != null) {
        commit = om.readValue(githubActionsJson, GsBranchCommit.class);
      } else {
        log.warn("No CI dev config parameters present. Skipping build publication(s).");
        commit.buildTarget = GsBuildTarget.LOCAL;
        commit.after = "C0C0C0C0";
      }
      commit.utcNow = utcNow();
      commit.shaTagTxt = format("%s-%s", commit.utcNow, commit.after.substring(0, 7));
      return commit;
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void configure(RepositoryHandler rh, GsOrgRepo orgRepo,
                               Function<GsOrgRepo, String> urlTransform) {
    if (orgRepo == null) {
      return;
    }
    String targetUrl = urlTransform == null ? orgRepo.url : urlTransform.apply(orgRepo);
    log.warn("Adding repository URL: [{}]", targetUrl);
    try {
      rh.maven(mvnRepo -> {
        mvnRepo.setName(orgRepo.id);
        mvnRepo.setUrl(targetUrl);
        mvnRepo.credentials(crd -> {
          crd.setUsername(orgRepo.username);
          crd.setPassword(orgRepo.password);
        });
      });
    } catch (Exception e) {
      String msg = format(
          "No credentials for repository [%s]. Verify environment variables: [%s, %s] or provide org config values for username/password",
          orgRepo.url, orgRepo.usernameEnvProperty, orgRepo.passwordEnvProperty
      );
      throw new IllegalStateException(msg, e);
    }
  }

}
