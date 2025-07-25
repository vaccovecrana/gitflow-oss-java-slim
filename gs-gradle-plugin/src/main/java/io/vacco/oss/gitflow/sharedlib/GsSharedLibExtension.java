package io.vacco.oss.gitflow.sharedlib;

import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.*;
import org.gradle.api.logging.*;
import org.gradle.api.plugins.*;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugins.signing.*;
import java.util.Arrays;
import java.util.function.Function;

import static io.vacco.oss.gitflow.impl.GsPluginUtil.*;
import static io.vacco.oss.gitflow.schema.GsConstants.*;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Objects.requireNonNull;

public class GsSharedLibExtension {

  private static final Logger log = Logging.getLogger(GsSharedLibExtension.class);
  public static final String name = "commonBuildSharedLibrary";

  private Function<GsOrgRepo, String> publishingUrlTransform;

  private boolean isPresent(Object s) {
    return s != null && !s.toString().isEmpty();
  }

  private String[] getLicense(Project p, boolean internalPublication) {
    if (internalPublication) return new String[] {"Proprietary", "Proprietary"};
    var libLicense = p.findProperty("libLicense");
    var libLicenseUrl = p.findProperty("libLicenseUrl");
    var ok = isPresent(libLicense) && isPresent(libLicenseUrl);
    if (!ok) {
      throw new GradleException("Missing license information for POM metadata.");
    }
    return new String[] { libLicense.toString(), libLicenseUrl.toString() };
  }

  public GsSharedLibExtension(Project project, GsOrgConfig orgConfig, GsBuildMeta meta,
                              boolean doPublish, boolean internal) {
    var plugins = project.getPlugins();
    var extensions = project.getExtensions();

    plugins.apply(JavaLibraryPlugin.class);

    if (doPublish) {
      log.info("Applying shared library publication support");
      extensions.configure(JavaPluginExtension.class, JavaPluginExtension::withSourcesJar);
      plugins.apply(MavenPublishPlugin.class);

      var pe = extensions.getByType(PublishingExtension.class);
      var libDesc = requireNonNull(project.findProperty(kLibDesc), format("please add a description property (%s) in gradle.properties", kLibDesc));
      var libGitUrl = requireNonNull(project.findProperty(kLibGitUrl), format("please add a project Git URL property (%s) in gradle.properties", kLibGitUrl));

      var mvn = pe.getPublications().create(orgConfig.publishing.id, MavenPublication.class, mp -> {
        mp.from(project.getComponents().getByName(java));
        mp.pom(pom -> {
          var license = getLicense(project, internal);
          pom.getName().set(project.getName());
          pom.getDescription().set(libDesc.toString());
          pom.getUrl().set(libGitUrl.toString());
          pom.licenses(lSpec -> lSpec.license(lic -> {
            lic.getName().set(license[0]);
            lic.getUrl().set(license[1]);
          }));
          pom.developers(dSpec -> dSpec.developer(d -> {
            d.getId().set(orgConfig.publishing.devId);
            d.getName().set(orgConfig.publishing.devContact);
            d.getEmail().set(orgConfig.publishing.devEmail);
          }));
          pom.scm(s -> {
            s.getConnection().set(libGitUrl.toString());
            s.getDeveloperConnection().set(libGitUrl.toString());
            s.getUrl().set(libGitUrl.toString());
          });
        });
      });

      if (meta.target.isPublication()) {
        if (internal) {
          defineMavenClassicRepository(pe.getRepositories(), orgConfig.internalRepo, publishingUrlTransform);
        } else {
          var signingKey = System.getenv(orgConfig.publishing.mavenSigningKeyEnvProperty);
          if (signingKey != null) {
            project.getPlugins().apply(SigningPlugin.class);
            var se = project.getExtensions().getByType(SigningExtension.class);
            se.useInMemoryPgpKeys(signingKey, "");
            se.sign(mvn);
            extensions.configure(JavaPluginExtension.class, JavaPluginExtension::withJavadocJar);
            var repo = meta.target.isSnapshot() ? orgConfig.snapshotsRepo : orgConfig.releasesRepo;
            try {
              var tasks = project.getTasks();
              if (repo.method == null) {
                throw new IllegalArgumentException(
                  format(join(" ",
                    "No publication method defined for repository [%s]. ",
                    "Specify one of %s"
                  ), repo.url, Arrays.toString(GsOrgRepoMethod.values()))
                );
              }
              switch (repo.method) {
                case PortalPublisherApiManual:
                case PortalPublisherApiAutomatic:
                  tasks.register(portalPublish, GsCentralPortalTask.class, t -> {
                    t.setGroup(publishing);
                    t.setDescription(GsCentralPortalTask.Description);
                    t.setRepo(repo);
                    t.setConfig(orgConfig);
                    t.setPublication(getPublication(project, orgConfig));
                    t.setBuildDir(project.getLayout().getBuildDirectory().get().getAsFile().toPath());
                  });
                  // I hate Gradle's lazy evaluation magic.
                  var signTask = format("sign%sPublication", orgConfig.publishing.id);
                  tasks.named(portalPublish).configure(pt -> pt.dependsOn(tasks.named(signTask)));
                  tasks.named(publish).configure(pt -> pt.dependsOn(tasks.named(portalPublish)));
                  break;
                case MavenClassic:
                  defineMavenClassicRepository(pe.getRepositories(), orgConfig.snapshotsRepo, publishingUrlTransform);
              }
            } catch (Exception e) {
              var msg = format(
                join(" ",
                  "Unable to configure repository [%s].",
                  "Verify environment variables: [%s, %s]",
                  "or provide org config values for username/password",
                  " - ", e.getMessage()
                ),
                repo.url, repo.usernameEnvProperty, repo.passwordEnvProperty
              );
              throw new IllegalStateException(msg, e);
            }
          } else {
            log.info("Missing signing key property [{}]", orgConfig.publishing.mavenSigningKeyEnvProperty);
          }
        }
      } else if (meta.target == GsBuildTarget.PRE_RELEASE) {
        extensions.configure(JavaPluginExtension.class, JavaPluginExtension::withJavadocJar);
      }
    }
  }

  public void setPublishingUrlTransform(Function<GsOrgRepo, String> publishingUrlTransform) {
    this.publishingUrlTransform = publishingUrlTransform;
  }

}
