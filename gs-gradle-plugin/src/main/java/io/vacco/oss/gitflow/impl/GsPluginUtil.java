package io.vacco.oss.gitflow.impl;

import io.vacco.oss.gitflow.schema.*;
import io.vacco.oss.gitflow.sharedlib.GsCentralPortalTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.logging.*;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import java.util.Arrays;
import java.util.function.Function;

import static java.lang.String.*;
import static io.vacco.oss.gitflow.schema.GsBuildTarget.*;
import static io.vacco.oss.gitflow.schema.GsConstants.*;

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

  /*
   * Note: this effectively allows:
   * - Pulling/pushing jars from internal Maven repositories using classic PUT/username/password requests.
   * - Pushing jars to Maven Central with portal publisher API via a custom task. Sigh...
   */
  public static void configureRepository(Project p, GsOrgConfig orgConfig, GsOrgRepo orgRepo,
                                         Function<GsOrgRepo, String> urlTransform) {
    if (orgRepo == null) {
      return;
    }
    var rh = p.getRepositories();
    var tasks = p.getTasks();
    var targetUrl = urlTransform == null ? orgRepo.url : urlTransform.apply(orgRepo);
    log.info("Adding repository URL: [{}]", targetUrl);
    try {
      if (orgRepo.method == null) {
        throw new IllegalArgumentException(
          format(join(" ",
            "No publication method defined for repository [%s]. ",
            "Specify one of %s"
          ), orgRepo.url, Arrays.toString(GsOrgRepoMethod.values()))
        );
      }
      switch (orgRepo.method) {
        case PortalPublisherApiManual:
        case PortalPublisherApiAutomatic:
          tasks.register(portalPublish, GsCentralPortalTask.class, t -> {
            t.setGroup(publishing);
            t.setDescription(GsCentralPortalTask.Description);
            t.setRepo(orgRepo);
            t.setConfig(orgConfig);
            t.setPublication(getPublication(p, orgConfig));
            t.setBuildDir(p.getLayout().getBuildDirectory().get().getAsFile().toPath());
          });
          // I hate Gradle's lazy evaluation magic.
          var signTask = format("sign%sPublication", orgConfig.publishing.id);
          tasks.named(portalPublish).configure(pt -> pt.dependsOn(tasks.named(signTask)));
          tasks.named(build).configure(bt -> bt.dependsOn(tasks.named(portalPublish)));
          break;
        case MavenClassic:
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
      }
    } catch (Exception e) {
      var msg = format(
        join(" ",
          "Unable to configure repository [%s].",
          "Verify environment variables: [%s, %s]",
          "or provide org config values for username/password",
          " - ", e.getMessage()
        ),
        orgRepo.url, orgRepo.usernameEnvProperty, orgRepo.passwordEnvProperty
      );
      throw new IllegalStateException(msg, e);
    }
  }

}
