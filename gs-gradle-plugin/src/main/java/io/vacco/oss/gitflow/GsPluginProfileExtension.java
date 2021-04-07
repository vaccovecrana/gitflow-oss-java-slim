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

  public void addPmd() {
    try {
      if (orgConfig.devConfig.pmdRulesUrl == null) {
        throw new IllegalArgumentException("Missing PMD XML configuration URL in org config.");
      } else if (orgConfig.devConfig.versions == null || orgConfig.devConfig.versions.pmd == null) {
        throw new IllegalArgumentException("Missing PMD tool version in org config.");
      }
      plugins.apply(PmdPlugin.class);
      JavaPluginConvention javaPlugin = project.getConvention().getPlugin(JavaPluginConvention.class);
      SourceSetContainer sourceSets = javaPlugin.getSourceSets();
      URL pmd = new URL(orgConfig.devConfig.pmdRulesUrl);
      File pmdTmp = fileAtTempDir(String.format(GS_PMD_XML_FMT, orgConfig.orgId));

      GsPluginUtil.sync(pmd, pmdTmp, localConfigUpdateDeltaMs);
      extensions.configure(PmdExtension.class, pmdXt -> {
        pmdXt.setConsoleOutput(true);
        pmdXt.setIgnoreFailures(true);
        pmdXt.getIncrementalAnalysis().set(true);
        pmdXt.setSourceSets(singletonList(sourceSets.getByName(main)));
        pmdXt.setToolVersion(orgConfig.devConfig.versions.pmd);
        pmdXt.setRuleSetConfig(project.getResources().getText().fromFile(pmdTmp));
        pmdXt.setRuleSets(emptyList());
      });
    } catch (Exception e) { throw new IllegalStateException(e); }
  }

  public void addGoogleJavaFormat() {
    plugins.apply(GoogleJavaFormatPlugin.class);
    tasks.getByName(GsConstants.classes).dependsOn(tasks.findByName(googleJavaFormat));
  }

  public void strict() {
    addPmd();
    addGoogleJavaFormat();
    addClasspathHell();
  }

  public void sharedLibrary(boolean publish, boolean internal) {
    log.info("Applying shared library conventions");
    extensions.getByType(GsPluginProfileExtension.class)
        .extensions.create(GsSharedLibExtension.name, GsSharedLibExtension.class, project, orgConfig, commit, publish, internal);
  }

}
