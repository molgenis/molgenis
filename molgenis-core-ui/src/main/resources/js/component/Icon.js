define(function(require, exports, module) {
	/**
	 * @module Icon
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var DeepPureRenderMixin = require('component/mixin/DeepPureRenderMixin');

	var span = React.DOM.span;

	/**
	 * @memberOf Icon
	 */
	var Icon = React.createClass({
		mixins : [ DeepPureRenderMixin ],
		displayName : 'Icon',
		propTypes : {
			name : React.PropTypes.string.isRequired,
			onClick : React.PropTypes.func,
			style : React.PropTypes.object
		},
		render : function() {
			var style = this.props.onClick ? _.extend({
				cursor : 'pointer'
			}, this.props.style) : this.props.style;
			return (span({
				onClick : this.props.onClick,
				style : style
			}, span({
				className : 'glyphicon glyphicon-' + this.props.name,
				'aria-hidden' : true
			}), span({
				className : 'sr-only'
			}, this.props.name)));
		}
	});

	module.exports = React.createFactory(Icon)
});
