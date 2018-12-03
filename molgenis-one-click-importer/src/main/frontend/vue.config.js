// Specifies the config location of the postcss config to loader for workspace Yarn usage
module.exports = {
  css: {
    loaderOptions: {
      postcss: {
        path: __dirname
      }
    }
  },
  devServer: {
    proxy: {
      "/api": {
        target: "http://localhost:8081"
      },
      "/plugin/one-click-importer/upload": {
        target: "http://localhost:8081"
      }
    }
  }
}
