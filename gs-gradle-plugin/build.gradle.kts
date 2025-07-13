plugins {
  `java-library`
  jacoco
  `maven-publish`
  signing
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

group = "io.vacco.oss.gitflow"
version = "1.8.0" // -SNAPSHOT

dependencies {
  api(gradleApi())
  implementation("io.vacco:io.vacco.cphell.gradle.plugin:1.8.0")
  implementation("com.google.code.gson:gson:2.10")
  implementation("gradle.plugin.com.github.sherter.google-java-format:google-java-format-gradle-plugin:0.9")
  testImplementation("io.github.j8spec:j8spec:3.0.1")
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
  withSourcesJar()
  withJavadocJar()
}

tasks.withType<JavaCompile> { options.compilerArgs.add("-Xlint:all") }
tasks.withType<Test> { this.testLogging { this.showStandardStreams = true } }

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
      name = "Snapshots"
      url = uri("https://central.sonatype.com/repository/maven-snapshots/")
      credentials {
        username = System.getenv("SONATYPE_USERNAME")
        password = System.getenv("SONATYPE_PASSWORD")
      }
    }
  }
}

signing {
  val key = System.getenv("MAVEN_SIGNING_KEY")
  if (key != null) {
    sign(publishing.publications["Java"])
    useInMemoryPgpKeys(key, "")
  }
}
