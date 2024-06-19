import { error, info, setFailed, warning } from "@actions/core"
import { normalize } from "path"
import * as fs from "fs";

import { jdkRoot, utf8, BuildTarget, INPUT_ORGCONFIG } from "cvw/common"
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
    commit.buildTarget = buildTarget
    return loadJdk(orgConfig.devConfig)
      .then(() => loadGradle(orgConfig.devConfig))
      .then(gradleRoot => gradleBuild(jdkRoot, gradleRoot, normalize(process.cwd()), commit, orgConfigUrl))
  })
}

const onCommit = (commit: any): Promise<any> => {
  const {ref} = commit

  // TODO pass this down to Gradle as env var
  info("*******************************************************************")
  info(`* Target ref: ${ref}`)
  info("*******************************************************************")


  warning(`Building non-managed ref combination: ${ref}`)
  return buildInit(commit, BuildTarget.LOCAL)
}

if (event.ref) {
  onCommit(event).catch(errorHandler)
}
