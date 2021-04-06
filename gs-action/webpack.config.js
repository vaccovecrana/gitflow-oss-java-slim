const path = require("path")
const CopyPlugin = require("copy-webpack-plugin")

const nodeModules = path.resolve(__dirname, "node_modules")
const buildPath = path.resolve(__dirname, "dist")

console.log(`====> Base build directory: [${buildPath}]`)

const wpc = {
  target: "node",
  entry: {main: "cvw/index.ts"},
  devtool: "source-map",
  output: {filename: "[name].js", path: buildPath},
  module: {
    rules: [{test: /\.ts/, use: ["ts-loader"], exclude: /node_modules/}]
  },
  resolve: {
    extensions: [".ts", ".js"],
    modules: [nodeModules, path.resolve(__dirname)],
  },
  optimization: {minimize: false}
}

module.exports = wpc
