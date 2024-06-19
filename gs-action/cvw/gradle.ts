import { promises, readFileSync } from "fs"
import { resolve } from "path"
import { GS_CONFIG_URL, GS_GH_EVENT, runCmd, tmp, utf8 } from "cvw/common"

const gradle = "gradle"

export const loadOrgConfig = (srcUrl: string): Promise<any> => {
  const configPath = resolve(process.env.RUNNER_WORKSPACE, "org-config.json")
  return runCmd("wget", ["--quiet", srcUrl, "--output-document", configPath])
    .then(() => JSON.parse(readFileSync(configPath, utf8)))
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
