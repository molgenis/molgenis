define(function(require, exports, module) {
	/**
	 * @module EntityInspectBtn
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var Button = require('component/Button');

	var DeepPureRenderMixin = require('component/mixin/DeepPureRenderMixin');

	/**
	 * @memberOf EntityInspectBtn
	 */
	var EntityInspectBtn = React.createClass({
		mixins : [ DeepPureRenderMixin ],
		displayName : 'EntityInspectBtn',
		propTypes : {
			name : React.PropTypes.string.isRequired,
			id : React.PropTypes.oneOfType([ React.PropTypes.string, React.PropTypes.number ]).isRequired,
			onInspect : React.PropTypes.func
		},
		getInitialState : function() {
			return {
				dialog : false
			};
		},
		getDefaultProps : function() {
			return {
				onInspect : function() {
				}
			};
		},
		render : function() {
			return Button({
				icon : 'search',
				style : 'info',
				title : 'Inspect row',
				size : 'xsmall',
				onClick : this._handleClick
			});
		},
		_handleClick : function() {
			this.props.onInspect({
				name : this.props.name,
				id : this.props.id
			});
		}
	});

	modules.exports = React.createFactory(EntityInspectBtn);
});