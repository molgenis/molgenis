define(function(require, exports, module) {
	/**
	 * @module EntityDeleteBtn
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var api = require('modules/RestClientV2')
	var Button = require('component/Button');
	var Dialog = require('component/Dialog');

	var DeepPureRenderMixin = require('component/mixin/DeepPureRenderMixin');
	var ReactLayeredComponentMixin = require('component/mixin/ReactLayeredComponentMixin');

	/**
	 * @memberOf EntityDeleteBtn
	 */
	var EntityDeleteBtn = React.createClass({
		mixins : [ DeepPureRenderMixin, ReactLayeredComponentMixin ],
		displayName : 'EntityDeleteBtn',
		propTypes : {
			name : React.PropTypes.string.isRequired,
			id : React.PropTypes.oneOfType([ React.PropTypes.string, React.PropTypes.number ]).isRequired,
			onDelete : React.PropTypes.func
		},
		getInitialState : function() {
			return {
				dialog : false
			};
		},
		getDefaultProps : function() {
			return {
				onDelete : function() {
				}
			};
		},
		render : function() {
			return Button({
				icon : 'trash',
				title : 'Delete row',
				style : 'danger',
				size : 'xsmall',
				onClick : this._handleDelete
			});
		},
		renderLayer : function() {
			return this.state.dialog ? Dialog({
				type : 'confirm',
				message : 'Are you sure you want to delete this row?',
				onCancel : this._handleDeleteCancel,
				onConfirm : this._handleDeleteConfirm
			}) : null;
		},
		_handleDelete : function() {
			this.setState({
				dialog : true
			});
		},
		_handleDeleteCancel : function() {
			this.setState({
				dialog : false
			});
		},
		_handleDeleteConfirm : function() {
			this.setState({
				dialog : false
			});
			api.remove(this.props.name, this.props.id).done(function() {
				this.props.onDelete({
					name : this.props.name,
					id : this.props.id
				});
			}.bind(this));
		},
	});

	modules.exports = React.createFactory(EntityDeleteBtn);
});