// This file runs in node so should use commonsjs require syntax
var path = require('path')
var webpack = require('webpack')

var configuration = {
	// Resolve location of node modules and molgenis modules specific javascript
	resolve : {
		root : [ path.resolve('../molgenis-core-ui/src/main/javascript'), path.resolve('./node_modules') ]
	},
	// The base directory for resolving the entry option
	context : path.join(__dirname, '../'),
	// Set an entry point for every module in the molgenis application
	entry : {
		'molgenis-core-ui' : './molgenis-core-ui/src/main/javascript/core-ui-webpack.js',
	},
	// Quick build for reloading or something...
	output : {
		path : './target/classes/js/dist/',
		filename : '[name].bundle.js',
		publicPath : '/js/dist/'
	},
	devtool : 'cheap-module-eval-source-map',
	plugins : [ new webpack.ProvidePlugin({
		$ : "jquery",
		jQuery : "jquery"
	}), new webpack.optimize.OccurenceOrderPlugin(), new webpack.NoErrorsPlugin() ],

	resolveLoader : {
		root : [ path.resolve('./node_modules') ]
	},

	// test is not a test! its a regex for the file name
	// loader is a preprocesser that understands the files
	module : {
		loaders : [ {
			test : /\.jsx?$/,
			// babel does a lot of little transforms in the right order, we
			// select these two presets that
			// make it compile jsx -> js and es6 -> js
			loader : 'babel',
			exclude : /node_modules/,
			query : {
				presets : [ require.resolve('babel-preset-es2015'), require.resolve('babel-preset-react') ]
			}
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