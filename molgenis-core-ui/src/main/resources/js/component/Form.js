/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, span = React.DOM.span;
	
	/**
	 * @memberOf component
	 */
	var Form = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.EntityLoaderMixin, molgenis.ui.mixin.EntityInstanceLoaderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'Form',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			entityInstance: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			modal: React.PropTypes.bool, // whether or not to show form in modal
			colOffset: React.PropTypes.number,
			onSubmitSuccess: React.PropTypes.func,
			onSubmitError: React.PropTypes.func
		},
		getDefaultProps: function() {
			return {
				mode: 'create',
				formLayout: 'horizontal',
				modal: false,
				colOffset: 3
			};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				validate: false,
				showModal: true
			});
		},
		getInitialState: function() {
			return {
				entity : null,
				entityInstance : null,
				values : {},
				validate : false,
				showModal: true
			};
		},
		render: function() {
			// render form in component container
			return this.props.modal ? span() : this._render();
		},
		renderLayer: function() {
			// render form in modal dialog
			return this.props.modal ? this._render() : span();
		},
		_render: function() {
			var Form = this._renderForm();
			
			if(this.props.modal === true) {
				// determine modal title based on form mode
				var title;
				if(this.state.entity !== null) {
					switch(this.props.mode) {
						case 'create':
							title = 'Create ' + this.state.entity.label;
							break;
						case 'edit':
							title = 'Edit ' + this.state.entity.label;
							break;
						case 'view':
							title = 'View' + this.state.entity.label;
							break;
						default:
							throw 'unknown mode [' + this.props.mode + ']';
					}
				} else {
					title = '';
				}
				
				return (
					molgenis.ui.Modal({title: title, show: this.state.showModal, onHide: this._handleCancel},
						Form
					)
				);
			} else {
				return Form;
			}
		},
		_renderForm: function() {
			// return empty div if entity data is not yet available
			if(this.state.entity === null) {
				return molgenis.ui.Spinner();
			}
			// return empty div if entity value is not yet available
			if((this.props.mode === 'edit' || this.props.mode === 'view') && this.state.entityInstance === null) {
				return molgenis.ui.Spinner();
			}
			
			var action, method;
			switch(this.props.mode) {
				case 'create':
					action = this.state.entity.hrefCollection;
					method = 'post';
					break;
				case 'edit':
					action = this.state.entityInstance.href + '?_method=PUT';
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
				onSubmit : this._handleSubmit,
				success: this._handleSubmitSuccess,
				error: this.props.onSubmitError
			};
			
			var formControlsProps = {
				entity : this.state.entity,
				value: this.props.mode !== 'create' ? this.state.entityInstance : undefined, // FIXME replace value with entity instance
				mode : this.props.mode,
				formLayout : this.props.formLayout,
				colOffset : this.props.colOffset,
				validate: this.state.validate,
				onValueChange : this._handleValueChange
			};
			
			return (
				molgenis.ui.wrapper.JQueryForm(formProps,
					FormControlsFactory(formControlsProps),
 					this.props.mode !== 'view' ? FormButtonsFactory({
 						mode : this.props.mode,
 						formLayout : this.props.formLayout,
						colOffset : this.props.colOffset,
						cancelBtn: this.props.modal === true,
						onCancelClick : this.props.modal === true ? this._handleCancel : undefined
					}) : null
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
		},
		_handleCancel: function() {
			this.setState({showModal: false});
		},
		_handleSubmitSuccess: function() {
			if(this.props.modal) {
				this.setState({showModal: false});
			}
			if(this.props.onSubmitSuccess) {
				this.props.onSubmitSuccess();
			}
		}
	});
		
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
			var foundFocusControl = false;
			var attributes = this.props.entity.attributes;
			var controls = [];
			for(var key in attributes) {
				if(attributes.hasOwnProperty(key)) {
					var attr = attributes[key];
					if(this.props.mode !== 'create' || (this.props.mode === 'create' && attr.auto !== true)) {
						var Control = attr.fieldType === 'COMPOUND' ? molgenis.ui.FormControlGroup : molgenis.ui.FormControl;
						var controlProps = {
							entity : this.props.entity,
							attr : attr,
							value: this.props.value ? this.props.value[key] : undefined,
							mode : this.props.mode,
							formLayout : this.props.formLayout,
							colOffset: this.props.colOffset,
							validate: this.props.validate,
							onValueChange : this.props.onValueChange,
							key : key
						};
						
						// IE9 does not support the autofocus attribute, focus the first visible input manually
						if(!foundFocusControl && attr.visible === true) {
							_.extend(controlProps, {focus: true});
							foundFocusControl = true;
						}
						controls.push(Control(controlProps));
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
	var FormButtons = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'FormSubmitButton',
		propTypes: {
			mode: React.PropTypes.oneOf(['create', 'edit']).isRequired,
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']).isRequired,
			colOffset: React.PropTypes.number,
			cancelBtn: React.PropTypes.bool,
			onCancelClick: React.PropTypes.func,
		},
		getDefaultProps: function() {
			return {
				onCancelClick : function() {}
			};
		},
		render: function() {
			var divClasses;
			if(this.props.formLayout === 'horizontal') {
				divClasses = 'col-md-offset-' + this.props.colOffset + ' col-md-' + (12 - this.props.colOffset);
			} else {
				divClasses = 'col-md-12';
			}
			
			var submitBtnText = this.props.mode === 'create' ? 'Create' : 'Save changes';
			return (
				div({className: 'row', style: {textAlign: 'right'}},
					div({className: divClasses},
						this.props.cancelBtn ? molgenis.ui.Button({text: 'Cancel', onClick: this.props.onCancelClick}, 'Cancel') : null,
						molgenis.ui.Button({type: 'submit', style: 'primary', css: {marginLeft: 5}, text: submitBtnText})
					)
				)
			);
		}
	});
	var FormButtonsFactory = React.createFactory(FormButtons);
	
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        Form: React.createFactory(Form)
    });
}(_, React, molgenis));	