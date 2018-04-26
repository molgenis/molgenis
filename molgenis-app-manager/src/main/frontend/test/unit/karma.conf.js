var webpackConfig = require('../../build/webpack.test.conf')

module.exports = function (config) {
  config.set({
    browsers: ['PhantomJS'], //ChromeCanary
    frameworks: ['mocha', 'sinon-chai'],
    reporters: ['spec', 'coverage'],
    files: [
      '../../node_modules/babel-polyfill/dist/polyfill.js',
      '../../node_modules/es6-promise/dist/es6-promise.auto.js',
      './index.js'
    ],
    preprocessors: {
      './index.js': ['webpack', 'sourcemap']
    },
    webpack: webpackConfig,
    webpackMiddleware: {
      noInfo: true
    },
    coverageReporter: {
      dir: '../../../../../target/generated-sources/coverage',
      reporters: [
        {type: 'cobertura', subdir: 'cobertura'}
      ]
    },
    junitReporter: {
      outputDir: '../../../../../target/surefire-reports',
      outputFile: 'TEST-results.xml',
      useBrowserName: false
    }
  })
}
