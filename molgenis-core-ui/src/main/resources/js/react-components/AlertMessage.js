define(function(require, exports, module) {
	/**
	 * @module AlertMessage
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var div = React.DOM.div, span = React.DOM.span, button = React.DOM.button;

	var DeepPureRenderMixin = require('./mixin/DeepPureRenderMixin');

	/**
	 * @memberOf AlertMessage
	 */
	var AlertMessage = React.createClass({
		mixins : [ DeepPureRenderMixin ],
		displayName : 'AlertMessage',
		propTypes : {
			type : React.PropTypes.oneOf([ 'success', 'info', 'warning', 'danger' ]),
			message : React.PropTypes.string.isRequired,
			onDismiss : React.PropTypes.func,
		},
		render : function() {
			return (div({
				className : 'alert alert-' + this.props.type + ' alert-dismissible',
				role : 'alert'
			}, this.props.onDismiss ? button({
				type : 'button',
				className : 'close',
				'aria-label' : 'Close',
				onClick : this.props.onDismiss
			}, // TODO use the Button module
			span({
				'aria-hidden' : true,
			}, String.fromCharCode(215)) // &times;
			) : null, this.props.type === 'danger' ? molgenis.ui.Icon({
				name : 'exclamation-sign'
			}) : null, this.props.type === 'danger' ? ' ' + this.props.message : this.props.message));
		}
	});

	module.exports = React.createFactory(AlertMessage)
});