import { existsSync, promises, readFileSync } from "fs"
import { resolve } from "path"
import { GS_CONFIG_URL, GS_GH_EVENT, runCmd, tmp, utf8 } from "cvw/common"

const gradle = "gradle"

export const loadOrgConfig = (srcUrl: string): Promise<any> => {
  const configPath = resolve(process.env.RUNNER_WORKSPACE, "org-config.json")
  return runCmd("wget", ["--quiet", srcUrl, "--output-document", configPath])
    .then(() => JSON.parse(readFileSync(configPath, utf8)))
}

export const loadJdk = (srcUrl: string, localPath: string): Promise<any> => {
  const cwd = process.cwd()
  const archivePath = resolve(localPath, "jdk.tar.gz")
  return promises.mkdir(localPath, { recursive: true })
    .then(() => process.chdir(localPath))
    .then(() => runCmd("wget", ["--quiet", srcUrl, "--output-document", archivePath]))
    .then(() => runCmd("tar", ["-xf", archivePath, "--strip-components=1"]))
    .then(() => process.chdir(cwd))
}

export const loadGradle = (gradleDist: string, gradleVer: string): Promise<any> => {
  return existsSync(gradleDist) ? Promise.resolve() : runCmd(
    "wget", ["--quiet", `https://services.gradle.org/distributions/${gradleVer}-bin.zip`]
  ).then(() => runCmd("unzip", ["-q", "-d", tmp, `${gradleVer}-bin.zip`]))
}

export const gradleBuild = (jdkRoot: string, gradleRoot: string, projectRoot: string, commit: any, orgConfigUrL: string): Promise<any> => {
  const buildArgs: string[] = ["build", "--info", "-b", resolve(projectRoot, "build.gradle.kts")]
  const {PATH} = process.env
  const gradleEnv = {...process.env, // secret values are passed in from the parent environment.
    JAVA_HOME: jdkRoot,
    PATH: `${PATH}:${resolve(gradleRoot, "bin")}`,
    [GS_GH_EVENT]: JSON.stringify(commit),
    [GS_CONFIG_URL]: orgConfigUrL
  } as any
  return runCmd(gradle, buildArgs, gradleEnv)
}
