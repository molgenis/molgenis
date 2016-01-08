define(function(require, exports, module) {
	/**
	 * @module CodeEditor
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var DeepPureRenderMixin = require('component/mixin/DeepPureRenderMixin');
	var Ace = require('component/wrapper/Ace')

	/**
	 * @memberOf CodeEditor
	 */
	var CodeEditor = React.createClass({
		mixins : [ DeepPureRenderMixin ],
		displayName : 'CodeEditor',
		propTypes : {
			id : React.PropTypes.string,
			name : React.PropTypes.string,
			placeholder : React.PropTypes.string,
			required : React.PropTypes.bool,
			disabled : React.PropTypes.bool,
			readOnly : React.PropTypes.bool,
			maxLength : React.PropTypes.number,
			mode : React.PropTypes.string,
			value : React.PropTypes.string,
			onValueChange : React.PropTypes.func.isRequired
		},
		render : function() {
			return Ace({
				id : this.props.id,
				name : this.props.name,
				placeholder : this.props.placeholder,
				required : this.props.required,
				disabled : this.props.disabled,
				readOnly : this.props.readOnly,
				maxLength : this.props.maxLength,
				mode : this.props.language,
				value : this.props.value,
				onChange : this._handleChange
			});
		},
		_handleChange : function(value) {
			this.props.onValueChange({
				value : value !== '' ? value : null
			});
		}
	});

	module.exports = React.createFactory(CodeEditor)
});