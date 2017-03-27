// https://github.com/shelljs/shelljs
require('./check-versions')()

process.env.NODE_ENV = 'production'

var ora = require('ora')
var path = require('path')
var chalk = require('chalk')
var shell = require('shelljs')
var webpack = require('webpack')
var config = require('../config')
var webpackConfig = require('./webpack.prod.conf')

var spinner = ora('building for production...')
spinner.start()

var assetsPath = path.join(config.build.assetsRoot, config.build.assetsSubDirectory)
shell.rm('-rf', assetsPath)
shell.mkdir('-p', assetsPath)
shell.config.silent = true
shell.cp('-R', 'static/*', assetsPath)
shell.config.silent = false

webpack(webpackConfig, function (err, stats) {
  spinner.stop()
  if (err) throw err
  process.stdout.write(stats.toString({
    colors: true,
    modules: false,
    children: false,
    chunks: false,
    chunkModules: false
  }) + '\n\n')

  var FolderZip = require('folder-zip');
  var zip = new FolderZip();
  var options = {
    excludeParentFolder: true
  };
  zip.zipFolder('dist/assets', options, function(){
    zip.writeToFile('dist/app-biobank-plots.zip');
  });

  console.log(chalk.cyan('  Build complete.\n'))
  console.log(chalk.yellow(
    '  Tip: built files are meant to be added to molgenis app store.\n' +
    '  Upload the app-biobank-plots.zip to your app.\n' +
    '  Copy the contents of view-biobank-plots.ftl to the app\'s FreemarkerTemplate.\n'
  ))
})
