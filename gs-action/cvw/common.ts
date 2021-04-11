import { spawn } from "child_process"

export enum BuildTarget {
  SNAPSHOT = "SNAPSHOT",
  MILESTONE = "MILESTONE",
  RELEASE = "RELEASE",
  PRE_RELEASE = "PRE_RELEASE",
  LOCAL = "LOCAL"
}

export const utf8 = "utf-8"
export const tmp = "/tmp"
export const jdkRoot = "/tmp/jdk"

export const GS_GH_EVENT = "GS_GH_EVENT"
export const GS_CONFIG_URL = "GS_CONFIG_URL"

export const INPUT_ORGCONFIG = "INPUT_ORGCONFIG"

export const runCmd = (cmd: string, args: string[], env: any = undefined) => {
  console.log(`Running: ${cmd} ${args}`)
  return new Promise((resolve, reject) => {
    const stdoutLines: string[] = []
    const stdErrlines: string[] = []
    const proc = spawn(cmd, args, env ? {env} : undefined)
    proc.stdout.on("data", buf => {
      process.stdout.write(buf.toString())
      stdoutLines.push(buf.toString())
    })
    proc.stderr.on("data", buf => {
      process.stderr.write(buf.toString())
      stdErrlines.push(buf.toString())
    })
    proc.on("exit", code => {
      console.log(`process exit code: [${code}]`)
      const out = {stdoutLines, stdErrlines, code}
      return code != 0 ? reject(out) : resolve(out)
    })
  })
}
