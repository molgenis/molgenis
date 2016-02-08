var path = require('path');
var webpack = require('webpack');

var configuration = {
    resolve: {
        root: [path.resolve('../molgenis-core-ui/src/main/javascript'),
            path.resolve('../molgenis-core-ui/src/test/javascript'),
            path.resolve('./node_modules')],
        alias: {
            'react-components': 'modules/react-components'
        }
    },
    context: path.join(__dirname, '../'),
    entry: {
        'test': './molgenis-core-ui/src/test/javascript/testsuite.js'
    },
    output: {
        path: './target/test-classes/js/dist/',
        filename: '[name].bundle.js'
    },
    target: 'node',
    plugins: [
        new webpack.ProvidePlugin({$: "jquery", jQuery: "jquery"}),
        new webpack.optimize.OccurenceOrderPlugin(),
        new webpack.NoErrorsPlugin(),
        new webpack.IgnorePlugin(/react-addons|react-dom/)
    ],
    resolveLoader: {
        root: [path.resolve('./node_modules')]
    },
    module: {
        loaders: [{
            test: /\.jsx?$/,
            loader: 'babel',
            exclude: /node_modules/,
        }, {
            test: /\.css$/,
            loader: 'null-loader'
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
        }]
    }
};

module.exports = configuration;