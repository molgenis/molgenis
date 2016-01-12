define(function(require, exports, module) {
	/**
	 * @module RadioGroup
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var DeepPureRenderMixin = require('./mixin/DeepPureRenderMixin');
	var GroupMixin = require('./mixin/GroupMixin');

	/**
	 * @memberOf RadioGroup
	 */
	var RadioGroup = React.createClass({
		displayName : 'RadioGroup',
		mixins : [ DeepPureRenderMixin, GroupMixin ],
		propTypes : {
			name : React.PropTypes.string.isRequired,
			layout : React.PropTypes.oneOf([ 'horizontal', 'vertical' ]),
			required : React.PropTypes.bool,
			disabled : React.PropTypes.bool,
			readOnly : React.PropTypes.bool,
			options : React.PropTypes.arrayOf(React.PropTypes.shape({
				value : React.PropTypes.string,
				label : React.PropTypes.string
			})).isRequired,
			focus : React.PropTypes.bool,
			value : React.PropTypes.string,
			onValueChange : React.PropTypes.func.isRequired
		},
		getDefaultProps : function() {
			return {
				type : 'radio',
				layout : 'vertical'
			};
		},
		getInitialState : function() {
			return {
				value : this.props.value
			};
		},
		componentWillReceiveProps : function(nextProps) {
			if (this.state.value !== nextProps.value) {
				this.setState({
					value : nextProps.value
				});
			}
		},
		_handleChange : function(event) {
			this.setState({
				value : event.value
			});
			this.props.onValueChange({
				value : event.value
			});
		},
		_isChecked : function(option) {
			var value = this.state.value === undefined && !this.props.required ? null : this.state.value;
			return value === this._inputToValue(option.value);
		}
	});

	module.exports = React.createFactory(RadioGroup)
});