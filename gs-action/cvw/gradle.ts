import { promises, readFileSync } from "fs"
import { resolve } from "path"
import { GS_CONFIG_URL, GS_GH_EVENT, runCmd, tmp, utf8 } from "cvw/common"

const gradle = "gradle"

export const loadOrgConfig = (srcUrl: string): Promise<any> => {
  const configPath = resolve(process.env.RUNNER_WORKSPACE, "org-config.json")
  return runCmd("wget", ["--quiet", srcUrl, "--output-document", configPath])
    .then(() => JSON.parse(readFileSync(configPath, utf8)))
}

export const loadJdk = (devConfig: any): Promise<any> => {
  const cwd = process.cwd()
  const localPath = resolve(tmp, "jdk")
  const archivePath = resolve(localPath, "jdk.tar.gz")
  return promises.mkdir(localPath, { recursive: true })
    .then(() => process.chdir(localPath))
    .then(() => runCmd("wget", ["--quiet", devConfig.jdkDistribution, "--output-document", archivePath]))
    .then(() => runCmd("tar",  ["--overwrite", "-xf", archivePath, "--strip-components=1"]))
    .then(() => process.chdir(cwd))
}

export const loadGradle = (devConfig: any): Promise<string> => {
  const cwd = process.cwd()
  const localPath = resolve(tmp, `gradle-${devConfig.gradleVersion}`)
  return promises.mkdir(localPath, {recursive: true})
    .then(() => process.chdir(tmp))
    .then(() => runCmd("wget", ["--quiet", devConfig.gradleDistribution, "--output-document", "gradle.zip"]))
    .then(() => runCmd("unzip", ["-o", "-q", "gradle.zip"]))
    .then(() => process.chdir(cwd))
    .then(() => localPath)
}

export const gradleBuild = (jdkRoot: string, gradleRoot: string, projectRoot: string, commit: any, orgConfigUrL: string): Promise<any> => {
  const gradleCmd = resolve(gradleRoot, "bin", gradle)
  const buildArgs: string[] = ["build", "-b", resolve(projectRoot, "build.gradle.kts")]
  const gradleEnv = {...process.env, // secret values are passed in from the parent environment.
    JAVA_HOME: jdkRoot,
    [GS_GH_EVENT]: JSON.stringify(commit),
    [GS_CONFIG_URL]: orgConfigUrL
  } as any
  return runCmd(gradleCmd, buildArgs, gradleEnv)
}
