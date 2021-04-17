/******/ (() => { // webpackBootstrap
/******/ 	"use strict";
/******/ 	var __webpack_modules__ = ({

/***/ 265:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
const os = __importStar(__webpack_require__(87));
const utils_1 = __webpack_require__(570);
/**
 * Commands
 *
 * Command Format:
 *   ::name key=value,key=value::message
 *
 * Examples:
 *   ::warning::This is the message
 *   ::set-env name=MY_VAR::some value
 */
function issueCommand(command, properties, message) {
    const cmd = new Command(command, properties, message);
    process.stdout.write(cmd.toString() + os.EOL);
}
exports.issueCommand = issueCommand;
function issue(name, message = '') {
    issueCommand(name, {}, message);
}
exports.issue = issue;
const CMD_STRING = '::';
class Command {
    constructor(command, properties, message) {
        if (!command) {
            command = 'missing.command';
        }
        this.command = command;
        this.properties = properties;
        this.message = message;
    }
    toString() {
        let cmdStr = CMD_STRING + this.command;
        if (this.properties && Object.keys(this.properties).length > 0) {
            cmdStr += ' ';
            let first = true;
            for (const key in this.properties) {
                if (this.properties.hasOwnProperty(key)) {
                    const val = this.properties[key];
                    if (val) {
                        if (first) {
                            first = false;
                        }
                        else {
                            cmdStr += ',';
                        }
                        cmdStr += `${key}=${escapeProperty(val)}`;
                    }
                }
            }
        }
        cmdStr += `${CMD_STRING}${escapeData(this.message)}`;
        return cmdStr;
    }
}
function escapeData(s) {
    return utils_1.toCommandValue(s)
        .replace(/%/g, '%25')
        .replace(/\r/g, '%0D')
        .replace(/\n/g, '%0A');
}
function escapeProperty(s) {
    return utils_1.toCommandValue(s)
        .replace(/%/g, '%25')
        .replace(/\r/g, '%0D')
        .replace(/\n/g, '%0A')
        .replace(/:/g, '%3A')
        .replace(/,/g, '%2C');
}
//# sourceMappingURL=command.js.map

/***/ }),

/***/ 225:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
const command_1 = __webpack_require__(265);
const file_command_1 = __webpack_require__(108);
const utils_1 = __webpack_require__(570);
const os = __importStar(__webpack_require__(87));
const path = __importStar(__webpack_require__(622));
/**
 * The code to exit an action
 */
var ExitCode;
(function (ExitCode) {
    /**
     * A code indicating that the action was successful
     */
    ExitCode[ExitCode["Success"] = 0] = "Success";
    /**
     * A code indicating that the action was a failure
     */
    ExitCode[ExitCode["Failure"] = 1] = "Failure";
})(ExitCode = exports.ExitCode || (exports.ExitCode = {}));
//-----------------------------------------------------------------------
// Variables
//-----------------------------------------------------------------------
/**
 * Sets env variable for this action and future actions in the job
 * @param name the name of the variable to set
 * @param val the value of the variable. Non-string values will be converted to a string via JSON.stringify
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function exportVariable(name, val) {
    const convertedVal = utils_1.toCommandValue(val);
    process.env[name] = convertedVal;
    const filePath = process.env['GITHUB_ENV'] || '';
    if (filePath) {
        const delimiter = '_GitHubActionsFileCommandDelimeter_';
        const commandValue = `${name}<<${delimiter}${os.EOL}${convertedVal}${os.EOL}${delimiter}`;
        file_command_1.issueCommand('ENV', commandValue);
    }
    else {
        command_1.issueCommand('set-env', { name }, convertedVal);
    }
}
exports.exportVariable = exportVariable;
/**
 * Registers a secret which will get masked from logs
 * @param secret value of the secret
 */
function setSecret(secret) {
    command_1.issueCommand('add-mask', {}, secret);
}
exports.setSecret = setSecret;
/**
 * Prepends inputPath to the PATH (for this action and future actions)
 * @param inputPath
 */
function addPath(inputPath) {
    const filePath = process.env['GITHUB_PATH'] || '';
    if (filePath) {
        file_command_1.issueCommand('PATH', inputPath);
    }
    else {
        command_1.issueCommand('add-path', {}, inputPath);
    }
    process.env['PATH'] = `${inputPath}${path.delimiter}${process.env['PATH']}`;
}
exports.addPath = addPath;
/**
 * Gets the value of an input.  The value is also trimmed.
 *
 * @param     name     name of the input to get
 * @param     options  optional. See InputOptions.
 * @returns   string
 */
function getInput(name, options) {
    const val = process.env[`INPUT_${name.replace(/ /g, '_').toUpperCase()}`] || '';
    if (options && options.required && !val) {
        throw new Error(`Input required and not supplied: ${name}`);
    }
    return val.trim();
}
exports.getInput = getInput;
/**
 * Sets the value of an output.
 *
 * @param     name     name of the output to set
 * @param     value    value to store. Non-string values will be converted to a string via JSON.stringify
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function setOutput(name, value) {
    process.stdout.write(os.EOL);
    command_1.issueCommand('set-output', { name }, value);
}
exports.setOutput = setOutput;
/**
 * Enables or disables the echoing of commands into stdout for the rest of the step.
 * Echoing is disabled by default if ACTIONS_STEP_DEBUG is not set.
 *
 */
function setCommandEcho(enabled) {
    command_1.issue('echo', enabled ? 'on' : 'off');
}
exports.setCommandEcho = setCommandEcho;
//-----------------------------------------------------------------------
// Results
//-----------------------------------------------------------------------
/**
 * Sets the action status to failed.
 * When the action exits it will be with an exit code of 1
 * @param message add error issue message
 */
function setFailed(message) {
    process.exitCode = ExitCode.Failure;
    error(message);
}
exports.setFailed = setFailed;
//-----------------------------------------------------------------------
// Logging Commands
//-----------------------------------------------------------------------
/**
 * Gets whether Actions Step Debug is on or not
 */
function isDebug() {
    return process.env['RUNNER_DEBUG'] === '1';
}
exports.isDebug = isDebug;
/**
 * Writes debug message to user log
 * @param message debug message
 */
function debug(message) {
    command_1.issueCommand('debug', {}, message);
}
exports.debug = debug;
/**
 * Adds an error issue
 * @param message error issue message. Errors will be converted to string via toString()
 */
function error(message) {
    command_1.issue('error', message instanceof Error ? message.toString() : message);
}
exports.error = error;
/**
 * Adds an warning issue
 * @param message warning issue message. Errors will be converted to string via toString()
 */
function warning(message) {
    command_1.issue('warning', message instanceof Error ? message.toString() : message);
}
exports.warning = warning;
/**
 * Writes info to log with console.log.
 * @param message info message
 */
function info(message) {
    process.stdout.write(message + os.EOL);
}
exports.info = info;
/**
 * Begin an output group.
 *
 * Output until the next `groupEnd` will be foldable in this group
 *
 * @param name The name of the output group
 */
function startGroup(name) {
    command_1.issue('group', name);
}
exports.startGroup = startGroup;
/**
 * End an output group.
 */
function endGroup() {
    command_1.issue('endgroup');
}
exports.endGroup = endGroup;
/**
 * Wrap an asynchronous function call in a group.
 *
 * Returns the same type as the function itself.
 *
 * @param name The name of the group
 * @param fn The function to wrap in the group
 */
function group(name, fn) {
    return __awaiter(this, void 0, void 0, function* () {
        startGroup(name);
        let result;
        try {
            result = yield fn();
        }
        finally {
            endGroup();
        }
        return result;
    });
}
exports.group = group;
//-----------------------------------------------------------------------
// Wrapper action state
//-----------------------------------------------------------------------
/**
 * Saves state for current action, the state can only be retrieved by this action's post job execution.
 *
 * @param     name     name of the state to store
 * @param     value    value to store. Non-string values will be converted to a string via JSON.stringify
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function saveState(name, value) {
    command_1.issueCommand('save-state', { name }, value);
}
exports.saveState = saveState;
/**
 * Gets the value of an state set by this action's main execution.
 *
 * @param     name     name of the state to get
 * @returns   string
 */
function getState(name) {
    return process.env[`STATE_${name}`] || '';
}
exports.getState = getState;
//# sourceMappingURL=core.js.map

/***/ }),

