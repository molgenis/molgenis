/**
 * @module EntityEditBtn
 */
"use strict";

import React from 'react';
import _ from 'underscore';

import Button from './Button';
import Form from './Form';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';
import ReactLayeredComponentMixin from './mixin/ReactLayeredComponentMixin';

/**
 * @memberOf EntityEditBtn
 */
var EntityEditBtn = React.createClass({
	mixins : [ DeepPureRenderMixin, ReactLayeredComponentMixin ],
	displayName : 'EntityEditBtn',
	propTypes : {
		name : React.PropTypes.string.isRequired,
		id : React.PropTypes.oneOfType([ React.PropTypes.string, React.PropTypes.number ]).isRequired,
		onEdit : React.PropTypes.func
	},
	getInitialState : function() {
		return {
			form : false
		};
	},
	getDefaultProps : function() {
		return {
			onEdit : function() {
			}
		};
	},
	render : function() {
		return Button({
			icon : 'edit',
			title : 'Edit row',
			size : 'xsmall',
			onClick : this._handleEdit
		});
	},
	renderLayer : function() {
		return this.state.form ? Form({
			entity : this.props.name,
			entityInstance : this.props.id,
			mode : 'edit',
			showHidden : true,
			modal : true,
			onSubmitSuccess : this._handleEditConfirm,
			onSubmitCancel : this._handleEditCancel
		}) : null;
	},
	_handleEdit : function() {
		this.setState({
			form : true
		});
	},
	_handleEditCancel : function() {
		this.setState({
			form : false
		});
	},
	_handleEditConfirm : function() {
		this.setState({
			form : false
		});
		this.props.onEdit({
			name : this.props.name,
			id : this.props.id
		});
	}
});

export default React.createFactory(EntityEditBtn);

