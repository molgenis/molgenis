/**
 * MOLGENIS attribute controls for all data types
 * 
 * Dependencies: TODO
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	var div = React.DOM.div, input = React.DOM.input, label = React.DOM.label, textarea = React.DOM.textarea;
	
	/**
	 * @memberOf control
	 */
	var InputControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'InputControl',
		propTypes: {
			type: React.PropTypes.string.isRequired,
			id: React.PropTypes.string,
			name: React.PropTypes.string,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			step: React.PropTypes.string,
			min: React.PropTypes.string,
			max: React.PropTypes.string,
			maxLength: React.PropTypes.number,
			value: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
			checked: React.PropTypes.bool,
			onValueChange: React.PropTypes.func.isRequired,
			onBlur: React.PropTypes.func
		},
		getInitialState: function() {
			return this._isRadioOrCheckbox() ? {checked: this.props.checked} : {value: this.props.value};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState(this._isRadioOrCheckbox() ? {checked: nextProps.checked} : {value: nextProps.value});
		},
		render: function() {//console.log('render InputControl', this.state, this.props);
			var props = this.props;
			
			var inputProps = {
				type: props.type,
				className: this._isRadioOrCheckbox() ? undefined : 'form-control',
				id: props.id,
				name: props.name,
				placeholder: props.placeholder,
				required: props.required,
				disabled: props.disabled,
				readOnly: props.readOnly,
				step: props.step,
				min: props.min,
				max: props.max,
				maxLength: props.maxLength,
				value: this._isRadioOrCheckbox() ? this.props.value : this.state.value,
				checked: this._isRadioOrCheckbox() ? this.state.checked : undefined,
				onChange: this._handleChange,
				onBlur: props.onBlur
			};
			
			if(props.readOnly && this._isRadioOrCheckbox()) {
				// readonly attribute doesn't work on checkboxes and radio buttons:
				// http://stackoverflow.com/questions/155291/can-html-checkboxes-be-set-to-readonly
				_.extend(inputProps, {name: undefined, disabled: true});
				
				 if(this.state.checked) {
					 // for checked checkboxes/radio buttons submit the value of a hidden input,
					 // disable visible input to display a readonly view to the user 
					 return div({},
						input({
							type: 'hidden',
							name: props.name,
							value: props.value
						}),
						input(inputProps)
					);	 
				 } else {
					// values of unchecked checkboxes/radio buttons are never submitted:
					// http://www.w3.org/TR/html401/interact/forms.html#h-17.13.2
				 	return input(inputProps);
				 }
			} else {
				return input(inputProps);
			}
		},
		_handleChange: function(event) {//console.log('_handleChange InputControl', event);
			this.setState(this._isRadioOrCheckbox() ? {checked: event.target.checked} : {value: event.target.value});
			
			var valueEvent;
			if(this._isRadioOrCheckbox()) {
				valueEvent = {value: this._emptyValueToNull(event.target.value), checked: event.target.checked};
			} else {
				var value = this._emptyValueToNull(event.target.value);
				
				if(this.props.type === 'number' && value !== null) {
					value = parseFloat(value); // convert js string to js number
				}
				valueEvent = {value: value};
			}
			
			this.props.onValueChange(valueEvent);
		},
		_isRadioOrCheckbox: function() {
			return this.props.type === 'radio' || this.props.type === 'checkbox';
		},
		_emptyValueToNull: function(value) {
			return value !== '' ? value : null;
		}
	});
	
	/**
	 * Mixin containing common code for RadioGroupControl and CheckboxGroupControl
	 *  
	 * @memberOf control.mixin
	 */
	var GroupControlMixin = {
		render: function() {//console.log('render GroupControlMixin', this.state, this.props);
			var options = this.props.options;
			if(!this.props.required) {
				options = options.concat({value: '', label: 'N/A'});
			}
			
			var type = this.props.type;
			var inputs = $.map(options, function(option, i) {
				var control = molgenis.control.InputControl({
					type : type,
					name : this.props.name,
					checked : this._isChecked(option),
					disabled : this.props.disabled,
					readOnly: this.props.readOnly,
					value : option.value,
					onValueChange : this._handleChange
				});
				if(this.props.layout === 'vertical') {
					var divClasses = this.props.disabled ? type + ' disabled' : type;
					return (
						div({className: divClasses, key: '' + i},
							label({},
								control, option.label
							)
						)
					);
				} else {
					var labelClasses = this.props.disabled ? type + '-inline disabled' : type + '-inline';
					return (
						label({className: labelClasses, key: '' + i},
							control, option.label
						)
					);
				}
			}.bind(this));
			return div({}, inputs);
		},
		_inputToValue: function(value) {
			return value === '' ? null : value;
		}
	};
	
	/**
	 * layout: stacked or inline
	 * 
	 * @memberOf control
	 */
	var RadioGroupControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin, GroupControlMixin],
		displayName: 'RadioGroupControl',
		propTypes: {
			name: React.PropTypes.string.isRequired,
			layout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			options: React.PropTypes.arrayOf(React.PropTypes.shape({value: React.PropTypes.string, label: React.PropTypes.string})).isRequired,
			value: React.PropTypes.string,
			onValueChange: React.PropTypes.func.isRequired
		},
		getDefaultProps: function() {
			return {
				type: 'radio',
				layout: 'vertical'
			};
		},
		getInitialState: function() {
			return {
				value: this.props.value
			};
		},
		_handleChange: function(event) {//console.log('_handleChange RadioGroupControl', event);			
			this.setState({value: event.value});
			this.props.onValueChange({value: event.value});
		},
		_isChecked: function(value) {
			return this.state.value === this._inputToValue(value.value);
		}
	});

	/**
	 * layout: stacked or inline
	 * 
	 * @memberOf control
	 */
	var CheckboxGroupControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin, GroupControlMixin],
		displayName: 'CheckboxGroupControl',
		propTypes: {
			name: React.PropTypes.string,
			layout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			options: React.PropTypes.arrayOf(React.PropTypes.shape({value: React.PropTypes.string, label: React.PropTypes.string})).isRequired,
			value: React.PropTypes.arrayOf(React.PropTypes.string),
			onValueChange: React.PropTypes.func.isRequired
		},
		getDefaultProps: function() {
			return {
				type: 'checkbox',
				layout: 'vertical'
			};
		},
		getInitialState: function() {
			return {
				value: this.props.value || []
			};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				value: nextProps.value || []
			});
		},
		_handleChange: function(event) {//console.log('_handleChange CheckboxGroupControl', event);
			var value = this._inputToValue(event.value);
			
			var values = this.state.value;
			if(event.checked) {
				values = values.concat(value);
			} else {
				values = values.slice(0); 
				values.splice(values.indexOf(value), 1);
			}
			
			this.setState({value: values});
			this.props.onValueChange({value: values});
		},
		_isChecked: function(value) {
			return this.state.value && this.state.value.indexOf(this._inputToValue(value)) > -1;
		}
	});

	/**
	 * @memberOf control
	 */
	var DateControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'DateControl',
		propTypes: {
			name: React.PropTypes.string,
			time: React.PropTypes.bool,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.string,
			onValueChange: React.PropTypes.func.isRequired
		},
		render: function() {//console.log('render DateControl', this.state, this.props);
			return molgenis.control.wrapper.DateTimePicker({
				name: this.props.name,
				time : this.props.time,
				placeholder : this.props.placeholder,
				required : this.props.required,
				disabled : this.props.disabled,
				readOnly : this.props.readOnly,
				value : this.props.value,
				onChange : this._handleChange
			});
		},
		_handleChange: function(value) {
			this.props.onValueChange({value: value});
		}
	});
	
	/**
	 * @memberOf control
	 */
	var TextAreaControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'TextAreaControl',
		propTypes: {
			id: React.PropTypes.string,
			name: React.PropTypes.string,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			value: React.PropTypes.string,
			onValueChange: React.PropTypes.func.isRequired
		},
		getInitialState: function() {
			return {value: this.props.value};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				value: nextProps.value
			});
		},
		render: function() {//console.log('render TextAreaControl', this.state, this.props);
			return textarea({
				className: 'form-control',
				id: this.props.id,
				name: this.props.name,
				placeholder: this.props.placeholder,
				required: this.props.required,
				disabled: this.props.disabled,
				readOnly: this.props.readOnly,
				value: this.state.value,
				onChange: this._handleChange});
		},
		_handleChange: function(event) {//console.log('_handleChange TextAreaControl', event);
			this.setState({value: event.target.value});
			
			var value = event.target.value !== '' ? event.target.value : null;
			this.props.onValueChange({value: value});
		}
	});
	
	/**
	 * @memberOf control
	 */
	var CodeEditorControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'CodeEditorControl',
		propTypes: {
			id : React.PropTypes.string,
			name: React.PropTypes.string,
			placeholder : React.PropTypes.string,
			required : React.PropTypes.bool,
			disabled : React.PropTypes.bool,
			readOnly : React.PropTypes.bool,
			mode: React.PropTypes.string,
			value : React.PropTypes.string,
			onValueChange : React.PropTypes.func.isRequired
		},
		render: function() {//console.log('render CodeEditorControl', this.state, this.props);
			return molgenis.control.wrapper.Ace({
				id : this.props.id,
				name: this.props.name,
				placeholder : this.props.placeholder,
				required : this.props.required,
				disabled : this.props.disabled,
				readOnly : this.props.readOnly,
				mode: this.props.language,
				value : this.props.value,
				onChange : this._handleChange
			});
		},
		_handleChange: function(value) {//console.log('_handleChange RangeSlider', event);
			this.props.onValueChange({value: value !== '' ? value : null});
		}
	});
	
	/**
	 * Range slider control for number types
	 * @memberOf control
	 */
	var RangeSlider = React.createClass({ // TODO add tests FIXME support readOnly
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'RangeSlider',
		propTypes: {
			id : React.PropTypes.string,
			range: React.PropTypes.shape({min: React.PropTypes.number.isRequired, max: React.PropTypes.number.isRequired}).isRequired,
			value: React.PropTypes.arrayOf(React.PropTypes.number),
			step: React.PropTypes.string,
			disabled: React.PropTypes.bool,
			onValueChange : React.PropTypes.func.isRequired
		},
		render: function() {//console.log('render RangeSlider', this.state, this.props);
			var range = this.props.range;
			var value = this.props.value;
			
			var fromValue = value && value[0] ? value[0] : range.min;
			var toValue = value && value[1] ? value[1] : range.max;
			var options = {
				symmetricPositionning: true,
				bounds: {min: range.min, max: range.max},
				defaultValues: {min: fromValue, max: toValue},
				step: this.props.step,
				type: 'number'
			};
			
			return molgenis.control.wrapper.JQRangeSlider({
				id: this.props.id,
				options : options,
				disabled : this.props.disabled,
				value : [ fromValue, toValue ],
				onChange : this._handleChange
			});
		},
		_handleChange: function(value) {//console.log('_handleChange RangeSlider', event);
			this.props.onValueChange({value: value});
		}
	});
	
	/**
	 * Input control for BOOL type with checkbox or radio buttons
	 * @memberOf control
	 */
	var BoolControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'BoolControl',
		propTypes: {
			id: React.PropTypes.string,
			name: React.PropTypes.string,
			label: React.PropTypes.string,
			layout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			type: React.PropTypes.oneOf(['single', 'group']),
			multiple: React.PropTypes.bool,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			value: React.PropTypes.oneOfType([React.PropTypes.bool, React.PropTypes.array]),
			onValueChange: React.PropTypes.func.isRequired
		},
		getDefaultProps: function() {
			return {
				type: 'single',
				layout: 'horizontal',
				required: true
			};
		},
		render: function() {//console.log('render BoolControl', this.state, this.props);
			if(this.props.multiple || !this.props.required || this.props.type === 'group') {
				var options = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];
				var Element = this.props.multiple ? molgenis.control.CheckboxGroupControl : molgenis.control.RadioGroupControl;
				return Element({
					id: this.props.id,
					name: this.props.name,
					options : options,
					required : this.props.required,
					disabled : this.props.disabled,
					readOnly: this.props.readOnly,
					layout : this.props.layout,
					value : this._boolToString(this.props.value),
					onValueChange : this._handleValueChange
				});
			} else {
				return molgenis.control.InputControl({
					type : 'checkbox',
					id : this.props.id,
					name: this.props.name,
					value: this.props.label,
					required : this.props.required,
					disabled : this.props.disabled,
					readOnly: this.props.readOnly,
					checked : this.props.value,
					onValueChange : this.props.onValueChange
				});
			}
		},
		_handleValueChange: function(e) {
			this.props.onValueChange({value: this._stringToBool(e.value)});
		},
		_boolToString: function(value) {
			if(this.props.multiple) {
				// do not use $.map since it removes null values
				if(value !== undefined) {
					value = value.slice(0);
					for(var i = 0; i < value.length; ++i)
						value[i] = value[i] === true ? 'true' : (value[i] === false ? 'false' : value[i]);
				}
				return value;
			} else {
				return value === true ? 'true' : (value === false ? 'false' : value);
			}
		},
		_stringToBool: function(value) {
			if(this.props.multiple) {
				// do not use $.map since it removes null values
				if(value !== undefined) {
					value = value.slice(0);
					for(var i = 0; i < value.length; ++i)
						value[i] = value[i] === 'true' ? true : (value[i] === 'false' ? false : value[i]);
				}
				return value;
			} else {
				return value === 'true' ? true : (value === 'false' ? false : value);
			}
		}
	});
	
	// export module
	molgenis.control = molgenis.control || {};
	
	$.extend(molgenis.control, {
		InputControl: React.createFactory(InputControl),
		RadioGroupControl: React.createFactory(RadioGroupControl),
		CheckboxGroupControl: React.createFactory(CheckboxGroupControl),
		DateControl: React.createFactory(DateControl),
		TextAreaControl: React.createFactory(TextAreaControl),
		CodeEditorControl: React.createFactory(CodeEditorControl),
		RangeSlider: React.createFactory(RangeSlider),
		BoolControl: React.createFactory(BoolControl)
	});
}($, window.top.molgenis = window.top.molgenis || {}));