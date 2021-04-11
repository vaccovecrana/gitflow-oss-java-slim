package io.vacco.oss.gitflow.java;

import com.github.benmanes.gradle.versions.VersionsPlugin;
import com.github.benmanes.gradle.versions.reporter.result.DependencyOutdated;
import com.github.benmanes.gradle.versions.reporter.result.Result;
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask;
import io.vacco.oss.gitflow.schema.*;
import io.vacco.oss.gitflow.GsPluginUtil;
import groovy.lang.Closure;
import org.gradle.api.*;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.logging.*;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.*;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static io.vacco.oss.gitflow.GsPluginUtil.*;
import static io.vacco.oss.gitflow.schema.GsBuildTarget.*;

public class GsPluginJavaExtension {

  public static final String name = "commonBuildCore";
  private static final Logger log = Logging.getLogger(GsPluginJavaExtension.class);

  public GsPluginJavaExtension(Project project, GsOrgConfig orgConfig, GsBranchCommit commit) {

    PluginContainer plugins = project.getPlugins();
    TaskContainer tasks = project.getTasks();

    project.getRepositories().mavenCentral();
    GsPluginUtil.configure(project.getRepositories(), orgConfig.internalRepo, null);

    plugins.apply(JavaPlugin.class);
    plugins.apply(JacocoPlugin.class);

    addBaseConventions(project.getExtensions(), tasks);
    addDependencyUpdates(tasks, plugins, orgConfig.devConfig);
    setVersionFor(project, null, commit);
    addReleaseGating(project, commit);
  }

  private void addBaseConventions(ExtensionContainer ext, TaskContainer tasks) {
    ext.configure(JavaPluginExtension.class, jXt -> {
      jXt.setSourceCompatibility(JavaVersion.VERSION_11);
      jXt.setTargetCompatibility(JavaVersion.VERSION_11);
    });
    tasks.withType(JavaCompile.class)
        .configureEach(t -> t.getOptions().getCompilerArgs().add("-Xlint:all"));
    tasks.withType(Test.class)
        .configureEach(t -> t.testLogging(tl -> tl.setShowStandardStreams(true)));
  }

  private void addDependencyUpdates(TaskContainer tasks, PluginContainer plugins, GsDevConfig devConfig) {
    plugins.apply(VersionsPlugin.class);
    Closure<Result> rfn = new Closure<Result>(this) {
      @Override public Result call(Object ... args) {
        Result result = (Result) args[0];
        if (!result.getOutdated().getDependencies().isEmpty()) {
          for (DependencyOutdated dep : result.getOutdated().getDependencies()) {
            for (String group : devConfig.dependencyExcludedGroups) {
              if (!dep.getGroup().contains(group)) {
                log.warn("Outdated upstream dependency: {}:{} [{} -> {}]",
                    dep.getGroup(), dep.getName(), dep.getVersion(), dep.getAvailable().getMilestone());
              }
            }
          }
        }
        return result;
      }
    };

    tasks.withType(DependencyUpdatesTask.class, t -> t.setOutputFormatter(rfn));
    tasks.getByName(GsConstants.build).dependsOn(tasks.findByName("dependencyUpdates"));
  }

  private void addReleaseGating(Project project, GsBranchCommit commit) {
    if (commit.buildTarget.isReleaseGated()) {
      project.getConfigurations().all(cfg -> cfg.resolutionStrategy(rs -> rs.eachDependency(details -> {
        ModuleVersionSelector mvs = details.getRequested();
        String version = requireNonNull(mvs.getVersion());
        if (version.contains(SNAPSHOT.name()) || version.contains(MILESTONE.name())) {
          String errMsg = format("Do NOT use %s dependency: [%s] when creating %s artifacts.",
              version.contains(SNAPSHOT.name()) ? SNAPSHOT : MILESTONE,
              labelFor(mvs), RELEASE
          );
          throw new GradleException(errMsg);
        }
      })));
    }
  }

}
