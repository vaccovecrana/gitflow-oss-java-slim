package io.vacco.oss.gitflow.sharedlib;

import io.vacco.oss.gitflow.schema.*;
import io.vacco.oss.gitflow.GsPluginUtil;
import org.gradle.api.*;
import org.gradle.api.logging.*;
import org.gradle.api.plugins.*;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.plugins.signing.*;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class GsSharedLibExtension {

  private static final Logger log = Logging.getLogger(GsSharedLibExtension.class);
  public static final String name = "commonBuildSharedLibrary";

  private Function<GsOrgRepo, String> publishingUrlTransform;

  private boolean isPresent(Object s) { return s != null && s.toString().length() > 0; }

  private String[] getLicense(Project p, boolean internalPublication) {
    if (internalPublication) return new String[] {"Proprietary", "Proprietary"};
    Object libLicense = p.findProperty("libLicense");
    Object libLicenseUrl = p.findProperty("libLicenseUrl");
    boolean ok = isPresent(libLicense) && isPresent(libLicenseUrl);
    if (!ok) { throw new GradleException("Missing license information for POM metadata."); }
    return new String[] {libLicense.toString(), libLicenseUrl.toString()};
  }

  public GsSharedLibExtension(Project project, GsOrgConfig orgConfig, GsBranchCommit commit,
                              boolean publish, boolean internal) {

    PluginContainer plugins = project.getPlugins();
    ExtensionContainer extensions = project.getExtensions();

    plugins.apply(JavaLibraryPlugin.class);

    if (publish) {
      log.info("Applying shared library publication support");
      extensions.configure(JavaPluginExtension.class, JavaPluginExtension::withSourcesJar);
      plugins.apply(MavenPublishPlugin.class);

      PublishingExtension pe = extensions.getByType(PublishingExtension.class);

      MavenPublication mvn = pe.getPublications().create(orgConfig.publishing.id, MavenPublication.class, mp -> {
        mp.from(project.getComponents().getByName("java"));
        mp.pom(pom -> {
          Object libDesc = requireNonNull(project.property("libDesc"));
          Object libGitUrl = requireNonNull(project.property("libGitUrl"));
          String[] license = getLicense(project, internal);

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

      if (commit.buildTarget.isPublication()) {
        if (internal) {
          pe.getRepositories().maven(m -> GsPluginUtil.configure(m, orgConfig.internalRepo, publishingUrlTransform));
        } else {
          project.getPlugins().apply(SigningPlugin.class);
          SigningExtension se = project.getExtensions().getByType(SigningExtension.class);
          se.useInMemoryPgpKeys(System.getenv(orgConfig.publishing.mavenSigningKeyEnvProperty), "");
          se.sign(mvn);
          extensions.configure(JavaPluginExtension.class, JavaPluginExtension::withJavadocJar);
          if (commit.buildTarget.isSnapshot()) {
            pe.getRepositories().maven(m -> GsPluginUtil.configure(m, orgConfig.snapshotsRepo, publishingUrlTransform));
          } else {
            pe.getRepositories().maven(m -> GsPluginUtil.configure(m, orgConfig.releasesRepo, publishingUrlTransform));
          }
        }
      } else if (commit.buildTarget == GsBuildTarget.PRE_RELEASE) {
        extensions.configure(JavaPluginExtension.class, JavaPluginExtension::withJavadocJar);
      }
    }
  }

  public void setPublishingUrlTransform(Function<GsOrgRepo, String> publishingUrlTransform) {
    this.publishingUrlTransform = publishingUrlTransform;
  }

}
