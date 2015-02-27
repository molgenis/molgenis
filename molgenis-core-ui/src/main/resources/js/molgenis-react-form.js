(function($, molgenis) {
	"use strict";

	var form = React.DOM.form, div = React.DOM.div, label = React.DOM.label, button = React.DOM.button, span = React.DOM.span, h4 = React.DOM.h4;
	var __spread = React.__spread;
	
	var api = new molgenis.RestClient();
	
	var AttributeFormControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'AttributeFormControl',
		propTypes: {
			layout: React.PropTypes.string,
			onValueChange: React.PropTypes.func.isRequired
		},
		getInitialState: function() {
			return {
				attr: this.props.attr.name !== undefined ? this.props.attr : null,
				value: this.props.value,
				pristine: true,
				validity: this.props.validate ? this._validate(this.props.value).valid : undefined
			};
		},
		componentWillReceiveProps: function(nextProps) {
			if(this.state.validity === undefined && nextProps.validate === true) {
				this.setState({
					validity: this._validate(nextProps.value)
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
						self.props.onValueChange({
							attr: attr.name,
							value: self.state.value,
							valid: self._validate(self.state.value).valid
						});
					}
				});
			}
//			if(this.props.attr.description !== undefined) {
//				$(this.refs.popover.getDOMNode()).popover(); // Bootstrap requires you to initialize popovers yourself
//			}
			
//			// notify parent of initial value validity
//			this.props.onValueChange({
//				attr: this.props.attr.name,
//				value: this.state.value,
//				valid: this._validate(this.state.value).valid
//			});
		},
		componentWillUnmount: function() {console.log('componentWillUnmount AttributeFormControl');
//			if(this.props.attr.description !== undefined) {
//				$(this.refs.popover.getDOMNode()).popover('destroy'); // cleanup
//			}
		},
		render: function() {console.log('render AttributeFormControl', this.state, this.props);
			if(this.state.attr === null) {
				// attribute not fetched yet
				return div({});
			}
			
			var attr = this.state.attr;
			var lbl = attr.label;
			
			// add info control for attribute description
//			var lblInfo;
//			if(attr.description !== undefined) {
//				lblInfo = (
//					a({href: '#', 'data-toggle': 'popover', 'data-content': attr.description, 'data-trigger': 'focus', ref: 'popover'},
//						span({className: 'glyphicon glyphicon-info-sign'})
//					)
//				);
//				lbl = ' ' + lbl; // put some space between icon and label text
//			} else {
//				lblInfo = undefined;
//			}
			
			if(attr.nillable === false) {
				lbl += ' *';
			}
			
			// add validation error message
			var validity = this.state.validity;
			var errorMessageSpan = validity && validity.valid === false ? span({className: 'help-block'}, validity.errorMessage) : null;
			
			// determine success and error classes for control 
			var formGroupClasses = 'form-group';
			if(validity) {
				if(validity.valid === false) {
					formGroupClasses += ' has-error';
				}
			}
			
			var multiple = attr.fieldType === 'MREF';
			if(attr.fieldType === 'MREF') attr.fieldType = 'CATEGORICAL';
			
			var id = attr.name;
			
			var description = attr.description !== undefined ? span({className: 'help-block'}, attr.description) : undefined;
			var labelClasses = this.props.layout === 'horizontal' ? 'col-sm-2 control-label' : 'control-label';
			//var labelElement = label({className: labelClasses, htmlFor: id}, lblInfo, lbl);
			var labelElement = label({className: labelClasses, htmlFor: id}, lbl);
			var control = molgenis.control.AttributeControl(__spread({}, this.props, {attr: attr, id: id, layout: undefined, multiple: multiple, onValueChange: this._handleValueChange, onBlur: this._handleBlur}));
			
			if(this.props.layout === 'horizontal') {
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
			var value = e.value;
			var validity = this._validate(value);
			
			this.setState({
				value: value,
				validity: validity,
				pristine: false // mark input as dirty
			});
			
			this.props.onValueChange({
				attr: this.state.attr.name,
				value: value,
				valid: validity.valid // notify parent of value validity immediately
			});
		},
		_handleBlur: function(e) {
			// only validate if control was touched
			if(this.state.pristine === true) {
				return;
			}
			
			this.setState(this._validate(this.state.value));
		},
		_validate: function(value) {
			// apply validation rules
			var attr = this.state.attr;
			
			var report;
			
			// required value constraint
			if(attr.nillable === false && (value === null || value === undefined)) {
				report = {valid: false, errorMessage: 'This field is required.'};
			}
			// browser input validation constraint
			else if(this.state.validity && this.state.validity.valid === false && this.state.validity.valueMissing === false) {
				var errorMessage;
				switch(attr.fieldType) {
					case 'EMAIL':
						errorMessage = 'Please enter a valid email address.';
						break;
					case 'HYPERLINK':
						errorMessage = 'Please enter a valid URL.';
						break;
					case 'DECIMAL':
					case 'INT':
					case 'LONG':
						errorMessage = 'Invalid value.';
						break;
					default:
						throw 'Unexpected data type: ' + attr.fieldType;
				}
				report = {valid: false, errorMessage: errorMessage};
			} else {
				report = {valid: true, errorMessage: undefined};	
			}
			// TODO validate uniqueness
			return report;
		}
	});
	
	var AttributeFormControlGroup = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'AttributeFormControlGroup',
		propTypes: {
			onValueChange: React.PropTypes.func.isRequired // TODO add all props
		},
		render: function() {
			var attributes = this.props.attr.attributes;
			
			// add control for each attribute
			var controls = [];
			for(var i = 0; i < attributes.length; ++i) {console.log('attribute', attributes[i]);
				var control;
				if(attributes[i].fieldType !== 'COMPOUND') { // FIXME attribute might be a href, so fieldtype does not exist
					control = AttributeFormControl({attr: attributes[i], key: 'attr' + i, onValueChange: this.props.onValueChange, validate: this.props.validate, layout: this.props.layout});
				} else {
					control = AttributeFormControlGroup({attr: attributes[i], key: 'attr' + i, onValueChange: this.props.onValueChange, validate: this.props.validate, layout: this.props.layout});
				}
				controls.push(control);
			}
			
			return (
				div({},
					h4({className: 'page-header'}, this.props.attr.label),
					div({className: 'row'},
						div({className: 'col-md-offset-1 col-md-11'},
							controls
						)
					)
				)
			);
		}
	});
	
	var Form = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'Form',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			layout: React.PropTypes.string
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
			for(var key in attributes) {console.log('attribute', attributes[key]);
				var control;
				if(attributes[key].fieldType !== 'COMPOUND') { // FIXME remove if, find other solution
					control = AttributeFormControl({attr: attributes[key], key: key, onValueChange: this._handleValueChange, validate: this.state.validate, layout: this.props.layout, ref: key});
				} else {
					control = AttributeFormControlGroup({attr: attributes[key], key: key, onValueChange: this._handleValueChange, layout: this.props.layout});
				}
				controls.push(control);
			}
			
			// add form buttons
			var saveControl = button({type: 'submit', className: 'btn btn-large btn-primary pull-right'}, 'Save');
			if(this.props.layout === 'horizontal') {
				saveControl = (
					div({className: 'form-group'},
						div({className: 'col-sm-offset-2 col-sm-10'},
							saveControl
						)
					)
				);
			}
			
			var formClasses = this.props.layout === 'horizontal' ? 'form-horizontal' : undefined; 
			return (
				form({className: formClasses, noValidate: true, onSubmit: this._handleSubmit},
					controls,
					saveControl
				)
			);
		},
		_handleValueChange: function(e) {console.log(e);
			this.state.values[e.attr] = {value: e.value, valid: e.valid};
			this.setState({values: this.state.values, valid: this.state.valid & e.valid});
		},
		_handleSubmit: function(e) {
			e.preventDefault();
			
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
						updatedEntity[key] = values[key].value; // FIXME continue ... fails for textarea
					}
				}
				
				// create on server
				api.create(this.state.entity.hrefCollection, updatedEntity);
			} else {
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