// This file runs in node so should use commonsjs require syntax
var path = require('path')
var webpack = require('webpack')

var configuration = {
	context : __dirname,
	entry : path.join(__dirname, '/src/main/resources/js/main.js'),
	devtool : 'cheap-module-eval-source-map',
	output : {
		path : '/Users/mdehaan/git/molgenis/molgenis-core-ui/target/classes/js/dist/',
		filename : 'molgenis-bundle.js',
		publicPath : '/js/dist/'
	},
	plugins : [ new webpack.ProvidePlugin({
		$ : "jquery",
		jQuery : "jquery"
	}), new webpack.optimize.OccurenceOrderPlugin() ]
}

module.exports = configuration;