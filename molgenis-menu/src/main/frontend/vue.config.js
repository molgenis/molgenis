// vue.config.js
module.exports = {
  filenameHashing: false,
  configureWebpack: config => {
    config.externals = {
      'jquery': 'jQuery',
      'bootstrap': 'bootstrap',
      'popper.js': 'popper.js'
    }
  }
}
