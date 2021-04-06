import { error, info, setFailed, warning } from "@actions/core"
import { normalize, resolve } from "path"

import { BuildTarget, tmp } from "cvw/common"
import { gradleVer, loadGradle, gradleBuild } from "cvw/gradle"

const event = JSON.parse(process.env.GH_EVENT)

const errorHandler = (e: any) => {
  const eJson = JSON.stringify(e, null, 2)
  if (Object.keys(e).length == 0) { error(e.message) }
  else { error(eJson) }
  setFailed(e)
}

const buildInit = (commit: any, buildTarget: BuildTarget): Promise<void> => {
  const gradleDist = resolve(tmp, gradleVer)
  commit.buildTarget = buildTarget
  return loadGradle(gradleDist).then(() => gradleBuild(gradleDist, normalize(process.cwd()), commit))
}

const onCommit = (commit: any): Promise<any> => {
  const {ref, base_ref} = commit
  const target = `[${base_ref} <== ${ref}]`

  info("*******************************************************************")
  info(`* Commit refs: ${target}`)
  info("*******************************************************************")

  if (ref && ref.includes("feature/")) {
    return buildInit(commit, BuildTarget.Int)
  } else if (ref && ref.includes("develop")) {
    return buildInit(commit, BuildTarget.Stage)
  } else if (ref.includes("refs/tags")) {
    return buildInit(commit, BuildTarget.Prod)
  } else if (ref && (ref.includes("master") || ref.includes("main"))) {
    return buildInit(commit, BuildTarget.PreRelease)
  }
  warning(`Building non-managed ref combination: ${target}`)
  return buildInit(commit, BuildTarget.Local)
}

if (event.ref) {
  onCommit(event).catch(errorHandler)
}
