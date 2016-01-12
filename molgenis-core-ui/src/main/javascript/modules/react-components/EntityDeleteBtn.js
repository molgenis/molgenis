/**
 * @module EntityDeleteBtn
 */


import React from 'react';
import _ from 'underscore';

import api from '../RestClientV2';
import Button from './Button';
import Dialog from './Dialog';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';
import ReactLayeredComponentMixin from './mixin/ReactLayeredComponentMixin';

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

export default React.createFactory(EntityDeleteBtn);
