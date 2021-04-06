import { existsSync } from "fs"
import { resolve } from "path"
import { runCmd, tmp } from "cvw/common"

export const gradleVer = "gradle-6.8.3"
const gradle = "gradle"

export const loadGradle = (gradleDist: string): Promise<any> => {
  return existsSync(gradleDist) ? Promise.resolve()
    : runCmd(
        "wget", ["--quiet", `https://services.gradle.org/distributions/${gradleVer}-bin.zip`]
      ).then(() => runCmd("unzip", ["-q", "-d", tmp, `${gradleVer}-bin.zip`]))
}

const gradleEnv = (gradleDistPath: string, commit: any) => {
  const {PATH} = process.env
  const env0 = {...process.env,
    PATH: `${PATH}:${resolve(gradleDistPath, "bin")}`,
    VACCO_BRANCH_COMMIT: JSON.stringify(commit)
  } as any
  return env0
}

export const gradleBuild = (gradleRoot: string, projectRoot: string, commit: any): Promise<any> => {
  const buildArgs: string[] = ["build", "-b", resolve(projectRoot, "build.gradle.kts")]
  const grdlEnv = gradleEnv(gradleRoot, commit)
  return runCmd(gradle, buildArgs, grdlEnv)
}
