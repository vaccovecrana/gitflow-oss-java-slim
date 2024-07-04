package io.vacco.oss.gitflow;

import com.google.gson.*;
import io.vacco.oss.gitflow.impl.*;
import io.vacco.oss.gitflow.schema.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.junit.runner.RunWith;

import java.io.File;

import static io.vacco.oss.gitflow.impl.GsOrgConfigs.loadOrgConfig;
import static j8spec.J8Spec.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class GsSpec {

  private static final Gson g = new GsonBuilder().setPrettyPrinting().create();

  static {
    it("Loads the org configuration", () -> {
      var testConfig = new File("./src/test/resources/test-config.json");
      var config = loadOrgConfig(g, testConfig.getParentFile(), testConfig.toURI().toURL().toString(), 5000);
      var meta = GsBuildMetas.loadBuildMeta();
      System.out.println(g.toJson(config));
      System.out.println(meta);
    });
    it("Load the org configuration from local bootstrap file", () -> {
      var localConfig = new File("./src/test/resources/local-config.json");
      var config = loadOrgConfig(g, localConfig, null, 5000);
      System.out.println(config);
    });
    it("Prints deploy target flags", () -> {
      for (var dt : GsBuildTarget.values()) {
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
