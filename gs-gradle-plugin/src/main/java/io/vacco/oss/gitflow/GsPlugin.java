package io.vacco.oss.gitflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import io.vacco.oss.gitflow.java.GsPluginJavaExtension;
import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.*;
import org.gradle.api.logging.*;
import org.gradle.api.plugins.*;

import static io.vacco.oss.gitflow.GsPluginUtil.*;
import static io.vacco.oss.gitflow.schema.GsConstants.*;

public class GsPlugin implements Plugin<Project> {

  private static final Logger log = Logging.getLogger(GsPlugin.class);
  private static final ObjectMapper om = new ObjectMapper();

  @Override public void apply(Project project) {
    GsOrgConfig orgConfig = loadOrgConfig(
        om, fileAtHomeDir(GS_LOCAL_CONFIG_FILE),
        System.getenv(GsConstants.GS_CONFIG_URL),
        GsConstants.localConfigUpdateDeltaMs
    );
    GsBranchCommit commit = loadBuildCommit(orgConfig, om);
    ExtensionContainer ext = project.getExtensions();

    try {
      ObjectWriter ow = om.writerWithDefaultPrettyPrinter();
      log.info("Organization configuration: {}", ow.writeValueAsString(orgConfig));
      GsBranchCommit c0 = om.readValue(om.writeValueAsString(commit), GsBranchCommit.class);
      log.warn("Build commit: {}", ow.writeValueAsString(c0));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }

    log.info("Applying base Java conventions");
    ext.create(GsPluginJavaExtension.name, GsPluginJavaExtension.class, project, orgConfig, commit);

    log.info("Applying project profiles");
    ext.create(GsPluginProfileExtension.name, GsPluginProfileExtension.class, project, orgConfig, commit);

    if (commit.buildTarget.isPublication()) {
      log.info("Applying publication conventions for environment [{}]", commit.buildTarget);
      project.afterEvaluate(p0 -> {
        Task buildTask = p0.getTasks().getByName(build);
        Task publishTask = project.getTasks().findByName(publish);
        if (publishTask != null) {
          buildTask.dependsOn(publishTask);
        }
      });
    }
  }
}
