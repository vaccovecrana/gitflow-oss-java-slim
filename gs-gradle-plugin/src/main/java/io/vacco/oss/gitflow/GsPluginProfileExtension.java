package io.vacco.oss.gitflow;

import io.vacco.oss.gitflow.schema.*;
import io.vacco.oss.gitflow.sharedlib.GsSharedLibExtension;
import io.vacco.cphell.*;
import io.vacco.oss.gitflow.schema.GsConstants;
import org.gradle.api.*;
import org.gradle.api.logging.*;
import org.gradle.api.plugins.*;
import org.gradle.api.tasks.*;

import java.util.Objects;

import static java.util.Collections.*;
import static io.vacco.oss.gitflow.schema.GsConstants.*;

public class GsPluginProfileExtension {

  private static final Logger log = Logging.getLogger(GsPluginProfileExtension.class);
  public static final String name = "commonBuildProfiles";

  private final Project project;
  private final ExtensionContainer extensions;
  private final TaskContainer tasks;
  private final PluginContainer plugins;

  private final GsOrgConfig orgConfig;
  private final GsBuildMeta meta;

  public GsPluginProfileExtension(Project project, GsOrgConfig orgConfig, GsBuildMeta meta) {
    this.project = project;
    this.extensions = project.getExtensions();
    this.tasks = project.getTasks();
    this.plugins = project.getPlugins();
    this.orgConfig = Objects.requireNonNull(orgConfig);
    this.meta = Objects.requireNonNull(meta);
  }

  public void addJ8Spec() {
    project.getDependencies().add(testImplementation, orgConfig.devConfig.versions.j8Spec);
  }

  public void addClasspathHell() {
    plugins.apply(ChPlugin.class);
    extensions.configure(ChPluginExtension.class, chXt -> {
      chXt.setSuppressExactDupes(true);
      chXt.setConfigurationsToScan(singletonList(project.getConfigurations().getByName(runtimeClasspath)));
      chXt.setResourceExclusions(chXt.commonResourceExclusions());
    });
    tasks.getByName(GsConstants.build).dependsOn(tasks.findByName(checkClasspath));
  }

  public void sharedLibrary(boolean publish, boolean internal) {
    log.info("Applying shared library conventions");
    extensions.getByType(GsPluginProfileExtension.class).extensions.create(
      GsSharedLibExtension.name, GsSharedLibExtension.class,
      project, orgConfig, meta, publish, internal
    );
  }

}
