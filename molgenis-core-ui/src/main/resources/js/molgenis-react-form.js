(function($, molgenis) {
	"use strict";

	var form = React.DOM.form, div = React.DOM.div, label = React.DOM.label, button = React.DOM.button, span = React.DOM.span, h4 = React.DOM.h4, p = React.DOM.p;
	var __spread = React.__spread;
	
	var api = new molgenis.RestClient();
	
	/**
	 * @memberOf control
	 */
	var AttributeFormControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'AttributeFormControl',
		propTypes: { // FIXME add all props
			entity: React.PropTypes.object.isRequired, // TODO support entity with only href
			attr: React.PropTypes.object.isRequired,
			formLayout: React.PropTypes.string,
			mode: React.PropTypes.oneOf(['create', 'edit']),
			onValueChange: React.PropTypes.func.isRequired
		},
		getDefaultProps: function() {
			return {
				// https://gist.github.com/dperini/729294
				regexUrl: /^(?:(?:https?|ftp):\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,})))(?::\d{2,5})?(?:\/\S*)?$/i,
				// http://www.w3.org/TR/html5/forms.html#valid-e-mail-address
				regexEmail: /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/,
				intMin: -2147483648, 
				intMax: 2147483647,
				longMin: Number.MIN_SAFE_INTEGER,
				longMax: Number.MAX_SAFE_INTEGER
			};
		},
		getInitialState: function() {
			return {
				attr: this.props.attr.name !== undefined ? this.props.attr : null,
				value: this.props.value || this.props.attr.defaultValue,
				pristine: true
			};
		},
		componentWillReceiveProps: function(nextProps) {
			if(nextProps.validate === true) {
				// validate control
				var self = this;
				this._validate(nextProps.value, function(validity) {
					self.setState({
						validity: validity 
					});		
				});
			}
		},
		componentDidMount: function() {console.log('componentDidMount AttributeFormControl');
			if(this.props.attr.name === undefined) {
				var self = this;
				api.getAsync(this.props.attr.href).done(function(attr) {
					if (self.isMounted()) {
						self.setState({attr: attr});
						
						// notify parent of initial value validity
						self._handleValueChange({value: self.state.value});
					}
				});
			} else {
				// notify parent of initial value validity
				this._handleValueChange({value: this.state.value});
			}
		},
		render: function() {console.log('render AttributeFormControl', this.state, this.props);
			if(this.state.attr === null) {
				// attribute not fetched yet
				return div({});
			}
			
			var attr = this.state.attr;
			
			// allow editing readonly controls in create mode
			if(this.props.mode === 'create' && attr.readOnly === true) {
				attr = _.extend({}, attr, {readOnly: false});
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
			var labelClasses = this.props.formLayout === 'horizontal' ? 'col-sm-2 control-label' : 'control-label';
			var labelElement = label({className: labelClasses, htmlFor: id}, lbl);
			var control = molgenis.control.AttributeControl(__spread({}, this.props, {attr: attr, id: id, name: id, formLayout: undefined, onValueChange: this._handleValueChange, onBlur: this._handleBlur}));
			
			if(this.props.formLayout === 'horizontal') {
				return(
					div({className: formGroupClasses},
						labelElement,
						div({className: 'col-sm-10'},
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
			var self = this; // FIXME use bind(this)
			this._validate(e.value, function(validity) {
				self.setState({
					value: e.value,
					valid: validity.valid,
					errorMessage: validity.errorMessage,
					pristine: self.state.value === e.value // mark input as dirty
				});
				
				self.props.onValueChange({
					attr: self.state.attr.name,
					value: e.value,
					valid: validity.valid,
					errorMessage: validity.errorMessage
				});
			});
		},
		_handleBlur: function(e) {
			// only validate if control was touched
			if(this.state.pristine === true) {
				return;
			}
			
			var self = this;
			this._validate(this.state.value, function(validity) {
				self.setState({
					valid: validity.valid,
					errorMessage: validity.errorMessage
				});
			});
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
			else if(type === 'EMAIL' && !nullOrUndefinedValue && !this.props.regexEmail.test(value)) {
				errorMessage = 'Please enter a valid email address.';
			}
			else if(type === 'HYPERLINK' && !nullOrUndefinedValue && !this.props.regexUrl.test(value)) {
				errorMessage = 'Please enter a valid URL.';
			}
			else if(type === 'INT' && !nullOrUndefinedValue && !this._isInt(value)) {
				errorMessage = 'Please enter a value between ' + this.props.intMin + ' and ' + this.props.intMax + '.';
			}
			else if(type === 'LONG' && !nullOrUndefinedValue && !this._isLong(value)) {
				errorMessage = 'Please enter a value between ' + this.props.longMin + ' and ' + this.props.longMax + '.';
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
			else if(attr.unique === true) { // value uniqueness constraint
				// FIXME temp hack because validation of ref types does not work yet
				if(type !== 'XREF' && type !== 'CATEGORICAL' && type !== 'MREF' && type !== 'CATEGORICAL_MREF')
				{
					api.getAsync(this.props.entity.hrefCollection, {q: {q: [{field: attr.name, operator: 'EQUALS', value: value}]}}, function(data) {
						if(data.total > 0) {
							callback({valid: false, errorMessage: 'This ' + attr.label + ' already exists. It must be unique.'});
						}	
					});
				}
			}
			callback({valid: errorMessage === undefined, errorMessage: errorMessage});
		},
		_isInt: function(value) {
			return Number.isInteger(value) && value >= -2147483648 && value <= 2147483647;
		},
		_isLong: function(value) {
			return Number.isInteger(value) && value >= Number.MAX_SAFE_INTEGER && value <= Number.MIN_SAFE_INTEGER; 
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
	var AttributeFormControlGroup = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'AttributeFormControlGroup',
		propTypes: {
			attributes: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
			onValueChange: React.PropTypes.func.isRequired // TODO add all props
		},
		render: function() {
			var attributes = this.props.attr.attributes;
			
			// add control for each attribute
			var controls = [];
			for(var i = 0; i < attributes.length; ++i) {console.log('attribute', attributes[i]);
				var control;
				if(attributes[i].fieldType !== 'COMPOUND') { // FIXME attribute might be a href, so fieldtype does not exist
					control = molgenis.control.AttributeFormControl({attr: attributes[i], key: 'attr' + i, onValueChange: this.props.onValueChange, validate: this.props.validate, formLayout: this.props.formLayout});
				} else {
					control = molgenis.control.AttributeFormControlGroup({attr: attributes[i], key: 'attr' + i, onValueChange: this.props.onValueChange, validate: this.props.validate, formLayout: this.props.formLayout});
				}
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
	var Form = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'Form',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			formLayout: React.PropTypes.string,
			groupLayout: React.PropTypes.string,
			mode: React.PropTypes.oneOf(['create', 'edit'])
		},
		getDefaultProps: function() {
			return {
				mode: 'create'
			};
		},
		getInitialState: function() {
			return {entity: null, values: {}, validate: false}; // TODO initialize with value
		},
		componentDidMount: function() {console.log('componentDidMount Form');
			this._retrieveEntity(this.props.entity);
		},
		componentWillReceiveProps : function(nextProps) {
			this._retrieveEntity(nextProps.entity);
		},
		render: function() {console.log('render Form', this.state, this.props);
			// return empty div if entity data is not yet available
			if(this.state.entity === null) {
				return div();
			}

			// add control for each attribute
			var attributes = this.state.entity.attributes;
			var controls = [];
			for(var key in attributes) {
				var control;
				if(attributes[key].fieldType === 'COMPOUND') { // FIXME remove if, find other solution
					control = molgenis.control.AttributeFormControlGroup({entity: this.state.entity, attr: attributes[key], key: key, onValueChange: this._handleValueChange, mode: this.props.mode, formLayout: this.props.formLayout});
				} else {
					control = molgenis.control.AttributeFormControl({entity: this.state.entity, attr: attributes[key], key: key, onValueChange: this._handleValueChange, validate: this.state.validate, mode: this.props.mode, formLayout: this.props.formLayout, ref: key});
				}
				controls.push(control);
			}
			
			// add form buttons
			var saveControl = button({type: 'submit', className: 'btn btn-large btn-primary pull-right'}, 'Save');
			if(this.props.formLayout === 'horizontal') {
				saveControl = (
					div({className: 'form-group'},
						div({className: 'col-sm-offset-2 col-sm-10'},
							saveControl
						)
					)
				);
			}

			var formProps = {
				className : this.props.formLayout === 'horizontal' ? 'form-horizontal' : undefined,
				action : this.state.entity.hrefCollection,
				method : 'post',
				encType : 'application/x-www-form-urlencoded', // use multipart/form-data if form contains one or more file inputs
				noValidate : true,
				onSubmit : this._handleSubmit
			};
			
			return (
				form(formProps,
					controls,
					saveControl
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
		},
		_retrieveEntity: function(entity) {
			// fetch entity meta if not exists
			if(typeof entity === 'string') {
				var self = this;
				api.getAsync(entity).done(function(entity) {
					if (self.isMounted()) { // check that the component is still mounted
						self.setState({entity: entity});
					}
				});
			} else if(typeof entity === 'object') {
				this.setState({entity: entity});
			}
		}
	});

	// export module
	molgenis.control = molgenis.control || {};
	
	$.extend(molgenis.control, {
		AttributeFormControl: React.createFactory(AttributeFormControl),
		AttributeFormControlGroup: React.createFactory(AttributeFormControlGroup),
		Form: React.createFactory(Form),
	});
}($, window.top.molgenis = window.top.molgenis || {}));