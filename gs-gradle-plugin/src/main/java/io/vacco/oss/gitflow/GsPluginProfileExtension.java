package io.vacco.oss.gitflow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.vacco.oss.gitflow.schema.*;
import io.vacco.oss.gitflow.sharedlib.GsSharedLibExtension;
import io.vacco.cphell.*;
import io.vacco.oss.gitflow.schema.GsConstants;
import org.gradle.api.*;
import org.gradle.api.logging.*;
import org.gradle.api.plugins.*;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.util.Objects;

import static io.vacco.oss.gitflow.impl.GsOrgConfigs.loadOrgConfig;
import static java.util.Collections.*;
import static io.vacco.oss.gitflow.schema.GsConstants.*;

public class GsPluginProfileExtension {

  private static final Logger log = Logging.getLogger(GsPluginProfileExtension.class);
  private static final Gson g = new GsonBuilder().setPrettyPrinting().create();

  public static final String name = "commonBuildProfiles";

  private final Project project;
  private final ExtensionContainer extensions;
  private final TaskContainer tasks;
  private final PluginContainer plugins;

  private final GsBuildMeta meta;
  public  final Property<GsOrgConfig> orgConfig;

  public GsPluginProfileExtension(Project project, GsBuildMeta meta) {
    this.project = project;
    this.extensions = project.getExtensions();
    this.orgConfig = project.getObjects().property(GsOrgConfig.class);
    this.tasks = project.getTasks();
    this.plugins = project.getPlugins();
    this.meta = Objects.requireNonNull(meta);
  }

  private void checkOrgConfig() {
    if (!this.orgConfig.isPresent()) {
      throw new IllegalStateException("Org config has not been loaded");
    }
  }

  public void addJ8Spec() {
    checkOrgConfig();
    project.getDependencies().add(testImplementation, orgConfig.get().devConfig.versions.j8Spec);
  }

  public void addClasspathHell() {
    checkOrgConfig();
    plugins.apply(ChPlugin.class);
    extensions.configure(ChPluginExtension.class, chXt -> {
      chXt.setSuppressExactDupes(true);
      chXt.setConfigurationsToScan(singletonList(project.getConfigurations().getByName(runtimeClasspath)));
      chXt.setResourceExclusions(chXt.commonResourceExclusions());
    });
    var cpTask = Objects.requireNonNull(tasks.findByName(checkClasspath));
    tasks.getByName(GsConstants.build).dependsOn(cpTask);
  }

  public void sharedLibrary(boolean publish, boolean internal) {
    checkOrgConfig();
    log.info("Applying shared library conventions");
    extensions.getByType(GsPluginProfileExtension.class).extensions.create(
      GsSharedLibExtension.name, GsSharedLibExtension.class,
      project, orgConfig.get(), meta, publish, internal
    );
  }

  public void orgConfigUrl(String url) {
    this.orgConfig.set(loadOrgConfig(g, url, localConfigUpdateDeltaMs));
    log.info("Organization configuration: {}", g.toJson(orgConfig.get()));
  }

}
