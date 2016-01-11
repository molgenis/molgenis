define(function(require, exports, module) {
	/**
	 * @memberOf AttrUtilsMixin
	 */
	'use strict';

	var _ = require('underscore');
	var molgenis = require('modules/MolgenisQuery');

	var AttrUtilsMixin = {
		_isSelectedAttr : function(attr, selectedAttrs) {
			return selectedAttrs['*'] !== undefined || selectedAttrs[attr.name] !== undefined;
		},
		_isExpandedAttr : function(attr, selectedAttrs) {
			return selectedAttrs[attr.name] !== null && selectedAttrs[attr.name] !== undefined;
		},
		_canExpandAttr : function(attr, path) {
			// expanding mrefs in expanded attr not supported
			return molgenis.isRefAttr(attr) && !(molgenis.isMrefAttr(attr) && _.size(path) > 0);
		}
	};

	module.exports = AttrUtilsMixin;
});