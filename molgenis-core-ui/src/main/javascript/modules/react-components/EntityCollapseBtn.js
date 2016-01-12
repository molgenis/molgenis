define(function(require, exports, module) {
	/**
	 * @module EntityCollapseBtn
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var Button = require('./Button');

	var DeepPureRenderMixin = require('./mixin/DeepPureRenderMixin');

	/**
	 * @memberOf EntityCollapseBtn
	 */
	var EntityCollapseBtn = React.createClass({
		mixins : [ DeepPureRenderMixin ],
		displayName : 'EntityCollapseBtn',
		propTypes : {
			attrPath : React.PropTypes.array.isRequired,
			onCollapse : React.PropTypes.func,
		},
		getDefaultProps : function() {
			return {
				onCollapse : function() {
				}
			};
		},
		render : function() {
			return Button({
				icon : 'collapse-up',
				size : 'xsmall',
				title : 'Collapse entity',
				onClick : this._handleCollapse
			});
		},
		_handleCollapse : function() {
			this.props.onCollapse({
				attrPath : this.props.attrPath
			});
		}
	});

	module.exports = React.createFactory(EntityCollapseBtn);
});