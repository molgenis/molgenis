define(function(require, exports, module) {
	/**
	 * @module JQueryForm
	 */
	"use strict";

	var _ = require('underscore');
	var $ = require('jquery');
	var React = require('react');

	var DeepPureRenderMixin = require('../mixin/DeepPureRenderMixin');

	var form = React.DOM.form;

	/**
	 * React component for jQuery Form Plugin (http://malsup.com/jquery/form/)
	 * 
	 * @memberOf component.wrapper
	 */
	var JQueryForm = React.createClass({
		displayName : 'JQueryForm',
		mixins : [ DeepPureRenderMixin ],
		propTypes : {
			className : React.PropTypes.string,
			action : React.PropTypes.string.isRequired,
			method : React.PropTypes.string,
			noValidate : React.PropTypes.bool,
			beforeSubmit : React.PropTypes.func,
			success : React.PropTypes.func,
			error : React.PropTypes.func,
		},
		submit : function() {
			var $form = $(this.refs.form.getDOMNode());
			$form.ajaxSubmit({
				resetForm : false,
				beforeSubmit : this.props.beforeSubmit,
				success : this.props.success,
				error : this.props.error
			});
		},
		componentDidMount : function() {
			this.refs.form.getDOMNode().submit = this.submit;
		},
		componentWillUnmount : function() {
			var $form = $(this.refs.form.getDOMNode());
			$form.ajaxFormUnbind();
		},
		render : function() {
			return (form(_.extend({}, this.props, {
				ref : 'form'
			}), this.props.children));
		}
	});

	module.exports = React.createFactory(JQueryForm);
});