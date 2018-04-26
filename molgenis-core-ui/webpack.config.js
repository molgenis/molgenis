// This file runs in node so should use commonsjs require syntax
var path = require('path');
var webpack = require('webpack');

var configuration = {
	// Resolve location of node modules and molgenis modules specific javascript
	resolve : {
		root : [ path.resolve('./src/main/javascript'), path.resolve('./node_modules') ],
		alias : {
			'react-components' : 'modules/react-components',
			'rest-client' : 'modules/rest-client',
			'i18n' : 'modules/i18n',
			'jquery-ui' : 'plugins/jquery-ui-1.9.2.custom.min',
			'jq-edit-rangeslider' : 'plugins/jQEditRangeSlider-min',
			'select2' : 'plugins/select2-patched',
			'utils' : 'modules/utils'
		}
	},
	// The base directory for resolving the entry option
	context : __dirname,
	devtool: 'eval',
	entry : {
		'global-ui' : [ './src/main/javascript/molgenis-global-ui-webpack' ],
		'global' : [ './src/main/javascript/molgenis-global-webpack' ],
		'vendor-bundle' : [ './src/main/javascript/molgenis-vendor-webpack' ]
	},
	output : {
		path : './target/classes/js/dist/',
		filename : 'molgenis-[name].js',
		publicPath : '/js/dist/'
	},
	plugins : [ new webpack.PrefetchPlugin('react/lib/React'), new webpack.PrefetchPlugin('react/lib/ReactWithAddons'),
			new webpack.PrefetchPlugin('react/lib/DOMChildrenOperations.js'), new webpack.PrefetchPlugin('react/lib/ReactDOMComponent'),
			new webpack.PrefetchPlugin('react/lib/ReactReconcileTransaction'), new webpack.PrefetchPlugin('react-components'),
			new webpack.PrefetchPlugin('./src/main/javascript/modules/react-components/wrapper/JQRangeSlider.js'), new webpack.ProvidePlugin({
				$ : "jquery",
				jQuery : "jquery"
			}), new webpack.optimize.OccurenceOrderPlugin(), new webpack.optimize.CommonsChunkPlugin([ 'vendor-bundle' ], 'molgenis-[name].js') ],
	resolveLoader : {
		root : [ path.resolve('./node_modules') ]
	},
	// The test property is a test regex to determine if the loader is relevant
	// for the file name.
	// loader is a preprocesser that understands the files that match the test
	module : {
		loaders : [ {
			test : /\.jsx?$/,
			// babel does a lot of little transforms in the right order, we
			// select these two presets that make it compile jsx -> js and es6
			// -> js
			loader : 'babel',
			exclude : [ /node_modules/, /src[/\\]main[/\\]javascript[/\\]plugins/]
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
		}, {
			test : /\.png$/,
			loader : "url?limit=10000&mimetype=image/png"
		}, {
			test : /\.gif$/,
			loader : "url?limit=10000&mimetype=image/gif"
		} ]
	},
}

module.exports = configuration;
