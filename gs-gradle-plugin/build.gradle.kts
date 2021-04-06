buildscript { dependencies { classpath("org.jsonschema2pojo:jsonschema2pojo-gradle-plugin:1.1.0") } }

plugins { `java-library`; jacoco; `maven-publish`; signing }
apply { plugin(org.jsonschema2pojo.gradle.JsonSchemaPlugin::class.java) }

repositories {
  mavenCentral()
  gradlePluginPortal()
}

group = "io.vacco.oss"
version = "1.0.0"

dependencies {
  api(gradleApi())
  implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
  implementation("gradle.plugin.com.github.sherter.google-java-format:google-java-format-gradle-plugin:0.9")
  implementation("com.github.ben-manes:gradle-versions-plugin:0.31.0")
  implementation("io.vacco:io.vacco.cphell.gradle.plugin:1.8.0")

  testImplementation("io.github.j8spec:j8spec:3.0.0")
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
  withSourcesJar()
}

tasks.withType<JavaCompile> { options.compilerArgs.add("-Xlint:all") }
tasks.withType<Test> { this.testLogging { this.showStandardStreams = true } }

configure<org.jsonschema2pojo.gradle.JsonSchemaExtension> {
  setAnnotationStyle("none")
  targetPackage = "io.vacco.oss.gitflow.schema"
  includeAdditionalProperties = false
  usePrimitives = true
  includeConstructors = false
  includeGetters = false
  includeSetters = false
  includeToString = false
  includeHashcodeAndEquals = false
}

publishing {
  publications {
    create<MavenPublication>("Java") {
      from(components["java"])
      pom {
        name.set("Slim Gitflow for Java OSS")
        description.set("Gradle plugin with common conventions for OSS Java projects")
        url.set("https://github.com/vaccovecrana/gitflow-oss-java-slim")
        licenses {
          license {
            name.set("The Unlicense")
            url.set("https://unlicense.org")
          }
        }
        developers {
          developer {
            id.set("vacco")
            name.set("Vaccove Crana, LLC.")
            email.set("humans@vacco.io")
          }
        }
        scm {
          connection.set("https://github.com/vaccovecrana/gitflow-oss-java-slim.git")
          developerConnection.set("https://github.com/vaccovecrana/gitflow-oss-java-slim.git")
          url.set("https://github.com/vaccovecrana/gitflow-oss-java-slim.git")
        }
      }
    }
  }
  repositories {
    maven {
      name = "GithubPackages"
      setUrl("https://maven.pkg.github.com/vaccovecrana/gitflow-oss-java-slim/")
      credentials {
        username = System.getenv("VACCO_USER")
        password = System.getenv("VACCO_PASSWORD")
      }
    }
  }
}

signing {
  sign(publishing.publications["Java"])
  useInMemoryPgpKeys(System.getenv("MAVEN_SIGNING_KEY"), "")
}
