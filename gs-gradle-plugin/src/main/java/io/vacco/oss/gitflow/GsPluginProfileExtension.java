package io.vacco.oss.gitflow;

import com.github.sherter.googlejavaformatgradleplugin.GoogleJavaFormatPlugin;
import io.vacco.oss.gitflow.schema.GsBranchCommit;
import io.vacco.oss.gitflow.schema.GsOrgConfig;
import io.vacco.oss.gitflow.sharedlib.GsSharedLibExtension;
import io.vacco.cphell.*;
import io.vacco.oss.gitflow.schema.GsConstants;
import org.gradle.api.*;
import org.gradle.api.logging.*;
import org.gradle.api.plugins.*;
import org.gradle.api.plugins.quality.*;
import org.gradle.api.tasks.*;

import java.io.File;
import java.net.URL;

import static java.util.Collections.*;
import static io.vacco.oss.gitflow.schema.GsConstants.*;
import static io.vacco.oss.gitflow.GsPluginUtil.*;

public class GsPluginProfileExtension {

  private static final Logger log = Logging.getLogger(GsPluginProfileExtension.class);
  public static final String name = "commonBuildProfiles";

  private final Project project;
  private final ExtensionContainer extensions;
  private final TaskContainer tasks;
  private final PluginContainer plugins;

  private final GsOrgConfig orgConfig;
  private final GsBranchCommit commit;

  public GsPluginProfileExtension(Project project, GsOrgConfig orgConfig, GsBranchCommit commit) {
    this.project = project;
    this.extensions = project.getExtensions();
    this.tasks = project.getTasks();
    this.plugins = project.getPlugins();
    this.orgConfig = orgConfig;
    this.commit = commit;
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

  public void addGoogleJavaFormat() {
    plugins.apply(GoogleJavaFormatPlugin.class);
    tasks.getByName(GsConstants.classes).dependsOn(tasks.findByName(googleJavaFormat));
  }

  public void sharedLibrary(boolean publish, boolean internal) {
    log.info("Applying shared library conventions");
    extensions.getByType(GsPluginProfileExtension.class)
        .extensions.create(GsSharedLibExtension.name, GsSharedLibExtension.class, project, orgConfig, commit, publish, internal);
  }

}
