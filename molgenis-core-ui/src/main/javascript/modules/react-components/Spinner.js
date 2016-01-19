/**
 * @module Spinner
 */

import React from 'react';
import _ from 'underscore';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';

var div = React.DOM.div, img = React.DOM.img;

/**
 * @memberOf Spinner
 */
var Spinner = React.createClass({
	mixins : [ DeepPureRenderMixin ],
	displayName : 'Spinner',
	render : function() {
		return (div(null, img({
			src : '/css/select2-spinner.gif',
			alt : 'Spinner',
			width : 16,
			height : 16
		})));
	}
});

export default React.createFactory(Spinner)
