package io.vacco.oss.gitflow.sharedlib;

import io.vacco.oss.gitflow.schema.*;
import org.gradle.api.*;
import org.gradle.api.logging.*;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.*;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.*;

import static io.vacco.oss.gitflow.schema.GsConstants.*;
import static java.lang.String.format;

public class GsCentralPortalTask extends DefaultTask {

  private static final Logger log = Logging.getLogger(GsCentralPortalTask.class);

  public  static final String Description = "Publishes artifacts to Sonatype Central Portal";
  private static final String BOUNDARY = "----GsCentralPortalBoundary" + UUID.randomUUID();
  private static final String CRLF = "\r\n";

  private GsOrgRepo         repo;
  private GsOrgConfig       config;
  private MavenPublication  publication;
  private Path              buildDir;

  private List<Path> collectArtifacts() {
    var artifacts = new ArrayList<Path>();
    var artifactId = publication.getArtifactId();
    var version = publication.getVersion();
    artifacts.add(buildDir.resolve(libs).resolve(format("%s-%s.jar", artifactId, version)));
    artifacts.add(buildDir.resolve(libs).resolve(format("%s-%s-javadoc.jar", artifactId, version)));
    artifacts.add(buildDir.resolve(libs).resolve(format("%s-%s-sources.jar", artifactId, version)));
    artifacts.add(buildDir.resolve(publications).resolve(config.publishing.id).resolve("pom-default.xml"));
    var signedArtifacts = new ArrayList<Path>();
    for (var artifact : artifacts) {
      var ascFile = artifact.getParent().resolve(artifact.getFileName() + ".asc");
      if (Files.exists(ascFile)) {
        signedArtifacts.add(ascFile);
      } else {
        log.warn("Missing GPG signature: {}", ascFile);
      }
    }
    artifacts.addAll(signedArtifacts);
    return artifacts.stream().filter(Files::exists).collect(Collectors.toList());
  }

  private void generateChecksums(List<Path> artifacts) throws IOException, NoSuchAlgorithmException {
    var algoList = new String[] {"MD5", "SHA-1"};
    var checksums = new ArrayList<Path>();
    for (var artifact : artifacts) {
      for (var algo : algoList) {
        var checksumFileName = artifact.getFileName() + "." + algo.toLowerCase().replace("-", "");
        var checksumFile = artifact.getParent().resolve(checksumFileName);
        var digest = MessageDigest.getInstance(algo);
        try (var input = Files.newInputStream(artifact)) {
          var buffer = new byte[8192];
          int bytesRead;
          while ((bytesRead = input.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
          }
        }
        var checksum = digest.digest();
        var hex = new StringBuilder();
        for (var b : checksum) {
          hex.append(format("%02x", b));
        }
        Files.write(checksumFile, hex.toString().getBytes(StandardCharsets.UTF_8));
        checksums.add(checksumFile);
      }
    }
    artifacts.addAll(checksums);
  }

  private void createZipBundle(List<Path> artifacts, Path bundlePath) throws IOException {
    Files.createDirectories(bundlePath.getParent());
    var artifactId = publication.getArtifactId();
    var version = publication.getVersion();
    var groupId = publication.getGroupId();
    var pomTargetName = format("%s-%s.pom", artifactId, version);
    var mavenPath = format("%s/%s/%s/", groupId.replace('.', '/'), artifactId, version);
    try (var zipOut = new ZipOutputStream(Files.newOutputStream(bundlePath))) {
      var zipEntries = new HashSet<String>();
      for (var artifact : artifacts) {
        var fileName = artifact.getFileName().toString();
        var entryName = fileName.startsWith("pom-default.xml") ? fileName.replace("pom-default.xml", pomTargetName) : fileName;
        var fullEntryName = mavenPath + entryName;
        if (!zipEntries.add(fullEntryName)) {
          log.warn("Duplicate ZIP entry ignored: {}", fullEntryName);
          continue;
        }
        var entry = new ZipEntry(fullEntryName);
        zipOut.putNextEntry(entry);
        Files.copy(artifact, zipOut);
        zipOut.closeEntry();
        log.info("Added to ZIP: {}", fullEntryName);
      }
    }
    log.warn("\uD83D\uDCE6 Created bundle: {}", bundlePath);
  }

  private void uploadBundle(Path bundlePath, String bearerToken, String artifactId, String version) throws IOException, InterruptedException {
    var client = HttpClient.newBuilder().build();
    var requestBody = new ByteArrayOutputStream();

    requestBody.write(("--" + BOUNDARY + CRLF).getBytes(StandardCharsets.UTF_8));
    requestBody.write(("Content-Disposition: form-data; name=\"bundle\"; filename=\"" + bundlePath.getFileName() + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
    requestBody.write(("Content-Type: application/octet-stream" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
    Files.copy(bundlePath, requestBody);
    requestBody.write((CRLF + "--" + BOUNDARY + "--" + CRLF).getBytes(StandardCharsets.UTF_8));

    var uriParts = new StringBuilder(repo.url).append("?");
    if (repo.method == GsOrgRepoMethod.PortalPublisherApiAutomatic) {
      uriParts.append("publishingType=AUTOMATIC").append("&");
    } else if (repo.method == GsOrgRepoMethod.PortalPublisherApiManual) {
      uriParts.append("publishingType=USER_MANAGED").append("&");
    }
    uriParts.append("name=").append(artifactId).append("-").append(version);

    var uri = URI.create(uriParts.toString());
    log.info("Request URI: [{}]", uri);

    var request = HttpRequest.newBuilder()
      .uri(uri)
      .header("Authorization", "Bearer " + bearerToken)
      .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
      .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody.toByteArray()))
      .build();
    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IOException(format("Upload failed: %d - %s", response.statusCode(), response.body()));
    }
    log.warn("\uD83C\uDF89 Successfully uploaded bundle for {}:{}", artifactId, version);
  }

  @TaskAction public void publishToCentralPortal() {
    try {
      var rawToken = format("%s:%s", repo.username, repo.password);
      var bearerToken = Base64.getEncoder().encodeToString(
        rawToken.getBytes(StandardCharsets.UTF_8)
      );
      var artifactId = publication.getArtifactId();
      var version = publication.getVersion();
      var bundleName = format("%s-%s-bundle.zip", artifactId, version);
      var bundlePath = buildDir.resolve(bundleName);
      var artifacts = collectArtifacts();
      generateChecksums(artifacts);
      createZipBundle(artifacts, bundlePath);
      uploadBundle(bundlePath, bearerToken, artifactId, version);
    } catch (Exception e) {
      throw new TaskExecutionException(this, e);
    }
  }

  public void setRepo(GsOrgRepo repo) {
    this.repo = Objects.requireNonNull(repo);
  }

  public void setConfig(GsOrgConfig config) {
    this.config = Objects.requireNonNull(config);
  }

  public void setPublication(MavenPublication publication) {
    this.publication = Objects.requireNonNull(publication);
  }

  public void setBuildDir(Path buildDir) {
    this.buildDir = Objects.requireNonNull(buildDir);
  }

}