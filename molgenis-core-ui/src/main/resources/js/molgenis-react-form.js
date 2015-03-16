(function($, molgenis) {
	"use strict";

	var form = React.DOM.form, div = React.DOM.div, label = React.DOM.label, button = React.DOM.button, span = React.DOM.span, h4 = React.DOM.h4, p = React.DOM.p;
	var __spread = React.__spread;
	
	var api = new molgenis.RestClient();
	
	/**
	 * @memberOf control.mixin
	 */
	var FormMixin = {
		componentDidMount: function() {//console.log('componentDidMount FormMixin');
			this._retrieveEntity(this.props.entity);
		},
		componentWillReceiveProps : function(nextProps) {
			this._retrieveEntity(nextProps.entity);
		},
		_retrieveEntity: function(entity) {
			// fetch entity meta if not exists
			if(typeof entity === 'string') {
				api.getAsync(entity).done(function(entity) {
					if (this.isMounted()) { // check that the component is still mounted
						this.setState({entity: entity});
					}
				}.bind(this));
			} else if(typeof entity === 'object') {
				this.setState({entity: entity});
			}
		}		
	};
	
	/**
	 * @memberOf control
	 */
	var ValidatedFormControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin, FormMixin],
		displayName: 'ValidatedFormControl',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			attr: React.PropTypes.object.isRequired,
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			validate: React.PropTypes.bool,
			value: React.PropTypes.any,
			onValueChange: React.PropTypes.func.isRequired
		},
		getInitialState: function() {
			return {
				attr: this.props.attr.name !== undefined ? this.props.attr : null,
				pristine: true
			};
		},
		componentWillReceiveProps: function(nextProps) {
			if(nextProps.validate === true) {
				// validate control
				this._validate(nextProps.value, function(validity) {
					this.setState({
						pristine: true,
						validity: validity
					});		
				}.bind(this));
			}
		},
		componentDidMount: function() {console.log('componentDidMount ValidatedFormControl');
			if(this.props.attr.name === undefined) {
				api.getAsync(this.props.attr.href).done(function(attr) {
					if (this.isMounted()) {
						this.setState({attr: attr});
						
						// notify parent of initial value validity
						this._handleValueChange({value: this.props.value});
					}
				}.bind(this));
			} else {
				// notify parent of initial value validity
				this._handleValueChange({value: this.props.value});
			}
		},
		render: function() {console.log('render ValidatedFormControl', this.state, this.props);
			if(this.state.attr === null) {
				// attribute not fetched yet
				return div({});
			}
			
			var attr = this.state.attr;
			
			
			// allow editing readonly controls in create mode
			if(this.props.mode === 'create' && attr.readOnly === true) {
				attr = _.extend({}, attr, {readOnly: false, required: true});
			}
			// show hidden controls and create and edit form
			if((this.props.mode === 'create' || this.props.mode === 'edit') && attr.visible !== true) {
				attr = _.extend({}, attr, {visible: true});				
			}
			// show auto controls as readonly in edit mode
			if(this.props.mode === 'edit' && attr.auto === true) {
				attr = _.extend({}, attr, {readOnly: true});
			}
			
			var lbl = attr.label;
			
			if(attr.nillable === false) {
				lbl += ' *';
			}
			
			// add validation error message
			var validate = this.state.pristine === false || this.props.validate === true;
			var errorMessageSpan = validate && this.state.valid === false ? span({className: 'help-block'}, this.state.errorMessage) : null;
			
			// determine success and error classes for control 
			var formGroupClasses = 'form-group';
			if(validate && this.state.valid === false) {
				formGroupClasses += ' has-error';
			}
						
			var id = attr.name;
			
			var description = attr.description !== undefined ? span({className: 'help-block'}, attr.description) : undefined;
			var labelClasses = this.props.formLayout === 'horizontal' ? 'col-md-2 control-label' : 'control-label';
			var labelElement = label({className: labelClasses, htmlFor: id}, lbl);
			console.log(__spread({}, this.props, {
				attr : attr,
				id : id,
				name : id,
				disabled: this.props.mode === 'view',
				formLayout : undefined,
				value: this.props.value,
				onValueChange : this._handleValueChange,
				onBlur : this._handleBlur
			}));
			var control = molgenis.control.AttributeControl(__spread({}, this.props, {
				attr : attr,
				id : id,
				name : id,
				disabled: this.props.mode === 'view',
				formLayout : undefined,
				value: this.props.value,
				onValueChange : this._handleValueChange,
				onBlur : this._handleBlur
			}));
			
			if(this.props.formLayout === 'horizontal') {
				return(
					div({className: formGroupClasses},
						labelElement,
						div({className: 'col-md-10'},
							description,
							control,
							errorMessageSpan
						)
					)
				);
			} else {
				return(
					div({className: formGroupClasses},
						labelElement,
						description,
						control,
						errorMessageSpan
					)
				);
			}
		},
		_handleValueChange: function(e) {
			this._validate(e.value, function(validity) {
				this.setState({
					value: e.value,
					valid: validity.valid,
					errorMessage: validity.errorMessage,
					pristine: this.props.value === e.value // mark input as dirty
				});
				
				this.props.onValueChange({
					attr: this.state.attr.name,
					value: e.value,
					valid: validity.valid,
					errorMessage: validity.errorMessage
				});
			}.bind(this));
		},
		_handleBlur: function(e) {
			// only validate if control was touched
			if(this.state.pristine === true) {
				return;
			}
			
			this._validate(e.value, function(validity) {
				this.setState({
					valid: validity.valid,
					errorMessage: validity.errorMessage
				});
			}.bind(this));
		},
		_validate: function(value, callback) {
			// apply validation rules, not that IE9 does not support constraint validation API 
			var attr = this.state.attr;
			var type = attr.fieldType;
			var nullOrUndefinedValue = value === null || value === undefined;
			
			var errorMessage = undefined;
			
			if(attr.nillable === false && nullOrUndefinedValue) { // required value constraint
				errorMessage = 'Please enter a value.';
			}
			else if(type === 'EMAIL' && !nullOrUndefinedValue && !this._statics.REGEX_EMAIL.test(value)) {
				errorMessage = 'Please enter a valid email address.';
			}
			else if(type === 'HYPERLINK' && !nullOrUndefinedValue && !this._statics.REGEX_URL.test(value)) {
				errorMessage = 'Please enter a valid URL.';
			}
			else if(type === 'INT' && !nullOrUndefinedValue && !this._isInt(value)) {
				errorMessage = 'Please enter a value between ' + this._statics.INT_MIN + ' and ' + this._statics.INT_MAX + '.';
			}
			else if(type === 'LONG' && !nullOrUndefinedValue && !this._isLong(value)) {
				errorMessage = 'Please enter a value between ' + this._statics.LONG_MIN + ' and ' + this._statics.LONG_MAX + '.';
			}
			else if((type === 'INT' || type === 'LONG') && attr.range && !nullOrUndefinedValue && !this._inRange(value, attr.range)) {
				if(attr.range.min !== undefined && attr.range.max !== undefined) {
					errorMessage = 'Please enter a value between ' + attr.range.min + ' and ' + attr.range.max + '.';
				}
				else if(attr.range.min !== undefined) {
					errorMessage = 'Please enter a value greater than or equal to ' + attr.range.min + '.';
				}
				else if(attr.range.max !== undefined) {
					errorMessage = 'Please enter a value lower than or equal to ' + attr.range.max + '.';
				}
			}
			else if(attr.unique === true && (this.props.mode !== 'edit' || value !== this.props.value)) { // value uniqueness constraint
				// FIXME temp hack because validation of ref types does not work yet
				if(type !== 'XREF' && type !== 'CATEGORICAL' && type !== 'MREF' && type !== 'CATEGORICAL_MREF')
				{
					var rules = [{field: attr.name, operator: 'EQUALS', value: value}];
					
					api.getAsync(this.props.entity.hrefCollection, {q: {q: rules}}, function(data) {
						if(data.total > 0) {
							callback({valid: false, errorMessage: 'This ' + attr.label + ' already exists. It must be unique.'});
						} else {
							callback({valid: true, errorMessage: undefined});
						}
					});
					return;
				}
			}
			callback({valid: errorMessage === undefined, errorMessage: errorMessage});
		},
		_statics: {
			// https://gist.github.com/dperini/729294
			REGEX_URL: /^(?:(?:https?|ftp):\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:\/\S*)?$/i,
			// http://www.w3.org/TR/html5/forms.html#valid-e-mail-address
			REGEX_EMAIL: /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/,
			INT_MIN: -2147483648, 
			INT_MAX: 2147483647,
			LONG_MIN: Number.MIN_SAFE_INTEGER,
			LONG_MAX: Number.MAX_SAFE_INTEGER
		},
		_isInt: function(value) {
			return Number.isInteger(value) && value >= this._statics.INT_MIN && value <= this._statics.INT_MAX;
		},
		_isLong: function(value) {
			return Number.isInteger(value) && value >= this._statics.LONG_MIN && value <= this._statics.LONG_MAX; 
		},
		_inRange: function(value, range) {
			var inRange = true;
			if(range.min !== undefined) {
				inRange = inRange && value >= range.min;
			}
			if(range.max !== undefined) {
				inRange = inRange && value <= range.max;
			}
			return inRange;
		}
	});
	
	/**
	 * @memberOf control
	 */
	var FormControlGroup = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'FormControlGroup',
		propTypes: {
			entity: React.PropTypes.object,
			attr: React.PropTypes.object.isRequired,
			value: React.PropTypes.object,
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			validate: React.PropTypes.bool,
			onValueChange: React.PropTypes.func.isRequired
		},
		render: function() {
			var attributes = this.props.attr.attributes;
			
			// add control for each attribute
			var controls = [];
			for(var i = 0; i < attributes.length; ++i) {console.log('attribute', attributes[i]);
				var Control = attributes[i].fieldType === 'COMPOUND' ? molgenis.control.FormControlGroup : molgenis.control.ValidatedFormControl;
				controls.push(Control({
					entity : this.props.entity,
					attr : attributes[i],
					value: this.props.value ? this.props.value[attributes[i].name] : undefined,
					mode : this.props.mode,
					formLayout : this.props.formLayout,
					validate: this.props.validate,
					onValueChange : this.props.onValueChange,
					key : '' + i
				}));
				controls.push(control);
			}
			
			return (
				div({},
					h4({className: 'page-header'}, this.props.attr.label),
					p({}, this.props.attr.description),
					div({className: 'row'},
						div({className: 'col-md-offset-1 col-md-11'},
							controls
						)
					)
				)
			);
		}
	});
	
	/**
	 * @memberOf control
	 */
	var FormControls = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'FormControls',
		propTypes: {
			entity: React.PropTypes.object.isRequired,
			value: React.PropTypes.object,
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			colOffset: React.PropTypes.number,
			validate: React.PropTypes.bool,
			onValueChange: React.PropTypes.func.isRequired
		},
		render: function() {
			// add control for each attribute
			var attributes = this.props.entity.attributes;
			var controls = [];
			for(var key in attributes) {
				if(attributes.hasOwnProperty(key)) {
					var attr = attributes[key];
					if(this.props.mode !== 'create' && attr.auto !== true) {
						var Control = attr.fieldType === 'COMPOUND' ? molgenis.control.FormControlGroup : molgenis.control.ValidatedFormControl;
						controls.push(Control({
							entity : this.props.entity,
							attr : attr,
							value: this.props.value ? this.props.value[key] : undefined,
							mode : this.props.mode,
							formLayout : this.props.formLayout,
							validate: this.props.validate,
							onValueChange : this.props.onValueChange,
							key : key
						}));
					}
				}
			}
			return div({}, controls);
		}
	});
	
	/**
	 * @memberOf control
	 */
	var FormSubmitButton = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'FormSubmitButton',
		propTypes: {
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			colOffset: React.PropTypes.number
		},
		render: function() {
			var saveControl = button({type: 'submit', className: 'btn btn-large btn-primary pull-right'}, 'Save');
			if(this.props.formLayout === 'horizontal') {
				var divClasses = 'col-md-offset-' + this.props.colOffset + ' col-md-' + (12 - this.props.colOffset);  
				saveControl = (
					div({className: 'form-group'},
						div({className: divClasses},
							saveControl
						)
					)
				);
			}
			return saveControl;
		}
	});
	
	/**
	 * @memberOf control
	 */
	var Form = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin, FormMixin],
		displayName: 'Form',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			value: React.PropTypes.object,
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			colOffset: React.PropTypes.number
		},
		getDefaultProps: function() {
			return {
				mode: 'create',
				formLayout: 'vertical',
				colOffset: 2
			};
		},
		getInitialState: function() {
			return {entity: null, values: {}, validate: false};
		},
		render: function() {console.log('render Form', this.state, this.props);
			// return empty div if entity data is not yet available
			if(this.state.entity === null) {
				return div();
			}
			
			var action, method;
			switch(this.props.mode) {
				case 'create':
					action = this.state.entity.hrefCollection;
					method = 'post';
					break;
				case 'edit':
					action = this.props.value.href + '?_method=PUT';
					method = 'post';
					break;
				case 'view':
					action = undefined;
					method = undefined;
					break;
				default:
					throw 'unknown mode [' + this.props.mode + ']';
			}
			
			var formProps = {
				className : this.props.formLayout === 'horizontal' ? 'form-horizontal' : undefined,
				action : action,
				method : method,
				encType : 'application/x-www-form-urlencoded', // use multipart/form-data if form contains one or more file inputs
				noValidate : true,
				onSubmit : this._handleSubmit
			};
			
			var formControlsProps = {
				entity : this.state.entity,
				value: this.props.mode !== 'create' ? this.props.value : undefined,
				mode : this.props.mode,
				formLayout : this.props.formLayout,
				colOffset : this.props.colOffset,
				validate: this.state.validate,
				onValueChange : this._handleValueChange
			};
			
			return (
				form(formProps,
					molgenis.control.FormControls(formControlsProps),
					this.props.mode !== 'view' ? molgenis.control.FormSubmitButton({layout: this.props.formLayout, colOffset: this.props.colOffset}) : null
				)
			);
		},
		_handleValueChange: function(e) {console.log('Form._handleValueChange', e);
			this.state.values[e.attr] = {value: e.value, valid: e.valid};
			this.setState({values: this.state.values, valid: this.state.valid & e.valid});
		},
		_handleSubmit: function(e) {			
			var values = this.state.values;
			
			// determine if form is valid
			var formValid = true;
			for(var key in values) {
				if(values.hasOwnProperty(key)) {
					var value = values[key];
					if(value.valid === false) {
						formValid = false;
						break;
					}
				}
			}
			
			if(formValid) {
				// create updated entity
				var updatedEntity = {};
				for(var key in values) {
					if(values.hasOwnProperty(key)) {
						updatedEntity[key] = values[key].value;
					}
				}
				
				
			} else {
				e.preventDefault(); // do not submit form
				this.setState({validate: true}); // render validated controls
			}
		}
	});

	// export module
	molgenis.control = molgenis.control || {};
	
	$.extend(molgenis.control, {
		Form: React.createFactory(Form),
		FormControls: React.createFactory(FormControls),
		ValidatedFormControl: React.createFactory(ValidatedFormControl),
		FormControlGroup: React.createFactory(FormControlGroup),
		FormSubmitButton: React.createFactory(FormSubmitButton),
	});
}($, window.top.molgenis = window.top.molgenis || {}));