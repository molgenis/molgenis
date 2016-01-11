define(function(require, exports, module) {
	/**
	 * @module EntityCreateBtn
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var Button = require('component/Button');
	var Form = require('component/Form');

	var DeepPureRenderMixin = require('component/mixin/DeepPureRenderMixin');

	/**
	 * @memberOf EntityCreateBtn
	 */
	var EntityCreateBtn = React.createClass({
		mixins : [ molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin ],
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

	modules.exports = React.createFactory(EntityCreateBtn);
});