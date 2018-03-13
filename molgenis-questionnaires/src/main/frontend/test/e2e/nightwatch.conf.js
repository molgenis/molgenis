require('babel-register')
var config = require('../../config')
var packageJson = require('../../package.json');

var defaultPauzeBeforeTestStart = 3000

// http://nightwatchjs.org/gettingstarted#settings-file
module.exports = {
  src_folders: ['test/e2e/specs'],
  output_folder: 'test/e2e/reports',
  custom_assertions_path: ['test/e2e/custom-assertions'],

  selenium: {
    start_process: true,
    server_path: require('selenium-server').path,
    host: '127.0.0.1',
    port: 4444,
    cli_args: {
      'webdriver.chrome.driver': require('chromedriver').path,
      'webdriver.firefox.driver': require('geckodriver').path
    }
  },

  test_settings: {
    default: {
      selenium_port: 4444,
      selenium_host: 'localhost',
      silent: true,
      globals: {
        devServerURL: 'http://localhost:' + (process.env.PORT || config.dev.port),
        waitForConditionTimeout: 10000,
        waitBeforeTestStart: 1000
      }
    },

    ci_chrome: {
      launch_url: "http://ondemand.saucelabs.com:80",
      selenium_port: 80,
      selenium_host: 'ondemand.saucelabs.com',
      silent: true,
      username: process.env.SAUCE_USERNAME,
      access_key: process.env.SAUCE_ACCESS_KEY,
      desiredCapabilities: {
        name: packageJson.name,
        build: 'build-${TRAVIS_JOB_NUMBER}',
        'tunnel-identifier': process.env.TRAVIS_JOB_NUMBER,
        browserName: 'chrome'
      },
      globals: {
        waitForConditionTimeout: 10000,
        waitBeforeTestStart: defaultPauzeBeforeTestStart
      }
    },

    ci_firefox: {
      launch_url: "http://ondemand.saucelabs.com:80",
      selenium_port: 80,
      selenium_host: 'ondemand.saucelabs.com',
      silent: true,
      username: process.env.SAUCE_USERNAME,
      access_key: process.env.SAUCE_ACCESS_KEY,
      desiredCapabilities: {
        name: packageJson.name,
        build: 'build-${TRAVIS_JOB_NUMBER}',
        'tunnel-identifier': process.env.TRAVIS_JOB_NUMBER,
        browserName: 'firefox'
      },
      globals: {
        waitForConditionTimeout: 10000,
        waitBeforeTestStart: defaultPauzeBeforeTestStart
      }
    },

    ci_ie11: {
      launch_url: "http://ondemand.saucelabs.com:80",
      selenium_port: 80,
      selenium_host: 'ondemand.saucelabs.com',
      silent: true,
      username: process.env.SAUCE_USERNAME,
      access_key: process.env.SAUCE_ACCESS_KEY,
      desiredCapabilities: {
        name: packageJson.name,
        build: 'build-${TRAVIS_JOB_NUMBER}',
        'tunnel-identifier': process.env.TRAVIS_JOB_NUMBER,
        browserName: 'internet explorer',
        platform: 'Windows 10',
        version: '11.103',
      },
      globals: {
        waitForConditionTimeout: 10000,
        waitBeforeTestStart: defaultPauzeBeforeTestStart
      }
    },

    ci_safari: {
      launch_url: "http://ondemand.saucelabs.com:80",
      selenium_port: 80,
      selenium_host: 'ondemand.saucelabs.com',
      silent: true,
      username: process.env.SAUCE_USERNAME,
      access_key: process.env.SAUCE_ACCESS_KEY,
      desiredCapabilities: {
        name: packageJson.name,
        build: 'build-${TRAVIS_JOB_NUMBER}',
        'tunnel-identifier': process.env.TRAVIS_JOB_NUMBER,
        browserName: 'safari'
      },
      globals: {
        waitForConditionTimeout: 10000,
        waitBeforeTestStart: 5000
      }
    },

    chrome: {
      desiredCapabilities: {
        browserName: 'chrome',
        javascriptEnabled: true,
        acceptSslCerts: true
      }
    },

    firefox: {
      desiredCapabilities: {
        browserName: 'firefox',
        javascriptEnabled: true,
        acceptSslCerts: true
      }
    },

    safari: {
      desiredCapabilities: {
        browserName: 'safari',
        javascriptEnabled: true,
        acceptSslCerts: true
      }
    }
  }
}
