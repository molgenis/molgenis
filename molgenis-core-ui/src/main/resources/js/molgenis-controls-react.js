/** @jsx React.DOM */
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
	
	/**
	 * React component for Select2
	 */
	var Select2Component = React.createClass({
		componentDidMount: function() {console.log('componentDidMount Select2Component');
			var $container = $(this.refs.select2.getDOMNode());
			var options = $.extend({containerCssClass: 'form-control'}, this.props.options);
			$container.select2(options).select2('val', []);
			
			var props = this.props;
			$container.on('change', function() {
				props.onChange($container.select2('val'));
			});
		},
		componentWillUnmount: function() {console.log('componentWillUnmount Select2Component');
			var $container = $(this.refs.select2.getDOMNode());
			$container.off();
			$container.select2('destroy');
		},
		render: function() {console.log('render Select2Component', this.state, this.props);
			if (this.isMounted()) {
				var $container = $(this.refs.select2.getDOMNode());
				$container.select2('val', this.props.value);
				$container.select2('enable', !this.props.disabled);
			}
			return(<input type="hidden" ref="select2" />);
		}
	});
	
	/**
	 * React component for jQRangeSlider
	 */
	var JQRangeSliderComponent = React.createClass({
		componentDidMount: function() {
			var $container = $(this.refs.rangeslider.getDOMNode());
			$container.editRangeSlider(this.props.options);
			$container.on('userValuesChanged', function(e, data) {
				this.props.onChange({from: data.values.min, to: data.values.max});
			});
		},
		componentWillUnmount: function() {
			var $container = $(this.refs.rangeslider.getDOMNode());
			$container.off();
			$container.editRangeSlider('destroy');
		},
		render: function() {console.log('render JQRangeSliderComponent', this.state, this.props);
			if(this.isMounted()) {
				var $container = $(this.refs.rangeslider.getDOMNode());
				$container.editRangeSlider(this.props.disabled ? 'disable' : 'enable');
			}
			return(
				<div ref="rangeslider"></div>		
			);
		}
	});
	
	/**
	 * Range slider control for number types 
	 */
	var RangeSliderControl = React.createClass({
		render: function() {
			var range = this.props.range;
			var value = this.props.value;
			
			var fromValue = this.props.value && value.from ? value.from : range.min;
			var toValue = this.props.value && value.to ? value.to : range.max;
			var options = {
				symmetricPositionning: true,
				bounds: {min: range.min, max: range.max},
				defaultValues: {min: fromValue, max: toValue},
				step: this.props.step,
				type: 'number'
			}
			return(
				<JQRangeSliderComponent options={options} disabled={this.props.disabled} onChange={this._handleChange} />
			);
		},
		_handleChange: function(event) {console.log('_handleChange RangeSliderControl', event);
			this.props.onValueChange({value: event.value});
		}
	});
	
	/**
	 * Input control for string and number types
	 */
	var InputControl = React.createClass({
		getDefaultProps: function() {
			return {
				disabled: false,
				required: true
			};
		},
		getInitialState: function() {
			return {
				value: this.props.value
			};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				value: nextProps.value
			});
		},
		render: function() {
			var type = this.props.type;
			var placeholder = this.props.placeholder;
			var required = this.props.required;
			var disabled = this.props.disabled;
			var step = this.props.step;
			var min = this.props.min;
			var max = this.props.max;
			var maxlength = this.props.maxlength;
			
			var value = this.state.value;
		    return (
	    		<input type={type} className="form-control" value={this.state.value}
	    			placeholder={placeholder} required={required} disabled={disabled} step={step} min={min} max={max}
	    			onChange={this._handleChange} />
		    );
		},
		_handleChange: function(event) {console.log('_handleChange InputControl', event);
			var value = event.target.value;
			this.setState({value: value});
			if(this.props.onValueChange) {
				this.props.onValueChange({value: value});
			}
		}
	});
	
	/**
	 * Input control for BOOL type with checkbox or radio buttons
	 */
	var BoolControl = React.createClass({
		render: function() {console.log('render BoolControl', this.state, this.props);
			if(this.props.nillable || this.props.layout === 'radio') {
				return <BoolRadioControl {...this.props} />
			} else {
				return <BoolCheckboxControl {...this.props} />
			}
		}
	});
	
	/**
	 * Input control for BOOL type with radio buttons for different states
	 */
	var BoolRadioControl = React.createClass({
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
			var inputs = $.map(states, function(state) {
				return (
					<label className="radio-inline">
						<input type="radio" name="bool-radio" checked={state.value === self.state.value} disabled={self.props.disabled} value={state.value} onChange={self._handleChange} /> {state.label}
					</label>
				);	
			});
			return (<div>{inputs}</div>);
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
	 */
	var BoolCheckboxControl = React.createClass({
		render: function() {console.log('render BoolCheckboxControl', this.state, this.props);
			var label = this.props.label || '\u00A0'; // workaround for https://github.com/facebook/react/issues/183
		    return (
		    		<div className="checkbox">
		    			<label>
		    				<input type="checkbox" disabled={this.props.disabled} checked={this.props.value === true} onChange={this._handleChange} /> {label}   
	    				</label>
		    		</div>
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
	 * Input control for DATE and DATE_TIME types
	 */
	var DateControl = React.createClass({
		componentDidMount: function() {
			var $container = $(this.refs.datepicker.getDOMNode());
			var datetimepickerSettings = this.props.time ? { pickTime : true, useSeconds : true } : { pickTime : false };
			$container.datetimepicker(datetimepickerSettings);
			
			var self = this;
			var $input = $(this.refs.input.getDOMNode());
			$container.on('change', function(event) {
				self._handleChange($input.val());
			});
		},
		componentWillUnmount: function() {
			var $container = $(this.refs.datepicker.getDOMNode());
			$container.datetimepicker('destroy');
		},
		render: function() {console.log('render DateControl', this.state, this.props);
			var dateFormat = this.props.time ? 'YYYY-MM-DD' : 'YYYY-MM-DDTHH:mm:ssZZ';
			var placeholder = this.props.placeholder;
			var required = this.props.required;
			var disabled = this.props.disabled;
			return(
				<div className="input-group date group-append" ref="datepicker">
					<input type="text" className="form-control" data-date-format={dateFormat} value={this.props.value}
		    			placeholder={placeholder} required={required} disabled={disabled} ref="input" />
					<span className="input-group-addon datepickerbutton">
						<span className="glyphicon glyp2icon-calendar"></span>
					</span>
				</div>
			);
		},
		_handleChange: function(value) {console.log('_handleChange DateControl', value);
			this.props.onValueChange({value: value});
		}
	});

	var EnumControl = React.createClass({
		render: function() {console.log('render EnumControl', this.state, this.props);
			var data = this.props.options.map(function(value) {
				return {id: value, text: value};
			});
			var value = this.props.value;
			
			var options = {
				enable: !this.props.disabled,
				containerCssClass: 'form-control',
				placeholder : this.props.placeholder || ' ', // cannot be an empty string
				minimumResultsForSearch: -1, // permanently hide the search field
				initSelection : function (element, callback) {
					if(value) {
						callback({id: value, text: value});
					}
				},
				query: function(query) {
					query.callback({results: data});
		        },
		        width: '100%'
			};
			return (
				<Select2Component options={options} disabled={this.props.disabled} value={this.props.value} onChange={this._handleChange} />
			);
		},
		_handleChange: function(value) {console.log('_handleChange EnumControl', value);
			var value = value !== '' ? value : null;
			this.props.onValueChange({value: value});
		}
	});
	
	var CategoricalControl = React.createClass({
		getInitialState: function() {
			// initialize with entity meta if exists
			var entity = this.props.entity;
			return {
				entity: entity.idAttribute !== undefined ? entity : null
			};
		},
		componentDidMount: function() {
			// fetch entity meta if not exists
			var entity = this.props.entity;
			if(entity.idAttribute === undefined) {
				api.getAsync(entity.href).done(function(entity) {
					if (this.isMounted()) {
						this.setState({entity: entity});
					}
				}.bind(this));	
			}
		},
		render: function() {console.log('render CategoricalControl', this.state, this.props);
			if(this.state.entity === null) {
				// entity meta data not fetched yet
				return (<div></div>);
			}
			
			var props = this.props;
			var entity = this.state.entity;
			
			var format = function(item) {
				if (item)
					return item[entity.labelAttribute];
			};
			
			var options = {
				enable: !this.props.disabled,
				containerCssClass: 'form-control',
				id: entity.idAttribute,
				multiple: props.multiple,
				allowClear : props.nillable ? true : false,
				placeholder : props.placeholder || ' ', // cannot be an empty string
				initSelection: function(element, callback) {
					if(self.value)
						callback(self.value);
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
			    formatSelection: format,
			    minimumResultsForSearch: -1, // permanently hide the search field
			    width: '100%'
			};
			return (
				<Select2Component options={options} disabled={this.props.disabled} value={this.props.value} onChange={this._handleChange} />
			);
		},
		_handleChange: function(value) {console.log('_handleChange CategoricalControl', value);
			var value = this.props.multiple && value.length === 0 ? undefined : value;
			this.props.onValueChange({value: value});
		}
	});
	
	var TextControl = React.createClass({
		render: function() {console.log('render TextControl', this.state, this.props);
			return (<textarea className="form-control" placeholder={this.props.placeholder} required={this.props.required} disabled={this.props.disabled} onChange={this._handleChange}>{this.props.value}</textarea>);
		},
		_handleChange: function(value) {console.log('_handleChange TextControl', value);
			var value = value !== '' ? value : null;
			this.props.onValueChange({value: value});
		}
	});
		
	var AttributeControl = React.createClass({
		render: function() {console.log('render AttributeControl', this.state, this.props);
			var props = this.props;
			var attr = props.attr;
			
			switch(attr.fieldType) {
				case 'BOOL':
					var layout = this.props.layout || 'checkbox';
					return <BoolControl label={props.label} nillable={attr.nillable} disabled={props.disabled} layout={layout} value={props.value} onValueChange={this._handleValueChange} />
				case 'CATEGORICAL':
					return this._createEntityControl(this.props.multiple === true);
				case 'DATE':
					return this._createDateControl(false);
				case 'DATE_TIME':
					return this._createDateControl(true);
				case 'DECIMAL':
					return this._createNumberControl('any');
				case 'EMAIL':
					return this._createStringControl('email');
				case 'ENUM':
					return <EnumControl value={props.value} options={attr.enumOptions} onValueChange={this._handleValueChange} />
				case 'HTML':
					return this._createTextControl();
				case 'HYPERLINK':
					return this._createStringControl('url');
				case 'INT':
				case 'LONG':
					return this._createNumberControl('1');
				case 'XREF':
					return this._createEntityControl(false);
				case 'MREF':
					return this._createEntityControl(true);
				case 'SCRIPT':
					return this._createTextControl();
				case 'STRING':
					return this._createNumberControl('text');
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
			this.props.onValueChange({attr: this.props.attr.name, value: event.value});
		},
		_createNumberControl: function(step) {
			var min = this.props.range ? this.props.range.min : undefined;
			var max = this.props.range ? this.props.range.max : undefined;
			return <InputControl type="number" placeholder={this.props.placeholder} required={this.props.required} disabled={this.props.disabled} step={step} min={min} max={max} value={this.props.value} onValueChange={this._handleValueChange} />
		},
		_createStringControl: function(type) {
			return <InputControl type={type} placeholder={this.props.placeholder} required={this.props.required} disabled={this.props.disabled} maxlength="255" value={this.props.value} onValueChange={this._handleValueChange} />
		},
		_createDateControl: function(time) {
			return <DateControl placeholder={this.props.placeholder} required={this.props.required} disabled={this.props.disabled} time={time} value={this.props.value} onValueChange={this._handleValueChange} />
		},
		_createTextControl: function() {
			return <TextControl placeholder={this.props.placeholder} required={this.props.required} disabled={this.props.disabled} value={this.props.value} onValueChange={this._handleValueChange} />
		},
		_createEntityControl: function(multiple) {
			var props = this.props;
			return <CategoricalControl entity={props.attr.refEntity} nillable={props.attr.nillable} multiple={multiple} disabled={this.props.disabled} value={props.value} onValueChange={this._handleValueChange} />
		}
	});
	
	molgenis.controls = {
			BoolControl: BoolControl,
			AttributeControl: AttributeControl,
			RangeSliderControl: RangeSliderControl 
	}
}($, window.top.molgenis = window.top.molgenis || {}));