/***/ 108:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


// For internal use, subject to change.
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (Object.hasOwnProperty.call(mod, k)) result[k] = mod[k];
    result["default"] = mod;
    return result;
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
// We use any as a valid input type
/* eslint-disable @typescript-eslint/no-explicit-any */
const fs = __importStar(__webpack_require__(747));
const os = __importStar(__webpack_require__(87));
const utils_1 = __webpack_require__(570);
function issueCommand(command, message) {
    const filePath = process.env[`GITHUB_${command}`];
    if (!filePath) {
        throw new Error(`Unable to find environment variable for file command ${command}`);
    }
    if (!fs.existsSync(filePath)) {
        throw new Error(`Missing file at path: ${filePath}`);
    }
    fs.appendFileSync(filePath, `${utils_1.toCommandValue(message)}${os.EOL}`, {
        encoding: 'utf8'
    });
}
exports.issueCommand = issueCommand;
//# sourceMappingURL=file-command.js.map

/***/ }),

/***/ 570:
/***/ ((__unused_webpack_module, exports) => {


// We use any as a valid input type
/* eslint-disable @typescript-eslint/no-explicit-any */
Object.defineProperty(exports, "__esModule", ({ value: true }));
/**
 * Sanitizes an input into a string so it can be passed into issueCommand safely
 * @param input input to sanitize into a string
 */
function toCommandValue(input) {
    if (input === null || input === undefined) {
        return '';
    }
    else if (typeof input === 'string' || input instanceof String) {
        return input;
    }
    return JSON.stringify(input);
}
exports.toCommandValue = toCommandValue;
//# sourceMappingURL=utils.js.map

/***/ }),

