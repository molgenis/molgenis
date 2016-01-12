define(function(require, exports, module) {
	/**
	 * @module Spinner
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var DeepPureRenderMixin = require('./mixin/DeepPureRenderMixin');

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

	module.exports = React.createFactory(Spinner)
});
