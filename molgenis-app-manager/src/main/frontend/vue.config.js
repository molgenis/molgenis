const ExtractTextPlugin = require('extract-text-webpack-plugin')

module.exports = {
  compiler: true,
  devServer: {
    proxy: 'http://localhost:8080'
  },
  configureWebpack: config => {
    if (process.env.NODE_ENV === 'production') {
      config.output.path = __dirname + '/../../../target/classes'
      config.output.filename = 'js/app-manager/[name].js'
      config.plugins.push(
        new ExtractTextPlugin({
          filename: 'css/app-manager/[name].css'
        })
      )
    }
  }
}