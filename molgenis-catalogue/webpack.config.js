var path = require('path');
var webpack = require('../molgenis-core-ui/node_modules/webpack');

var config = {
	resolve : {
		root : [ path.resolve('../molgenis-core-ui/src/main/javascript'), path.resolve('../molgenis-core-ui/node_modules') ],
		alias : {
			'react-components' : 'modules/react-components',
			'rest-client' : 'modules/rest-client',
			'select2' : 'plugins/select2-patched'
		}
	},
	context : __dirname,
	entry : {
		'catalogue' : [ './src/main/javascript/catalogue-webpack' ]
	},
	output : {
		path : './target/classes/js/dist/',
		filename : 'molgenis-[name].js',
		publicPath : '/js/dist/'
	},
	plugins : [ new webpack.ProvidePlugin({
		$ : "jquery",
		jQuery : "jquery"
	}), new webpack.optimize.OccurenceOrderPlugin(), new webpack.NoErrorsPlugin(), new webpack.optimize.CommonsChunkPlugin([ 'vendor-bundle' ], 'molgenis-[name].js') ],
	resolveLoader: {
        root: [path.resolve('../molgenis-core-ui/node_modules')]
    },
    module: {
        loaders: [{
            test: /\.jsx?$/,
            loader: 'babel',
            exclude: [/node_modules/]
        }, {
            test: /\.css$/,
            loader: 'style-loader!css-loader'
        }, {
            test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
            loader: "file"
        }, {
            test: /\.(woff|woff2)$/,
            loader: "url?prefix=font/&limit=5000"
        }, {
            test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
            loader: "url?limit=10000&mimetype=application/octet-stream"
        }, {
            test: /\.svg(\?v=\d+\.\d+\.\d+)?$/,
            loader: "url?limit=10000&mimetype=image/svg+xml"
        }, {
        	test: /\.png$/,
        	loader: "url?limit=10000&mimetype=image/png"
        },
        {
        	test: /\.gif$/,
        	loader: "url?limit=10000&mimetype=image/gif"
        }]
    }
}

module.exports = config;