package io.vacco.oss.gitflow;

import com.google.gson.*;
import io.vacco.oss.gitflow.java.GsPluginJavaExtension;
import org.gradle.api.*;
import org.gradle.api.logging.*;

import static io.vacco.oss.gitflow.impl.GsOrgConfigs.*;
import static io.vacco.oss.gitflow.impl.GsBuildMetas.loadBuildMeta;
import static io.vacco.oss.gitflow.impl.GsPluginUtil.*;
import static io.vacco.oss.gitflow.schema.GsConstants.*;

public class GsPlugin implements Plugin<Project> {

  private static final Logger log = Logging.getLogger(GsPlugin.class);
  private static final Gson g = new GsonBuilder().setPrettyPrinting().create();

  @Override public void apply(Project project) {
    var orgConfig = loadOrgConfig(g, fileAtHomeDir(GS_LOCAL_CONFIG_FILE), loadRemoteConfigUrl(), localConfigUpdateDeltaMs);
    var meta = loadBuildMeta();
    var ext = project.getExtensions();

    log.warn("Build metadata: [{}]", meta);
    log.info("Organization configuration: {}", g.toJson(orgConfig));
    log.info("Applying base Java conventions");
    ext.create(GsPluginJavaExtension.name, GsPluginJavaExtension.class, project, orgConfig, meta);

    log.info("Applying project profiles");
    ext.create(GsPluginProfileExtension.name, GsPluginProfileExtension.class, project, orgConfig, meta);

    if (meta.target.isPublication()) {
      log.info("Applying publication conventions for environment [{}]", meta.target);
      project.afterEvaluate(p0 -> {
        var testTask = p0.getTasks().getByName(test);
        var checkTask = p0.getTasks().getByName(check);
        var publishTask = project.getTasks().findByName(publish);
        if (publishTask != null) {
          checkTask.dependsOn(publishTask);
          publishTask.mustRunAfter(testTask);
        }
      });
    }
  }
}
