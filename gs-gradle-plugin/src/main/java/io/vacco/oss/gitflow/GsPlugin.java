package io.vacco.oss.gitflow;

import com.google.gson.*;
import io.vacco.oss.gitflow.java.GsPluginJavaExtension;
import org.gradle.api.*;
import org.gradle.api.logging.*;

import static io.vacco.oss.gitflow.impl.GsBuildMetas.loadBuildMeta;
import static io.vacco.oss.gitflow.schema.GsConstants.*;

public class GsPlugin implements Plugin<Project> {

  private static final Logger log = Logging.getLogger(GsPlugin.class);

  @Override public void apply(Project project) {
    var meta = loadBuildMeta();
    var ext = project.getExtensions();

    log.warn("Build metadata: [{}]", meta);
    log.info("Applying project profiles");
    ext.create(GsPluginProfileExtension.name, GsPluginProfileExtension.class, project, meta);

    var javaExt = project.getExtensions().create(GsPluginJavaExtension.name, GsPluginJavaExtension.class, project, meta);
    javaExt.configure();

    project.afterEvaluate(p0 -> {
      log.info("Applying base Java conventions");
      javaExt.add();
    });

    if (meta.target.isPublication()) {
      log.info("Applying publication conventions for build type [{}]", meta.target);
      project.afterEvaluate(p0 -> {
        var buildTask = p0.getTasks().getByName(build);
        var testTask = p0.getTasks().getByName(test);
        var checkTask = p0.getTasks().getByName(check);
        var publishTask = p0.getTasks().findByName(publish);
        if (publishTask != null) {
          checkTask.dependsOn(publishTask);
          publishTask.mustRunAfter(testTask);
          buildTask.dependsOn(publishTask);
        }
      });
    }
  }
}
