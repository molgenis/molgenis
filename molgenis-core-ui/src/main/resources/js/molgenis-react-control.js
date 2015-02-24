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
	
	molgenis.controls = molgenis.controls || {};

	var api = new molgenis.RestClient();
	
	var div = React.DOM.div, input = React.DOM.input, label = React.DOM.label, textarea = React.DOM.textarea;
	var Select2 = React.createFactory(molgenis.controls.Select2), JQRangeSlider = React.createFactory(molgenis.controls.JQRangeSlider), DateTimePicker = React.createFactory(molgenis.controls.DateTimePicker);
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
			
			return JQRangeSlider({options: options, disabled: this.props.disabled, value: [fromValue, toValue], onChange: this._handleChange});
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
				value: nextProps.value
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
				className: 'form-control',
				id: props.id,
				placeholder: props.placeholder,
				required: props.required,
				disabled: props.disabled,
				step: props.step,
				min: props.min,
				max: props.max,
				maxLength: props.maxLength,
				value: this.state.value,
				onChange: this._handleChange,
				onBlur: props.onBlur
			});
		},
		_handleChange: function(event) {console.log('_handleChange InputControl', event);
			var value = event.target.value !== '' ? event.target.value : null;
			if(value !== null && this.props.type === 'number') {
				value = parseFloat(value); // convert js string to js number
			}
			
			this.props.onValueChange({
				value: value,
				validity: event.target.validity // constraint validation API not supported by IE9
			});
		}
	});
	
	/**
	 * Input control for BOOL type with radio buttons for different states
	 * @memberOf controls
	 */
	var BoolRadioControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'BoolRadioControl',
		propTypes: {
			nillable: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.bool,
			onValueChange: React.PropTypes.func
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
		render: function() {console.log('render BoolRadioControl', this.state, this.props);
			var states = [{value: 'true', label: 'True'}, {value: 'false', label: 'False'}];
			if(this.props.nillable) {
				states.push({value: 'null', label: 'N/A'});
			}

			var self = this;
			var inputs = $.map(states, function(state, i) {
				return (
					label({className: 'radio-inline', key: 'r' + i},
						input({type: 'radio', name: 'bool-radio', checked: state.value === self.state.value, disabled: self.props.disabled, value: state.value, onChange: self._handleChange}), state.label
					)
				);	
			});
			return div({}, inputs);
		},
		_handleChange: function(event) {console.log('_handleChange BoolRadioControl', event);
			var value = this._inputToValue(event.target.value);
			this.setState({value: value});
			this.props.onValueChange({value: value});
		},
		_valueToInputValue: function(value) {
			if(value === 'true' || value === true)
				return 'true';
			else if(value === 'false' || value === false)
				return 'false';
			else if(value === 'null' || value === null)
				return 'null';
			else
				return undefined;
		},
		_inputToValue: function(value) {
			return value === 'true' ? true : (value === 'false' ? false : (value === 'null' ? null : undefined));
		}
	});

	/**
	 * Input control for BOOL type with radio buttons for different states
	 * @memberOf controls
	 */
	var BoolCheckboxControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'BoolCheckboxControl',
		propTypes: {
			label: React.PropTypes.string,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.bool,
			onValueChange: React.PropTypes.func
		},
		render: function() {console.log('render BoolCheckboxControl', this.state, this.props);
			var lbl = this.props.label || '\u00A0'; // workaround for https://github.com/facebook/react/issues/183
		    return (
		    	div({className: 'checkbox'},
					label({},
						input({type: 'checkbox', disabled: this.props.disabled, checked: this.props.value === true, onChange: this._handleChange}), lbl
					)
		    	)
		    );
		},
		_handleChange: function(event) {console.log('_handleChange BoolCheckboxControl', event);
			var checked = event.target.checked;
			this.setState({value: checked});
			if(this.props.onValueChange) {
				this.props.onValueChange({value: checked});
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
			nillable: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			value: React.PropTypes.bool,
			onValueChange: React.PropTypes.func
		},
		render: function() {console.log('render BoolControl', this.state, this.props);
			if(this.props.nillable || this.props.layout === 'radio') {
				return React.createElement(BoolRadioControl, {nillable: this.props.nillable, disabled: this.props.disabled, value: this.props.value, onValueChange: this.props.onValueChange}); // FIXME check if works
			} else {
				return React.createElement(BoolCheckboxControl, {nillable: this.props.nillable, disabled: this.props.disabled, value: this.props.value, onValueChange: this.props.onValueChange}); // FIXME check if works
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
			return DateTimePicker({time: this.props.time, placeholder: this.props.placeholder, required: this.props.required, disabled: this.props.disabled, value: this.state.value, onChange: this._handleChange});
		},
		_handleChange: function(value) {console.log('_handleChange DateControl', value);
			this.setState(value);
			this.props.onValueChange(value);
		}
	});

	/**
	 * Input control for ENUM types
	 * @memberOf controls
	 */
	var EnumControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'EnumControl',
		propTypes: {
			options: React.PropTypes.arrayOf(React.PropTypes.string).isRequired,
			disabled: React.PropTypes.bool,
			multiple: React.PropTypes.bool,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			value: React.PropTypes.arrayOf(React.PropTypes.string),
			onValueChange: React.PropTypes.func
		},
		render: function() {console.log('render EnumControl', this.state, this.props);
			var data = this.props.options.map(function(option) {
				return {id: option, text: option};
			});
			
			var options = {
				enable: !this.props.disabled,
				multiple: this.props.multiple,
				placeholder : this.props.placeholder || ' ', // cannot be an empty string
				initSelection : function (element, callback) {
					if(value) {
						callback(value);
					}
				},
				query: function(query) {
					query.callback({results: data});
		        }
			};

			var value;
			if(this.props.value) {
				if(this.props.multiple) {
					value = $.map(this.props.value, function(option) {
						return {id: option, text: option};
					});
				} else {
					value = {id: this.props.value, text: this.props.value};
				}
			} else {
				value = undefined;
			}

			return Select2({options: options, disabled: this.props.disabled, value: value, onChange: this._handleChange});
		},
		_handleChange: function(value) {console.log('_handleChange EnumControl', value);
			var val = value !== '' ? value : null;
			this.props.onValueChange({value: val});
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
			return Select2({options: options, disabled: this.props.disabled, value: this.props.value, onChange: this._handleChange});
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
		// FIXME add propTypes
		render: function() {console.log('render AttributeControl', this.state, this.props);
			var props = this.props;
			var attr = props.attr;
			
			switch(attr.fieldType) {
				case 'BOOL':
					var layout = props.layout || 'checkbox';
					return React.createElement(BoolControl, {label: props.label, nillable: attr.nillable, disabled: props.disabled, layout: layout, value: props.value, onValueChange: this._handleValueChange});
				case 'CATEGORICAL':
					var placeholder = props.placeholder || 'Select a Category';
					var multiple = props.multiple || false;
					return this._createEntityControl(multiple, placeholder);
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
					var placeholder = props.placeholder || 'Select an Option';
					var multiple = props.multiple || false;
					return React.createElement(EnumControl, {multiple: multiple, placeholder: placeholder, required: props.required, disabled: props.disabled, options: attr.enumOptions, value: props.value, onValueChange: this._handleValueChange});
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
			return React.createElement(InputControl, {type: 'number', id: this.props.id, placeholder: placeholder, required: this.props.required, disabled: this.props.disabled, step: step, min: min, max: max, value: this.props.value, onValueChange: this._handleValueChange, onBlur: this.props.onBlur});
		},
		_createStringControl: function(type, placeholder) {
			return React.createElement(InputControl, {type: type, id: this.props.id, placeholder: placeholder, required: this.props.required, disabled: this.props.disabled, maxlength: '255', value: this.props.value, onValueChange: this._handleValueChange, onBlur: this.props.onBlur});
		},
		_createDateControl: function(time, placeholder) {
			return React.createElement(DateControl, {id: this.props.id, placeholder: placeholder, required: this.props.required, disabled: this.props.disabled, time: time, value: this.props.value, onValueChange: this._handleValueChange});
		},
		_createTextControl: function() {
			return React.createElement(TextControl, {id: this.props.id, placeholder: this.props.placeholder, required: this.props.required, disabled: this.props.disabled, value: this.props.value, onValueChange: this._handleValueChange});
		},
		_createEntityControl: function(multiple, placeholder) {
			var props = this.props;
			return React.createElement(EntityControl, {id: this.props.id, placeholder: placeholder, nillable: props.attr.nillable, multiple: multiple, disabled: this.props.disabled, entity: props.attr.refEntity, value: props.value, onValueChange: this._handleValueChange});
		}
	});
	
	molgenis.controls = {
			BoolControl: BoolControl,
			AttributeControl: AttributeControl,
			RangeSlider: RangeSlider,
			EntityControl: EntityControl,
			EnumControl: EnumControl
	};
}($, window.top.molgenis = window.top.molgenis || {}));