package io.vacco.oss.gitflow.java;

import io.vacco.oss.gitflow.schema.*;
import io.vacco.oss.gitflow.impl.GsPluginUtil;
import org.gradle.api.*;
import org.gradle.api.plugins.*;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static io.vacco.oss.gitflow.impl.GsPluginUtil.*;
import static io.vacco.oss.gitflow.schema.GsBuildTarget.*;

public class GsPluginJavaExtension {

  public static final String name = "commonBuildCore";

  public GsPluginJavaExtension(Project project, GsOrgConfig orgConfig, GsBuildMeta meta) {
    var plugins = project.getPlugins();

    project.getRepositories().mavenCentral();
    GsPluginUtil.configureRepository(project, orgConfig, orgConfig.internalRepo, null);

    plugins.apply(JavaPlugin.class);
    plugins.apply(JacocoPlugin.class);

    addBaseConventions(project);
    setVersionFor(project, null, meta);
    addReleaseGating(project, meta);
  }

  private void addBaseConventions(Project p) {
    var ext = p.getExtensions();
    var tasks = p.getTasks();
    ext.configure(JavaPluginExtension.class, jXt -> {
      jXt.setSourceCompatibility(JavaVersion.VERSION_11);
      jXt.setTargetCompatibility(JavaVersion.VERSION_11);
    });
    tasks.withType(JavaCompile.class)
        .configureEach(t -> t.getOptions().getCompilerArgs().add("-Xlint:all"));
    tasks.withType(Test.class)
        .configureEach(t -> t.testLogging(tl -> tl.setShowStandardStreams(true)));
  }

  private void addReleaseGating(Project project, GsBuildMeta meta) {
    if (meta.target.isReleaseGated()) {
      project.getConfigurations().all(cfg -> cfg.resolutionStrategy(rs -> rs.eachDependency(details -> {
        var mvs = details.getRequested();
        var version = requireNonNull(mvs.getVersion());
        if (version.contains(SNAPSHOT.name()) || version.contains(MILESTONE.name())) {
          var errMsg = format("Do NOT use %s dependency: [%s] when creating %s artifacts.",
              version.contains(SNAPSHOT.name()) ? SNAPSHOT : MILESTONE,
              labelFor(mvs), RELEASE
          );
          throw new GradleException(errMsg);
        }
      })));
    }
  }

}
