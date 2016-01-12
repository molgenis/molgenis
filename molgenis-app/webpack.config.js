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
	}), new webpack.optimize.OccurenceOrderPlugin(), new webpack.NoErrorsPlugin() ],

	// test is not a test! its a regex for the file name
	// loader is a preprocesser that understands the files
	module : {
		loaders : [ {
			test : /\.jsx?$/,
			// babel does a lot of little transforms in the right order, we
			// select these two presets that
			// make it compile jsx -> js and es6 -> js
			loaders : [ 'babel?presets[]=es2015,presets[]=react' ],
			exclude : /node_modules/,
			include : __dirname
		}, {
			test : /\.css$/,
			loader : 'style-loader!css-loader'
		}, {
			test : /\.eot(\?v=\d+\.\d+\.\d+)?$/,
			loader : "file"
		}, {
			test : /\.(woff|woff2)$/,
			loader : "url?prefix=font/&limit=5000"
		}, {
			test : /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
			loader : "url?limit=10000&mimetype=application/octet-stream"
		}, {
			test : /\.svg(\?v=\d+\.\d+\.\d+)?$/,
			loader : "url?limit=10000&mimetype=image/svg+xml"
		} ]
	}
}

module.exports = configuration;