(function($, molgenis) {
	"use strict";

	var form = React.DOM.form, div = React.DOM.div, label = React.DOM.label, button = React.DOM.button, span = React.DOM.span, a = React.DOM.a;
	var __spread = React.__spread;
	
	var api = new molgenis.RestClient();
	
	var AttributeFormControl = React.createClass({
//		mixins: [DeepPureRenderMixin],
		displayName: 'AttributeFormControl',
		getInitialState: function() {
			return {
				value: this.props.value,
				pristine: true,
				valid: undefined
			};
		},
		componentDidMount: function() {console.log('componentDidMount AttributeFormControl');
			if(this.props.attr.description !== undefined) {
				$(this.refs.popover.getDOMNode()).popover(); // Bootstrap requires you to initialize popovers yourself
			}
			
			// notify parent of initial value validity
			this.props.onValueChange({
				attr: this.props.attr.name,
				value: this.state.value,
				valid: this._validate().valid
			});
		},
		componentWillUnmount: function() {console.log('componentWillUnmount AttributeFormControl');
			if(this.props.attr.description !== undefined) {
				$(this.refs.popover.getDOMNode()).popover('destroy'); // cleanup
			}
		},
		render: function() {console.log('render AttributeFormControl', this.state, this.props);
			var lbl = this.props.attr.label;
			
			// add info control for attribute description
			var lblInfo;
			if(this.props.attr.description !== undefined) {
				lblInfo = (
					a({href: '#', 'data-toggle': 'popover', 'data-content': this.props.attr.description, 'data-trigger': 'focus', ref: 'popover'},
						span({className: 'glyphicon glyphicon-info-sign'})
					)
				);
				lbl = ' ' + lbl; // put some space between icon and label text
			}
			
			if(this.props.attr.nillable === false) {
				lbl += ' *';
			}
			
			// add validation error message
			var errorMessageSpan = this.state.valid === false ? span({className: 'help-block'}, this.state.errorMessage) : null;
			
			// determine success and error classes for control 
			var formGroupClasses = 'form-group';
			if(this.state.valid === false) {
				formGroupClasses += ' has-error';
			} else if(this.state.pristine === false && this.state.valid === true) {
				formGroupClasses += ' has-success';
			}
			 
			var id = this.props.attr.name;
			return(
				div({className: formGroupClasses},
					label({className: 'col-sm-2 control-label', htmlFor: id}, lblInfo, lbl),
					div({className: 'col-sm-10'},
						React.createElement(molgenis.controls.AttributeControl, __spread({}, this.props, {id: id, onValueChange: this._handleValueChange, onBlur: this._handleBlur})),
						errorMessageSpan
					)
				)
			);	
		},
		_handleValueChange: function(e) {
			this.setState({
				value: e.value,
				valid: undefined, // postpone displaying validation errors until blur event
				validity: e.validity,
				pristine: false // mark input as dirty
			});
			
			this.props.onValueChange({
				attr: this.props.attr.name,
				value: e.value,
				valid: this._validate().valid // notify parent of value validity immediately
			});
		},
		_handleBlur: function(e) {
			// only validate if control was touched
			if(this.state.pristine === true) {
				return;
			}
			
			this.setState(this._validate());
		},
		_validate: function() {
			// apply validation rules
			var attr = this.props.attr;
			var value = this.state.value;
			
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
	
	var Form = React.createClass({
//		mixins: [DeepPureRenderMixin],
		displayName: 'Form',
		propTypes: {
			entity: React.PropTypes.object
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
			var controls = [];
			for(var key in this.state.entity.attributes) {
				var control = React.createElement(AttributeFormControl, {attr: this.state.entity.attributes[key], key: key, onValueChange: this._handleValueChange});
				controls.push(control);
			}
			
			// add form buttons
			var saveControl = (
				div({className: 'form-group', key: 'submitBtn'},
					div({className: 'col-sm-offset-2 col-sm-10'},
						button({type: 'submit', className: 'btn btn-large btn-primary pull-right'}, 'Save')
					)
				)
			);
			controls.push(saveControl);
			
			return (
				form({className: 'form-horizontal', noValidate: true, onSubmit: this._handleSubmit},
					controls
				)
			);
		},
		_handleValueChange: function(e) {console.log(e);
			this.state.values[e.attr] = {value: e.value, valid: e.valid};
			this.setState({values: this.state.values, valid: this.state.valid & e.valid});
		},
		_handleSubmit: function(e) {
			e.preventDefault();
			
			for(var key in this.state.entity.attributes) {
				// TODO continue
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

	molgenis.Form = Form;
	
}($, window.top.molgenis = window.top.molgenis || {}));