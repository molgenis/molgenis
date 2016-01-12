define(function(require, exports, module) {
	/**
	 * @module EntityExpandBtn
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var Button = require('./Button');

	var DeepPureRenderMixin = require('./mixin/DeepPureRenderMixin');

	/**
	 * @memberOf EntityExpandBtn
	 */
	var EntityExpandBtn = React.createClass({
		mixins : [ DeepPureRenderMixin ],
		displayName : 'EntityExpandBtn',
		propTypes : {
			attrPath : React.PropTypes.array.isRequired,
			onExpand : React.PropTypes.func,
		},
		getDefaultProps : function() {
			return {
				onExpand : function() {
				}
			};
		},
		render : function() {
			return Button({
				icon : 'expand',
				size : 'xsmall',
				title : 'Expand entity',
				onClick : this._handleExpand
			});
		},
		_handleExpand : function() {
			this.props.onExpand({
				attrPath : this.props.attrPath
			});
		}
	});

	module.exports = React.createFactory(EntityExpandBtn);
});