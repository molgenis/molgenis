define(function(require, exports, module) {
	/**
	 * @module MolgenisGlobalObject
	 */
	
	'use strict';
	var ui = require('component/global-ui/global-ui');
	
	/*
	 * Old style molgenis object to put in the global scope
	 * for backwards compatibility
	 */
	var molgenis = {
		'ui' : ui
	};

	module.exports = molgenis;
});