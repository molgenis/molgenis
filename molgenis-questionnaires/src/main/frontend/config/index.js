'use strict'
// Template version: 1.3.1
// see http://vuejs-templates.github.io/webpack for documentation.

const path = require('path')
const questionnaireList = require('./dev-responses/questionnaire-list.js')
const localizedMessages = require('./dev-responses/localized-messages.js')
const firstQuestionnaireResponse = require('./dev-responses/first-questionnaire-response.js')
const secondQuestionnaireResponse = require('./dev-responses/second-questionnaire-response.js')
const thirdQuestionnaireResponse = require('./dev-responses/third-questionnaire-response.js')

module.exports = {
  dev: {

    // Paths
    assetsSubDirectory: 'static',
    assetsPublicPath: '/',
    proxyTable: {
      '/login': {
        target: 'http://localhost:8080'
      },
      '/api': {
        target: 'http://localhost:8080'
      },
      '/menu/plugins/questionnaires': {
        target: 'http://localhost:8080'
      }
    },

    // Various Dev Server settings
    host: 'localhost', // can be overwritten by process.env.HOST
    port: 8080, // can be overwritten by process.env.PORT, if port is in use, a free one will be determined
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

    // https://webpack.js.org/configuration/devtool/#development
    devtool: 'cheap-module-eval-source-map',

    // If you have problems debugging vue-files in devtools,
    // set this to false - it *may* help
    // https://vue-loader.vuejs.org/en/options.html#cachebusting
    cacheBusting: true,

    cssSourceMap: true,

    /**
     * GET and POST interceptors
     * Removes the need for a running backend during development
     */

    before (app) {
      // app.get('/menu/plugins/questionnaires/list', function (req, res) {
      //   res.json(questionnaireList)
      // })
      //
      // app.get('/api/v2/i18n/questionnaire/en', function (req, res) {
      //   res.json(localizedMessages)
      // })
      //
      // app.get('/menu/plugins/questionnaires/start/questionnaire_1', function (req, res) {
      //   res.json('OK')
      // })
      //
      // app.get('/menu/plugins/questionnaires/start/questionnaire_2', function (req, res) {
      //   res.json('OK')
      // })
      //
      // app.get('/api/v2/questionnaire_1', function (req, res) {
      //   res.json(firstQuestionnaireResponse)
      // })
      //
      // app.get('/api/v2/questionnaire_2', function (req, res) {
      //   res.json(secondQuestionnaireResponse)
      // })
      //
      // app.get('/api/v2/questionnaire_3', function (req, res) {
      //   res.json(thirdQuestionnaireResponse)
      // })
      //
      // app.put('/api/v1/*', function (req, res) {
      //   res.json('OK')
      // })
      //
      // app.post('/api/v2/*', function (req, res) {
      //   res.json('OK')
      // })
      //
      // app.get('/menu/plugins/questionnaires/submission-text/*', function (req, res) {
      //   res.json('<h1>Thank you</h1>')
      // })
    }
  },

  build: {
    // Template for index.html
    index: path.resolve(__dirname, '../../../../target/classes/index.html'),

    // Paths
    assetsRoot: path.resolve(__dirname, '../../../../target/classes'),
    assetsSubDirectory: '',
    assetsPublicPath: '/',

    /**
     * Source Maps
     */

    productionSourceMap: true,
    // https://webpack.js.org/configuration/devtool/#production
    devtool: '#source-map',

    // Gzip off by default as many popular static hosts such as
    // Surge or Netlify already gzip all static assets for you.
    // Before setting to `true`, make sure to:
    // npm install --save-dev compression-webpack-plugin
    productionGzip: false,
    productionGzipExtensions: ['js', 'css'],

    // Run the build command with an extra argument to
    // View the bundle analyzer report after build finishes:
    // `npm run build --report`
    // Set to `true` or `false` to always turn it on or off
    bundleAnalyzerReport: process.env.npm_config_report
  }
}
