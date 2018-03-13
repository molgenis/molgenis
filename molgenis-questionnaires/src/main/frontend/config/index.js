'use strict'

const path = require('path')
const questionnaireList = require('./dev-responses/questionnaire-list.js')
const localizedMessages = require('./dev-responses/localized-messages.js')

module.exports = {
  build: {
    env: require('./prod.env'),
    index: path.resolve(__dirname, '../../../../target/classes/index.html'),
    assetsRoot: path.resolve(__dirname, '../../../../target/classes'),
    assetsSubDirectory: '',
    assetsPublicPath: '/',
    productionSourceMap: true,
    productionGzip: false,
    productionGzipExtensions: ['js', 'css'],
    bundleAnalyzerReport: process.env.npm_config_report
  },
  dev: {
    // Environment
    env: require('./dev.env'),

    // Paths
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    proxyTable: {},

    // Various Dev Server settings
    host: 'localhost', // can be overwritten by process.env.HOST
    port: 3000,
    autoOpenBrowser: false,
    errorOverlay: true,
    notifyOnErrors: true,
    poll: false, // https://webpack.js.org/configuration/dev-server/#devserver-watchoptions-

    // Use Eslint Loader?
    // If true, your code will be linted during bundling and
    // linting errors and warnings will be shown in the console.
    useEslint: true,
    // If true, eslint errors and warnings will also be shown in the error overlay
    // in the browser.
    showEslintErrorsInOverlay: false,

    /**
     * Source Maps
     */

    // If you have problems debugging vue-files in devtools,
    // set this to false - it *may* help
    // https://vue-loader.vuejs.org/en/options.html#cachebusting
    cacheBusting: true,

    // CSS Sourcemaps off by default because relative paths are "buggy"
    // with this option, according to the CSS-Loader README
    // (https://github.com/webpack/css-loader#sourcemaps)
    // In our experience, they generally work as expected,
    // just be aware of this issue when enabling this option.
    cssSourceMap: false,

    /**
     * GET and POST interceptors
     * Removes the need for a running backend during development
     */

    before (app) {
      app.get('/menu/plugins/questionnaires/list', function (req, res) {
        res.json(questionnaireList)
      })

      app.get('/api/v2/i18n/form/en', function (req, res) {
        res.json(localizedMessages)
      })
    }
  }
}
