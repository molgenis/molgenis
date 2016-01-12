define(function(require, exports, module) {
	/**
	 * @module FormButtons
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');
	var api = require('../RestClientV1');

	var Button = require('./Button');

	var DeepPureRenderMixin = require('./mixin/DeepPureRenderMixin');

	var div = React.DOM.div;

	/**
	 * @memberOf FormButtons
	 */
	var FormButtons = React.createClass({
		mixins : [ DeepPureRenderMixin ],
		displayName : 'FormButtons',
		propTypes : {
			mode : React.PropTypes.oneOf([ 'create', 'edit' ]).isRequired,
			formLayout : React.PropTypes.oneOf([ 'horizontal', 'vertical' ]).isRequired,
			colOffset : React.PropTypes.number,
			cancelBtn : React.PropTypes.bool,
			onCancelClick : React.PropTypes.func,
			onSubmitClick : React.PropTypes.func.isRequired
		},
		getDefaultProps : function() {
			return {
				onCancelClick : function() {
				}
			};
		},
		render : function() {
			var divClasses;
			if (this.props.formLayout === 'horizontal') {
				divClasses = 'col-md-offset-' + this.props.colOffset + ' col-md-' + (12 - this.props.colOffset);
			} else {
				divClasses = 'col-md-12';
			}

			var submitBtnText = this.props.mode === 'create' ? 'Create' : 'Save changes';
			var submitBtnName = this.props.mode === 'create' ? 'create' : 'save-changes';
			return (div({
				className : 'row',
				style : {
					textAlign : 'right'
				}
			}, div({
				className : divClasses
			}, this.props.cancelBtn ? Button({
				name : 'cancel',
				text : 'Cancel',
				onClick : this.props.onCancelClick
			}, 'Cancel') : null, Button({
				type : 'button',
				style : 'primary',
				css : {
					marginLeft : 5
				},
				name : submitBtnName,
				text : submitBtnText,
				onClick : this.props.onSubmitClick
			}))));
		}
	});

	module.exports = React.createFactory(FormButtons);
});