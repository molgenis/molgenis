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
	
	molgenis.control = molgenis.control || {};

	var api = new molgenis.RestClient();
	
	var div = React.DOM.div, input = React.DOM.input, label = React.DOM.label, textarea = React.DOM.textarea;
	var __spread = React.__spread;

	/**
	 * Range slider control for number types
	 * @memberOf controls
	 */
	var RangeSlider = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'RangeSlider',
		propTypes: {
			range: React.PropTypes.shape({min: React.PropTypes.number.isRequired, max: React.PropTypes.number.isRequired}).isRequired,
			value: React.PropTypes.arrayOf(React.PropTypes.number),
			step: React.PropTypes.string,
			disabled: React.PropTypes.bool,
			onChange: React.PropTypes.func
		},
		render: function() {console.log('render RangeSlider', this.state, this.props);
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
			
			return molgenis.control.wrapper.JQRangeSlider({options: options, disabled: this.props.disabled, value: [fromValue, toValue], onChange: this._handleChange});
		},
		_handleChange: function(event) {console.log('_handleChange RangeSlider', event);
			this.props.onValueChange({value: event.value});
		}
	});
	
	/**
	 * Input control for string and number types
	 * @memberOf controls
	 */
	var InputControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'InputControl',
		propTypes: {
			type: React.PropTypes.string.isRequired,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			step: React.PropTypes.string,
			min: React.PropTypes.string,
			max: React.PropTypes.string,
			maxLength: React.PropTypes.number,
			onValueChange: React.PropTypes.func.isRequired,
			onBlur: React.PropTypes.func
		},
		getInitialState: function() {
			return {value: this.props.value};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				value: nextProps.value // FIXME value in state, checked not in state?
			});
		},
		getDefaultProps: function() {
			return {
				disabled: false,
				required: true
			};
		},
		render: function() {
			var props = this.props;
			return input({
				type: props.type,
				className:  props.type !== 'radio' && props.type !== 'checkbox' ? 'form-control' : undefined,
				id: props.id,
				placeholder: props.placeholder,
				required: props.required,
				disabled: props.disabled,
				step: props.step,
				min: props.min,
				max: props.max,
				maxLength: props.maxLength,
				value: this.state.value,
				checked: props.type === 'radio' || props.type === 'checkbox' ? this.props.checked : undefined,
				onChange: this._handleChange,
				onBlur: props.onBlur
			});
		},
		_handleChange: function(event) {console.log('_handleChange InputControl', event);
			var value;
			if(this.props.type === 'radio' || this.props.type === 'checkbox') {
				value = event.target.checked;
			} else {
				value = event.target.value !== '' ? event.target.value : null;
				if(value !== null && this.props.type === 'number') {
					value = parseFloat(value); // convert js string to js number
				}
			}
			
			this.props.onValueChange({
				value: value,
				validity: event.target.validity // constraint validation API not supported by IE9
			});
		}
	});
	
	/**
	 * Radio group with stacked or inline layout
	 * 
	 * @memberOf controls
	 */
	var RadioGroupControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'RadioGroupControl',
		propTypes: {
			layout: React.PropTypes.string,
			nillable: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			options: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
			value: React.PropTypes.string,
			onValueChange: React.PropTypes.func
		},
		getDefaultProps: function() {
			return {
				layout: 'stacked',
				nillable: false,
				disabled: false
			};
		},
		getInitialState: function() {
			return {
				value: this._valueToInputValue(this.props.value)
			};
		},
		render: function() {console.log('render RadioGroupControl', this.state, this.props);
			var options = this.props.options;
			if(this.props.nillable) {
				options = options.concat({value: '', label: 'N/A'});
			}
			
			var self = this;
			var inputs = $.map(options, function(option, i) {
				var control = input({type: 'radio', name: 'bool-radio', checked: option.value === self._valueToInputValue(self.state.value), disabled: self.props.disabled, value: option.value, onChange: self._handleChange});
				if(self.props.layout === 'stacked') {
					var divClasses = self.props.disabled ? 'radio disabled' : 'radio';
					return (
						div({className: divClasses, key: '' + i},
							label({},
								control, option.label
							)
						)
					);
				} else {
					var labelClasses = self.props.disabled ? 'radio-inline disabled' : 'radio-inline';
					return (
						label({className: labelClasses, key: '' + i},
							control, option.label
						)
					);
				}
			});
			return div({}, inputs);
		},
		_handleChange: function(event) {console.log('_handleChange RadioGroupControl', event);
			var value = this._inputToValue(event.target.value);
			this.setState({value: value});
			this.props.onValueChange({value: value});
		},
		_valueToInputValue: function(value) {
			return value === null ? '' : value;
		},
		_inputToValue: function(value) {
			return value === '' ? null : value;
		}
	});

	/**
	 * Checkbox group with stacked or inline layout
	 * 
	 * @memberOf controls
	 */
	var CheckboxGroupControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'CheckboxGroupControl',
		propTypes: {
			layout: React.PropTypes.string,
			nillable: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			options: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
			value: React.PropTypes.arrayOf(React.PropTypes.string),
			onValueChange: React.PropTypes.func
		},
		getDefaultProps: function() {
			return {
				layout: 'stacked',
				nillable: false,
				disabled: false,
				value: []
			};
		},
		getInitialState: function() {
			return {
				value: this._valueToInputValue(this.props.value)
			};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				value: this._valueToInputValue(nextProps.value)
			});
		},
		render: function() {console.log('render CheckboxGroupControl', this.state, this.props);
			var options = this.props.options;
			if(this.props.nillable) {
				options = options.concat({value: '', label: 'N/A'});
			}
			
			var self = this;
			var inputs = $.map(options, function(option, i) {
				var checked = self._valueToInputValue(self.state.value).indexOf(option.value) > -1;
				var control = input({type: 'checkbox', name: 'bool-radio', checked: checked, disabled: self.props.disabled, value: option.value, onChange: self._handleChange});
				if(self.props.layout === 'stacked') {
					var divClasses = self.props.disabled ? 'checkbox disabled' : 'checkbox';
					return (
						div({className: divClasses, key: '' + i},
							label({},
								control, option.label
							)
						)
					);
				} else {
					var labelClasses = self.props.disabled ? 'checkbox-inline disabled' : 'checkbox-inline';
					return (
						label({className: labelClasses, key: '' + i},
							control, option.label
						)
					);
				}
			});
			return div({}, inputs);
		},
		_handleChange: function(event) {console.log('_handleChange CheckboxGroupControl', event);
			var value = this._inputToValue(event.target.value);
			
			var values = this.state.value;
			if(event.target.checked) {
				values = values.concat(value);
			} else {
				values = values.slice(0); 
				values.splice(values.indexOf(value), 1);
			}
			
			this.setState({value: values});
			this.props.onValueChange({value: values});
		},
		_valueToInputValue: function(values) {
			if(values !== undefined) {
				// do not use $.map since it removes null values
				for(var i = 0; i < values.length; ++i)
					values[i] = values[i] === null ? '' : values[i];
				return values;
			} else {
				return [];
			}
		},
		_inputToValue: function(values) {
			if(values !== undefined) {
				// do not use $.map since it removes null values
				for(var i = 0; i < values.length; ++i)
					values[i] = values[i] === '' ? null : values[i];
				return values;
			} else {
				return [];
			}
		}
	});

	/**
	 * Input control for BOOL type with checkbox or radio buttons
	 * @memberOf controls
	 */
	var BoolControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'BoolControl',
		propTypes: {
			layout: React.PropTypes.string,
			multiple: React.PropTypes.bool,
			nillable: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.oneOfType([React.PropTypes.bool, React.PropTypes.array]),
			onValueChange: React.PropTypes.func
		},
		getDefaultProps: function() {
			return {
				type: 'single',
				layout: 'inline'
			};
		},
		render: function() {console.log('render BoolControl', this.state, this.props);
			if(this.props.multiple || this.props.nillable || this.props.type === 'group') {
				var options = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];
				var Element = this.props.multiple ? molgenis.control.CheckboxGroupControl : molgenis.control.RadioGroupControl;
				return Element({options: options, nillable: this.props.nillable, disabled: this.props.disabled, layout: this.props.layout, value: this._boolToString(this.props.value), onValueChange: this._handleValueChange});
			} else {
				return molgenis.control.InputControl({type: 'checkbox', id: this.props.id, required: this.props.required, disabled: this.props.disabled, checked: this.props.value, onValueChange: this.props.onValueChange});
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
	
	/**
	 * Input control for DATE and DATE_TIME types
	 * @memberOf controls
	 */
	var DateControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'DateControl',
		propTypes: {
			time: React.PropTypes.bool,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.string,
			onValueChange: React.PropTypes.func
		},
		getInitialState: function() {
			return {value: this.props.value};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				value: nextProps.value
			});
		},
		render: function() {console.log('render DateControl', this.state, this.props);
			return molgenis.control.wrapper.DateTimePicker({time: this.props.time, placeholder: this.props.placeholder, required: this.props.required, disabled: this.props.disabled, value: this.state.value, onChange: this._handleChange});
		},
		_handleChange: function(value) {console.log('_handleChange DateControl', value);
			this.setState(value);
			this.props.onValueChange(value);
		}
	});

	/**
	 * Input control for CATEGORICAL, XREF and MREF types
	 * @memberOf controls
	 */
	var EntityControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'EntityControl',
		propTypes: {
			// TODO add entity
			multiple: React.PropTypes.bool,
			nillable: React.PropTypes.bool,
			placeholder: React.PropTypes.string,
			value: React.PropTypes.oneOfType([React.PropTypes.object, React.PropTypes.array]),
			onValueChange: React.PropTypes.func
		},
		getInitialState: function() {
			// initialize with entity meta if exists
			var entity = this.props.entity;
			return {
				entity: entity.idAttribute !== undefined ? entity : null
			};
		},
		componentDidMount: function() {console.log('componentDidMount EntityControl');
			// fetch entity meta if not exists
			var entity = this.props.entity;
			if(entity.idAttribute === undefined) {
				var self = this;
				api.getAsync(entity.href).done(function(entity) {
					if (self.isMounted()) {
						self.setState({entity: entity});
					}
				});	
			}
		},
		render: function() {console.log('render EntityControl', this.state, this.props);
			if(this.state.entity === null) {
				// entity meta data not fetched yet
				return div({});
			}
			
			var props = this.props;
			var entity = this.state.entity;
			
			var format = function(item) {
				if (item)
					return item[entity.labelAttribute];
			};
			
			var self = this;
			var options = {
				enable: !this.props.disabled,
				id: entity.idAttribute,
				multiple: props.multiple,
				allowClear : props.nillable ? true : false,
				placeholder : props.placeholder || ' ', // cannot be an empty string
				initSelection: function(element, callback) {
					if(self.props.value)
						callback([self.props.value]);
				},
			    query: function (query) {
			    	var num = 25;
				    var q = {
						q : {
							start : (query.page - 1) * num, 
							num : num
						}
					};
			    	
			    	api.getAsync(entity.hrefCollection, q).done(function(data) {
			    		query.callback({results: data.items, more: data.nextHref ? true : false});
			    	});
			    },
			    formatResult: format,
			    formatSelection: format
			};
			return molgenis.control.wrapper.Select2({options: options, disabled: this.props.disabled, value: this.props.value, onChange: this._handleChange});
		},
		_handleChange: function(value) {console.log('_handleChange EntityControl', value);
			var val = this.props.multiple && value.length === 0 ? undefined : value;
			this.props.onValueChange({value: val});
		}
	});
	
	/**
	 * @memberOf controls
	 */
	var TextControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'TextControl',
		propTypes: {
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.string,
			onValueChange: React.PropTypes.func
		},
		render: function() {console.log('render TextControl', this.state, this.props);
			return textarea({
				className: 'form-control',
				id: this.props.id,
				placeholder: this.props.placeholder,
				required: this.props.required,
				disabled: this.props.disabled,
				value: this.props.value,
				onChange: this._handleChange});
		},
		_handleChange: function(value) {console.log('_handleChange TextControl', value);
			var val = value !== '' ? value : null;
			this.props.onValueChange({value: val});
		}
	});
	
	/**
	 * @memberOf controls
	 */
	var AttributeControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'AttributeControl',
		getInitialState: function() {
			return {options: undefined};
		},
		// FIXME add propTypes
		componentDidMount: function() {console.log('componentDidMount AttributeFormControl');
			if(this.props.attr.fieldType === 'CATEGORICAL') {
				// retrieve all categories
				var self = this;
				api.getAsync(self.props.attr.refEntity.href).done(function(meta) {
					var idAttr = meta.idAttribute;
					var lblAttr = meta.labelAttribute;
					
					if (self.isMounted()) {
						api.getAsync(self.props.attr.refEntity.hrefCollection, {'attributes' : [idAttr, lblAttr]}).done(function(data) { // FIXME problems in case of large number of categories
							if (self.isMounted()) {
								var options = $.map(data.items, function(entity, i) {
									return {value: entity[idAttr], label: entity[lblAttr]};
								});
								self.setState({options: options});
							}
						});	
					}
				});
			}
		},
		render: function() {console.log('render AttributeControl', this.state, this.props);
			var props = this.props;
			var attr = props.attr;
			
			switch(attr.fieldType) {
				case 'BOOL':
					var layout = props.layout || 'checkbox';
					return molgenis.control.BoolControl({label: props.label, nillable: attr.nillable, disabled: props.disabled, layout: layout, value: props.value, onValueChange: this._handleValueChange});
				case 'CATEGORICAL':
					if(this.state.options === undefined) {
						// options not yet available
						return div();
					}
//					var placeholder = props.placeholder || 'Select a Category';
//					var multiple = props.multiple || false;
//					return this._createEntityControl(multiple, placeholder);
					
					var Element = props.multiple === true ? molgenis.control.CheckboxGroupControl : molgenis.control.RadioGroupControl;
					return Element({options: this.state.options, nillable: attr.nillable, disabled: props.disabled, layout: props.layout, value: props.value, onValueChange: this._handleValueChange});
				case 'DATE':
					var placeholder = props.placeholder || 'Date';
					return this._createDateControl(false, placeholder);
				case 'DATE_TIME':
					var placeholder = props.placeholder || 'Date';
					return this._createDateControl(true, placeholder);
				case 'DECIMAL':
					return this._createNumberControl('any');
				case 'EMAIL':
					var placeholder = props.placeholder || 'Email';
					return this._createStringControl('email', placeholder);
				case 'ENUM':
					var options = $.map(attr.enumOptions, function(option){return {value: option, label: option};});
					var Element = props.multiple === true ? molgenis.control.CheckboxGroupControl : molgenis.control.RadioGroupControl;
					return Element({options: options, nillable: attr.nillable, disabled: props.disabled, layout: props.layout, value: props.value, onValueChange: this._handleValueChange});
//					var placeholder = props.placeholder || 'Select an Option';
//					var multiple = props.multiple || false;
//					return React.createElement(EnumControl, {multiple: multiple, placeholder: placeholder, required: props.required, disabled: props.disabled, options: attr.enumOptions, value: props.value, onValueChange: this._handleValueChange});
				case 'HTML':
					return this._createTextControl();
				case 'HYPERLINK':
					var placeholder = props.placeholder || 'URL';
					return this._createStringControl('url', placeholder);
				case 'INT':
				case 'LONG':
					return this._createNumberControl('1');
				case 'XREF':
					var placeholder = props.placeholder || 'Search for a Value';
					var multiple = props.multiple || false;
					return this._createEntityControl(multiple, placeholder);
				case 'MREF':
					var placeholder = props.placeholder || 'Search for Values';
					var multiple = props.multiple || true;
					return this._createEntityControl(multiple, placeholder);
				case 'SCRIPT':
					return this._createTextControl();
				case 'STRING':
					var placeholder = props.placeholder || '';
					return this._createStringControl('text', placeholder);
				case 'TEXT':
					return this._createTextControl();
				case 'COMPOUND' :
				case 'FILE':
				case 'IMAGE':
					throw 'Unsupported data type: ' + attr.fieldType;
				default:
					throw 'Unknown data type: ' + attr.fieldType;
			}
		},
		_handleValueChange: function(event) {console.log('_handleChange AttributeControl', event);
			this.props.onValueChange(__spread({}, event, {attr: this.props.attr.name}));
		},
		_createNumberControl: function(step) {
			var min = this.props.range ? this.props.range.min : undefined;
			var max = this.props.range ? this.props.range.max : undefined;
			var placeholder = this.props.placeholder || 'Number';
			return molgenis.control.InputControl({type: 'number', id: this.props.id, placeholder: placeholder, required: this.props.required, disabled: this.props.disabled, step: step, min: min, max: max, value: this.props.value, onValueChange: this._handleValueChange, onBlur: this.props.onBlur});
		},
		_createStringControl: function(type, placeholder) {
			return molgenis.control.InputControl({type: type, id: this.props.id, placeholder: placeholder, required: this.props.required, disabled: this.props.disabled, maxlength: '255', value: this.props.value, onValueChange: this._handleValueChange, onBlur: this.props.onBlur});
		},
		_createDateControl: function(time, placeholder) {
			return molgenis.control.DateControl({id: this.props.id, placeholder: placeholder, required: this.props.required, disabled: this.props.disabled, time: time, value: this.props.value, onValueChange: this._handleValueChange});
		},
		_createTextControl: function() {
			return molgenis.control.TextControl({id: this.props.id, placeholder: this.props.placeholder, required: this.props.required, disabled: this.props.disabled, value: this.props.value, onValueChange: this._handleValueChange});
		},
		_createEntityControl: function(multiple, placeholder) {
			var props = this.props;
			return molgenis.control.EntityControl({id: this.props.id, placeholder: placeholder, nillable: props.attr.nillable, multiple: multiple, disabled: this.props.disabled, entity: props.attr.refEntity, value: props.value, onValueChange: this._handleValueChange});
		}
	});
	
	// export module
	molgenis.control = molgenis.control || {};
	
	$.extend(molgenis.control, {
		RangeSlider: React.createFactory(RangeSlider),
		InputControl: React.createFactory(InputControl),
		RadioGroupControl: React.createFactory(RadioGroupControl),
		CheckboxGroupControl: React.createFactory(CheckboxGroupControl),
		BoolControl: React.createFactory(BoolControl),
		DateControl: React.createFactory(DateControl),
		EntityControl: React.createFactory(EntityControl),
		TextControl: React.createFactory(TextControl),
		AttributeControl: React.createFactory(AttributeControl)
	});
}($, window.top.molgenis = window.top.molgenis || {}));