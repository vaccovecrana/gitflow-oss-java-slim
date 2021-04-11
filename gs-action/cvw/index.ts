import { error, info, setFailed, warning } from "@actions/core"
import { normalize, resolve } from "path"
import * as fs from "fs";

import { jdkRoot, utf8, BuildTarget, tmp, INPUT_ORGCONFIG } from "cvw/common"
import { loadJdk, loadGradle, gradleBuild, loadOrgConfig } from "cvw/gradle"

const event = JSON.parse(fs.readFileSync(process.env.GITHUB_EVENT_PATH, utf8))

const errorHandler = (e: any) => {
  const eJson = JSON.stringify(e, null, 2)
  if (Object.keys(e).length == 0) { error(e.message) }
  else { error(eJson) }
  setFailed(e)
}

const buildInit = (commit: any, buildTarget: BuildTarget): Promise<void> => {
  const orgConfigUrl = process.env[INPUT_ORGCONFIG]
  return loadOrgConfig(orgConfigUrl).then(orgConfig => {
    const {jdkDistribution} = orgConfig.devConfig
    const gradleVer = orgConfig.devConfig.versions.gradle
    const gradleDist = resolve(tmp, gradleVer)
    commit.buildTarget = buildTarget
    return loadJdk(jdkDistribution, jdkRoot)
      .then(() => loadGradle(gradleDist, gradleVer))
      .then(() => gradleBuild(jdkRoot, gradleDist, normalize(process.cwd()), commit, orgConfigUrl))
  })
}

const onCommit = (commit: any): Promise<any> => {
  const {ref} = commit

  info("*******************************************************************")
  info(`* Target ref: ${ref}`)
  info("*******************************************************************")

  if (ref && ref.includes("feature/")) {
    return buildInit(commit, BuildTarget.SNAPSHOT)
  } else if (ref && ref.includes("develop")) {
    return buildInit(commit, BuildTarget.MILESTONE)
  } else if (ref.includes("refs/tags")) {
    return buildInit(commit, BuildTarget.RELEASE)
  } else if (ref && (ref.includes("master") || ref.includes("main"))) {
    return buildInit(commit, BuildTarget.PRE_RELEASE)
  }
  warning(`Building non-managed ref combination: ${ref}`)
  return buildInit(commit, BuildTarget.LOCAL)
}

if (event.ref) {
  onCommit(event).catch(errorHandler)
}
