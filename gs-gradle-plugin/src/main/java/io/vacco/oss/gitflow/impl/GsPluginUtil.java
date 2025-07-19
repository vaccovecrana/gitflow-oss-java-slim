package io.vacco.oss.gitflow.impl;

import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.logging.*;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
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

  public static MavenPublication getPublication(Project project, GsOrgConfig config) {
    var publishing = project.getExtensions().findByType(PublishingExtension.class);
    if (publishing == null) {
      throw new IllegalStateException("Publishing extension not found. Apply 'maven-publish' plugin.");
    }
    var publication = publishing.getPublications().findByName(config.publishing.id);
    if (!(publication instanceof MavenPublication)) {
      throw new IllegalStateException(format(
        "Maven publication [%s] not found.", config.publishing.id
      ));
    }
    return (MavenPublication) publication;
  }

  public static void defineMavenClassicRepository(RepositoryHandler rh, GsOrgRepo orgRepo,
                                                  Function<GsOrgRepo, String> urlTransform) {
    if (orgRepo == null) {
      return;
    }
    var targetUrl = urlTransform == null ? orgRepo.url : urlTransform.apply(orgRepo);
    log.info("Defining repository URL: [{}]", targetUrl);
    rh.maven(repo -> {
      repo.setName(orgRepo.id);
      repo.setUrl(targetUrl);
      if (orgRepo.username != null && orgRepo.password != null) {
        repo.credentials(crd -> {
          crd.setUsername(orgRepo.username);
          crd.setPassword(orgRepo.password);
        });
      } else {
        log.warn("Missing credentials for repository [{}]", orgRepo.id);
      }
    });
  }

}
