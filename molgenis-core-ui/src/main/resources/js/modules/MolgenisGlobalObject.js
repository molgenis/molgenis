define(function(require, exports, module) {
	/**
	 * @module MolgenisGlobalObject
	 */

	'use strict';
	var ui = require('component/global-ui/global-ui');
	var alert = require('modules/MolgenisAlert');
	var i18n

	/*
	 * Old style molgenis object to put in the global scope for backwards
	 * compatibility
	 */
	var molgenis = {
		'ui' : ui,
		'createAlert' : alert.createAlert,
		'i18n' : i18n
	};

	module.exports = molgenis;
});