/**
 * @memberOf AttrUtilsMixin
 */
'use strict';

import _ from 'underscore';
import molgenis from '../../MolgenisQuery';

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

export default  AttrUtilsMixin;
