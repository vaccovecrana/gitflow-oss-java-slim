import { existsSync } from "fs"
import { resolve } from "path"
import { GS_GH_EVENT, GS_SECRETS, runCmd, tmp } from "cvw/common"

const gradle = "gradle"

export const loadGradle = (gradleDist: string, gradleVer: string): Promise<any> => {
  return existsSync(gradleDist) ? Promise.resolve()
    : runCmd(
        "wget", ["--quiet", `https://services.gradle.org/distributions/${gradleVer}-bin.zip`]
      ).then(() => runCmd("unzip", ["-q", "-d", tmp, `${gradleVer}-bin.zip`]))
}

export const gradleBuild = (gradleRoot: string, projectRoot: string, commit: any): Promise<any> => {
  const buildArgs: string[] = ["build", "-b", resolve(projectRoot, "build.gradle.kts")]
  const {PATH} = process.env
  const secrets = JSON.parse(process.env[GS_SECRETS])
  const gradleEnv = {...process.env,
    PATH: `${PATH}:${resolve(gradleRoot, "bin")}`,
    [GS_GH_EVENT]: JSON.stringify(commit)
  } as any
  Object.keys(secrets).forEach(k => gradleEnv[k] = secrets[k])
  return runCmd(gradle, buildArgs, gradleEnv)
}
