/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, button = React.DOM.button, form = React.DOM.form;
    
    /**
	 * @memberOf component
	 */
	var FormSubmitButton = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
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
	var FormSubmitButtonFactory = React.createFactory(FormSubmitButton);
	
	/**
	 * @memberOf component
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
					if(this.props.mode !== 'create' || (this.props.mode === 'create' && attr.auto !== true)) {
						var Control = attr.fieldType === 'COMPOUND' ? molgenis.ui.FormControlGroup : molgenis.ui.FormControl;
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
	var FormControlsFactory = React.createFactory(FormControls);
	
	/**
	 * @memberOf component
	 */
	var Form = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.EntityLoaderMixin],
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
		render: function() {
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
					FormControlsFactory(formControlsProps),
					this.props.mode !== 'view' ? FormSubmitButtonFactory({layout: this.props.formLayout, colOffset: this.props.colOffset}) : null
				)
			);
		},
		_handleValueChange: function(e) {
			this.state.values[e.attr] = {value: e.value, valid: e.valid};
			this.setState({values: this.state.values, valid: this.state.valid && e.valid});
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
				for(var valueKey in values) {
					if(values.hasOwnProperty(valueKey)) {
						updatedEntity[valueKey] = values[valueKey].value;
					}
				}
				
				
			} else {
				e.preventDefault(); // do not submit form
				this.setState({validate: true}); // render validated controls
			}
		}
	});
	
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        Form: React.createFactory(Form)
    });
}(_, React, molgenis));	