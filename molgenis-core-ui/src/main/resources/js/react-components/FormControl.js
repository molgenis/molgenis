define(function(require, exports, module) {
	/**
	 * @module FormControl
	 */
	"use strict";

	var React = require('react-with-addons.min');
	var _ = require('underscore-min');

	var AttributeControl = require('component/AttributeControl');

	var DeepPureRenderMixin = require('component/mixin/DeepPureRenderMixin');
	var AttributeLoaderMixin = require('component/mixin/AttributeLoaderMixin');

	var div = React.DOM.div, span = React.DOM.span, label = React.DOM.label, strong = React.DOM.strong;

	/**
	 * @memberOf FormControl
	 */
	var FormControl = React.createClass({
		mixins : [ DeepPureRenderMixin, AttributeLoaderMixin ],
		displayName : 'FormControl',
		propTypes : {
			entity : React.PropTypes.object.isRequired,
			entityInstance : React.PropTypes.object,
			attr : React.PropTypes.object.isRequired,
			formLayout : React.PropTypes.oneOf([ 'horizontal', 'vertical' ]),
			mode : React.PropTypes.oneOf([ 'create', 'edit', 'view' ]),
			colOffset : React.PropTypes.number,
			errorMessage : React.PropTypes.string,
			focus : React.PropTypes.bool,
			value : React.PropTypes.any,
			onValueChange : React.PropTypes.func.isRequired,
			onBlur : React.PropTypes.func.isRequired,
			categoricalMrefShowSelectAll : React.PropTypes.bool,
			showAsteriskIfNotNillable : React.PropTypes.bool
		},
		getInitialState : function() {
			return {
				attr : null,
				pristine : true
			};
		},
		getDefaultProps : function() {
			return {
				colOffset : 2
			};
		},
		render : function() {
			if (this.state.attr === null) {
				// attribute not fetched yet
				return Spinner();
			}

			var attr = this.state.attr;

			var lbl = attr.label;

			if ((attr.nillable === false) && (this.props.showAsteriskIfNotNillable === true)) {
				lbl += ' *';
			}

			// add validation error message
			var errorMessage = this.props.errorMessage;
			var showErrorMessage = (errorMessage !== undefined) && (errorMessage !== null);
			var errorMessageSpan = showErrorMessage ? span({
				className : 'help-block'
			}, strong({}, this.props.errorMessage)) : null;

			// determine success and error classes for control
			var formGroupClasses = 'form-group';
			if (showErrorMessage) {
				formGroupClasses += ' has-error';
			}

			var id = attr.name;

			var description = attr.description !== undefined ? FormControlDescriptionFactory({
				description : attr.description
			}) : undefined;
			var labelClasses = this.props.formLayout === 'horizontal' ? 'col-md-' + this.props.colOffset + ' control-label' : 'control-label';
			var labelElement = label({
				className : labelClasses,
				htmlFor : id
			}, lbl);

			var attributeControlProps = _.extend({}, this.props, {
				attr : attr,
				id : id,
				name : id,
				disabled : this.props.mode === 'view',
				focus : this.props.focus,
				formLayout : undefined,
				value : this._getValue(this.props.value),
				onValueChange : this._handleValueChange,
				onBlur : this._handleBlur,
				categoricalMrefShowSelectAll : this.props.categoricalMrefShowSelectAll
			});

			// allow editing readonly controls in create mode
			if (this.props.mode === 'create' && attr.readOnly === true) {
				_.extend(attributeControlProps, {
					readOnly : false,
					required : true
				});
			}
			// show hidden controls and create and edit form
			if ((this.props.mode === 'create' || this.props.mode === 'edit') && attr.visible !== true) {
				attributeControlProps.visible = true;
			}
			// show auto controls as readonly in edit mode
			if (this.props.mode === 'edit' && attr.auto === true) {
				attributeControlProps.readOnly = true;
			}

			var control = AttributeControl(attributeControlProps);

			if (this.props.formLayout === 'horizontal') {
				return (div({
					className : formGroupClasses
				}, labelElement, div({
					className : 'col-md-' + (12 - this.props.colOffset)
				}, control, description, errorMessageSpan)));
			} else {
				return (div({
					className : formGroupClasses
				}, labelElement, description, control, errorMessageSpan));
			}
		},

		_handleValueChange : function(e) {
			this.setState({
				pristine : false
			});

			this.props.onValueChange({
				attr : this.state.attr.name,
				value : this._getValue(e.value),
			});
		},

		_handleBlur : function(e) {
			// only validate if control was touched
			if (this.state.pristine === true || this.props.onBlur === undefined) {
				return;
			}

			e.attr = this.state.attr;
			this.props.onBlur(e);
		},
		_getValue : function(value) {
			// Please don't manipulate the values here. It is not the place to
			// do it!
			return value;
		}
	});

	module.exports = React.createFactory(FormControl);
});
