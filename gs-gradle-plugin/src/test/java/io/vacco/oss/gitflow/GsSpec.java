package io.vacco.oss.gitflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vacco.oss.gitflow.schema.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.io.File;

import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class GsSpec {

  private static final ObjectMapper om = new ObjectMapper();

  static {
    it("Loads the org configuration", () -> {
      File testConfig = new File("./src/test/resources/test-config.json");
      GsOrgConfig config = GsPluginUtil.loadOrgConfig(om, testConfig.getParentFile(), testConfig.toURI().toURL().toString(), 5000);
      GsBranchCommit commit = GsPluginUtil.loadBuildCommit(config, om);

      System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(config));
      System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(commit));
      System.out.println(commit.buildTarget);
      System.out.println(GsPluginUtil.loadCredentials(config.internalRepo));
      System.out.println(GsPluginUtil.loadCredentials(config.snapshotsRepo));
      System.out.println(GsPluginUtil.loadCredentials(config.releasesRepo));
    });
    it("Load the org configuration from local bootstrap file", () -> {
      File localConfig = new File("./src/test/resources/local-config.json");
      GsOrgConfig config = GsPluginUtil.loadOrgConfig(om, localConfig, null, 5000);
      System.out.println(config);
    });
    it("Prints deploy target flags", () -> {
      for (GsBuildTarget dt : GsBuildTarget.values()) {
        System.out.printf(
            "[snapshot: %s, milestone: %s, publish: %s, relGate: %s] %s%n",
            dt.isSnapshot(), dt.isMilestone(),
            dt.isPublication(), dt.isReleaseGated(),
            dt
        );
      }
    });
  }

}