/***/ 747:
/***/ ((module) => {

module.exports = require("fs");;

/***/ }),

/***/ 87:
/***/ ((module) => {

module.exports = require("os");;

/***/ }),

/***/ 622:
/***/ ((module) => {

module.exports = require("path");;

/***/ })

/******/ 	});
/************************************************************************/
/******/ 	// The module cache
/******/ 	var __webpack_module_cache__ = {};
/******/ 	
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/ 		// Check if module is in cache
/******/ 		var cachedModule = __webpack_module_cache__[moduleId];
/******/ 		if (cachedModule !== undefined) {
/******/ 			return cachedModule.exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = __webpack_module_cache__[moduleId] = {
/******/ 			// no module.id needed
/******/ 			// no module.loaded needed
/******/ 			exports: {}
/******/ 		};
/******/ 	
/******/ 		// Execute the module function
/******/ 		__webpack_modules__[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/ 	
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/ 	
/************************************************************************/
var __webpack_exports__ = {};
// This entry need to be wrapped in an IIFE because it need to be isolated against other modules in the chunk.
(() => {

// EXTERNAL MODULE: ./node_modules/@actions/core/lib/core.js
var core = __webpack_require__(225);
// EXTERNAL MODULE: external "path"
var external_path_ = __webpack_require__(622);
// EXTERNAL MODULE: external "fs"
var external_fs_ = __webpack_require__(747);
;// CONCATENATED MODULE: external "child_process"
const external_child_process_namespaceObject = require("child_process");;
;// CONCATENATED MODULE: ./cvw/common.ts

var BuildTarget;
(function (BuildTarget) {
    BuildTarget["SNAPSHOT"] = "SNAPSHOT";
    BuildTarget["MILESTONE"] = "MILESTONE";
    BuildTarget["RELEASE"] = "RELEASE";
    BuildTarget["PRE_RELEASE"] = "PRE_RELEASE";
    BuildTarget["LOCAL"] = "LOCAL";
})(BuildTarget || (BuildTarget = {}));
const utf8 = "utf-8";
const tmp = "/tmp";
const jdkRoot = "/tmp/jdk";
const GS_GH_EVENT = "GS_GH_EVENT";
const GS_CONFIG_URL = "GS_CONFIG_URL";
const INPUT_ORGCONFIG = "INPUT_ORGCONFIG";
const runCmd = (cmd, args, env = undefined) => {
    console.log(`Running: ${cmd} ${args}`);
    return new Promise((resolve, reject) => {
        const stdoutLines = [];
        const stdErrlines = [];
        const proc = (0,external_child_process_namespaceObject.spawn)(cmd, args, env ? { env } : undefined);
        proc.stdout.on("data", buf => {
            process.stdout.write(buf.toString());
            stdoutLines.push(buf.toString());
        });
        proc.stderr.on("data", buf => {
            process.stderr.write(buf.toString());
            stdErrlines.push(buf.toString());
        });
        proc.on("exit", code => {
            console.log(`process exit code: [${code}]`);
            const out = { stdoutLines, stdErrlines, code };
            return code != 0 ? reject(out) : resolve(out);
        });
    });
};

;// CONCATENATED MODULE: ./cvw/gradle.ts



const gradle = "gradle";
const loadOrgConfig = (srcUrl) => {
    const configPath = (0,external_path_.resolve)(process.env.RUNNER_WORKSPACE, "org-config.json");
    return runCmd("wget", ["--quiet", srcUrl, "--output-document", configPath])
        .then(() => JSON.parse((0,external_fs_.readFileSync)(configPath, utf8)));
};
const loadJdk = (devConfig) => {
    const cwd = process.cwd();
    const localPath = (0,external_path_.resolve)(tmp, "jdk");
    const archivePath = (0,external_path_.resolve)(localPath, "jdk.tar.gz");
    return external_fs_.promises.mkdir(localPath, { recursive: true })
        .then(() => process.chdir(localPath))
        .then(() => runCmd("wget", ["--quiet", devConfig.jdkDistribution, "--output-document", archivePath]))
        .then(() => runCmd("tar", ["-xf", archivePath, "--strip-components=1"]))
        .then(() => process.chdir(cwd));
};
const loadGradle = (devConfig) => {
    const cwd = process.cwd();
    const localPath = (0,external_path_.resolve)(tmp, `gradle-${devConfig.gradleVersion}`);
    return external_fs_.promises.mkdir(localPath, { recursive: true })
        .then(() => process.chdir(tmp))
        .then(() => runCmd("wget", ["--quiet", devConfig.gradleDistribution]))
        .then(() => runCmd("unzip", ["-q", "gradle-*.zip"]))
        .then(() => process.chdir(cwd))
        .then(() => localPath);
};
const gradleBuild = (jdkRoot, gradleRoot, projectRoot, commit, orgConfigUrL) => {
    const gradleCmd = (0,external_path_.resolve)(gradleRoot, "bin", gradle);
    const buildArgs = ["build", "-b", (0,external_path_.resolve)(projectRoot, "build.gradle.kts")];
    const gradleEnv = Object.assign(Object.assign({}, process.env), { JAVA_HOME: jdkRoot, [GS_GH_EVENT]: JSON.stringify(commit), [GS_CONFIG_URL]: orgConfigUrL });
    return runCmd(gradleCmd, buildArgs, gradleEnv);
};

;// CONCATENATED MODULE: ./cvw/index.ts





const cvw_event = JSON.parse(external_fs_.readFileSync(process.env.GITHUB_EVENT_PATH, utf8));
const errorHandler = (e) => {
    const eJson = JSON.stringify(e, null, 2);
    if (Object.keys(e).length == 0) {
        (0,core.error)(e.message);
    }
    else {
        (0,core.error)(eJson);
    }
    (0,core.setFailed)(e);
};
const buildInit = (commit, buildTarget) => {
    const orgConfigUrl = process.env[INPUT_ORGCONFIG];
    return loadOrgConfig(orgConfigUrl).then(orgConfig => {
        commit.buildTarget = buildTarget;
        return loadJdk(orgConfig.devConfig)
            .then(() => loadGradle(orgConfig.devConfig))
            .then(gradleRoot => gradleBuild(jdkRoot, gradleRoot, (0,external_path_.normalize)(process.cwd()), commit, orgConfigUrl));
    });
};
const onCommit = (commit) => {
    const { ref } = commit;
    (0,core.info)("*******************************************************************");
    (0,core.info)(`* Target ref: ${ref}`);
    (0,core.info)("*******************************************************************");
    if (ref && ref.includes("feature/")) {
        return buildInit(commit, BuildTarget.SNAPSHOT);
    }
    else if (ref && ref.includes("develop")) {
        return buildInit(commit, BuildTarget.MILESTONE);
    }
    else if (ref.includes("refs/tags")) {
        return buildInit(commit, BuildTarget.RELEASE);
    }
    else if (ref && (ref.includes("master") || ref.includes("main"))) {
        return buildInit(commit, BuildTarget.PRE_RELEASE);
    }
    (0,core.warning)(`Building non-managed ref combination: ${ref}`);
    return buildInit(commit, BuildTarget.LOCAL);
};
if (cvw_event.ref) {
    onCommit(cvw_event).catch(errorHandler);
}

})();

/******/ })()
;
//# sourceMappingURL=main.js.map