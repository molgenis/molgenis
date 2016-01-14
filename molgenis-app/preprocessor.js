// Can't use ES6 because the preprocesser is not processed by babel
'use strict';

var babel = require('babel-core');

module.exports = {
	process : function(src, filename) {
		// Ignore files other than .js, .es, .jsx or .es6
		if (!babel.util.canCompile(filename)) {
			return '';
		}
		// Ignore all files within node_modules
		if (filename.indexOf('node_modules') === -1) {
			return babel.transform(src, {
				filename : filename
			}).code;
		}
		return src;
	}
};