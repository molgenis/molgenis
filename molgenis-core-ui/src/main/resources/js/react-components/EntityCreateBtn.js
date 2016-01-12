/**
 * @module EntityCreateBtn
 */
import React from 'react';
import _ from 'underscore';

import Button from './Button';
import Form from './Form';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';
import ReactLayeredComponentMixin from './mixin/ReactLayeredComponentMixin';

/**
 * @memberOf EntityCreateBtn
 */
var EntityCreateBtn = React.createClass({
	mixins : [ DeepPureRenderMixin, ReactLayeredComponentMixin ],
	displayName : 'EntityCreateBtn',
	propTypes : {
		entity : React.PropTypes.object.isRequired,
		onCreate : React.PropTypes.func
	},
	getInitialState : function() {
		return {
			form : false
		};
	},
	getDefaultProps : function() {
		return {
			onCreate : function() {
			}
		};
	},
	render : function() {
		return Button({
			icon : 'plus',
			title : 'Add row',
			style : 'success',
			size : 'xsmall',
			onClick : this._handleCreate
		});
	},
	renderLayer : function() {
		return this.state.form ? Form({
			entity : this.props.entity.name,
			mode : 'create',
			showHidden : true,
			modal : true,
			onSubmitSuccess : this._handleCreateConfirm,
			onSubmitCancel : this._handleCreateCancel
		}) : null;
	},
	_handleCreate : function() {
		this.setState({
			form : true
		});
	},
	_handleCreateCancel : function() {
		this.setState({
			form : false
		});
	},
	_handleCreateConfirm : function(e) {
		this.setState({
			form : false
		});
		this.props.onCreate({
			href : this.props._href
		});
	}
});

export default React.createFactory(EntityCreateBtn);
