/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, span = React.DOM.span, ol = React.DOM.ol, li = React.DOM.li, a = React.DOM.a;
	
	/**
	 * @memberOf component
	 */
	var Form = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.EntityLoaderMixin, molgenis.ui.mixin.EntityInstanceLoaderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'Form',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]).isRequired,
			entityInstance: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			modal: React.PropTypes.bool, // whether or not to render form in a modal dialog
			enableOptionalFilter: React.PropTypes.bool, // whether or not to show a control to filter optional form fields
			saveOnBlur: React.PropTypes.bool, // save form control values on blur
			enableFormIndex: React.PropTypes.bool, // whether or not to show a form index to navigate to form controls
			beforeSubmit: React.PropTypes.func,
			onSubmitCancel: React.PropTypes.func,
			onSubmitSuccess: React.PropTypes.func,
			onSubmitError: React.PropTypes.func,
			onValueChange: React.PropTypes.func
		},
		getDefaultProps: function() {
			return {
				mode: 'create',
				formLayout: 'horizontal',
				modal: false,
				enableOptionalFilter: true,
				enableFormIndex: false,
				colOffset: 3,
				saveOnBlur: false,
				beforeSubmit: function() {},
				onSubmitCancel: function() {},
				onSubmitSuccess: function() {},
				onSubmitError: function() {},
				onValueChange: function() {}
			};
		},
		componentWillReceiveProps : function(nextProps) {
			var newState = {
				invalids : {},
				validate: false,
				showModal: true,
				submitMsg: null
			};
			if(this.props.mode === 'create') {
				_.extend(newState, {entityInstance: {}});
			}
			this.setState(newState);
		},
		getInitialState: function() {
			return {
				entity : null,			// transfered from props to state, loaded from server if required
				entityInstance : null,	// transfered from props to state, loaded from server if required
				invalids : {},
				validate : false,
				showModal: true,
				hideOptional: false
			};
		},
		componentWillMount: function() {
			if(this.props.mode === 'create') {
				this.setState({entityInstance: {}});
			}
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
					molgenis.ui.Modal({title: title, size: 'large', show: this.state.showModal, onHide: this._handleCancel},
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
				encType : 'application/x-www-form-urlencoded', // TODO use multipart/form-data if form contains one or more file inputs
				noValidate : true,
				beforeSubmit: this.props.beforeSubmit,
				onSubmit : this._handleSubmit,
				success: this._handleSubmitSuccess,
				error: this._handleSubmitError,
				key: 'form'
			};
			
			var formControlsProps = {
				entity : this.state.entity,
				value: this.state.entityInstance, // FIXME replace value with entity instance
				mode : this.props.mode,
				formLayout : this.props.formLayout,
				colOffset : this.props.colOffset,
				hideOptional: this.state.hideOptional,
				saveOnBlur: this.props.saveOnBlur,
				enableFormIndex: this.props.enableFormIndex,
				validate: this.state.validate,
				onValueChange : this._handleValueChange
			};
			
			var AlertMessage = this.state.submitMsg ? (
				molgenis.ui.AlertMessage({type: this.state.submitMsg.type, message: this.state.submitMsg.message, onDismiss: this._handleAlertMessageDismiss, key: 'alert'})	
			) : null;
			
			var Filter = this.props.enableOptionalFilter ? (
				div({className: 'row', style: {textAlign: 'right'}, key: 'filter'},
					div({className: 'col-md-12'},
 						molgenis.ui.Button({
							icon : this.state.hideOptional ? 'eye-open' : 'eye-close',
							title: this.state.hideOptional ? 'Show all fields' : 'Hide optional fields',
							size: 'xsmall',
							css : {
								marginBottom : 15,
								textAlign : 'right'
							},
							onClick : this._handleOptionalFilterClick
						})
					)
				)
			) : null;
					
			var Form = (
				molgenis.ui.wrapper.JQueryForm(formProps,
					FormControlsFactory(formControlsProps),
 					this.props.mode !== 'view' && !(this.props.mode === 'edit' && this.props.saveOnBlur) ? FormButtonsFactory({
 						mode : this.props.mode,
 						formLayout : this.props.formLayout,
						colOffset : this.props.colOffset,
						cancelBtn: this.props.modal === true,
						onCancelClick : this.props.modal === true ? this._handleCancel : undefined
					}) : null,
					this.props.children
				)
			);

			var FormWithMessageAndFilter = (
				div(null,
					AlertMessage,
					Filter,
					Form
				)
			);
			
			if(this.props.enableFormIndex) {
				return (
					div({className: 'row'},
						div({className: 'col-md-2'},
							FormIndexFactory({entity: this.state.entity})
						),
						div({className: 'col-md-10'},
							FormWithMessageAndFilter
						)
					)
				);
			} else {
				return FormWithMessageAndFilter;
			}
		},
		_handleValueChange: function(e) {
			// update value in entity instance
			var entityInstance = _.extend({}, this.state.entityInstance);
			entityInstance[e.attr] = e.value;
			this.setState({entityInstance: entityInstance});
			
			var invalids = this.state.invalids;
			if(e.valid === true) {
				// remove item from invalids
				if(_.has(invalids, e.attr)) {
					invalids = _.omit(this.state.invalids, e.attr);
					this.setState({invalids: invalids});
				}
			} else {
				// add item to invalids
				if(!_.has(invalids, e.attr)) {
					invalids[e.attr] = null;
					this.setState({invalids: invalids});
				}
			}
			
			this.props.onValueChange(e);
		},
		_handleSubmit: function(e) {
			// determine if form is valid
			if(_.size(this.state.invalids) > 0) {
				e.preventDefault(); // do not submit form
				this.setState({validate: true}); // render validated controls
			}
		},
		_handleCancel: function() {
			if(this.props.modal) {
				this.setState({showModal: false});
			}
			this.props.onSubmitCancel();
		},
		_handleSubmitSuccess: function() {
			var message = this.props.mode === 'create' ? 'has been created.' : 'changes have been saved.';
			var stateProps = {
				submitMsg: {type: 'success', message: this.state.entity.label + ' ' + message},
				invalids : {},
				validate : false,
			};
			if(this.props.modal) {
				_.extend(stateProps, {
					showModal : false
				});
			}
			if(this.props.mode === 'create') {
				// clear form to create new entity
				_.extend(stateProps, {
					entityInstance : {}
				});
			}
			this.setState(stateProps);
			
			if(!this.props.modal) {
				window.scrollTo(0, 0);
			}
			
			this.props.onSubmitSuccess();
		},
		_handleSubmitError: function() {
			var message = this.props.mode === 'create' ? 'could not be created.' : 'changes could not be saved.';
			this.setState({
				submitMsg: {type: 'danger', message: this.state.entity.label + ' ' + message}
			});
			
			if(!this.props.modal) {
				window.scrollTo(0, 0);
			}
			
			this.props.onSubmitError();
		},
		_handleAlertMessageDismiss: function() {
			this.setState({
				submitMsg: undefined
			});
		},
		_handleOptionalFilterClick: function() {
			this.setState({
				hideOptional: !this.state.hideOptional
			});
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
			hideOptional: React.PropTypes.bool,
			saveOnBlur: React.PropTypes.bool,
			enableFormIndex: React.PropTypes.bool,
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
						var ControlFactory = attr.fieldType === 'COMPOUND' ? molgenis.ui.FormControlGroup : molgenis.ui.FormControl;
						var controlProps = {
							entity : this.props.entity,
							entityInstance : this.props.value,
							attr : attr,
							value: attr.fieldType === 'COMPOUND' ? this.props.value : (this.props.value ? this.props.value[key] : undefined),
							mode : this.props.mode,
							formLayout : this.props.formLayout,
							colOffset: this.props.colOffset,
							saveOnBlur: this.props.saveOnBlur,
							validate: this.props.validate,
							onValueChange : this.props.onValueChange,
							key : key
						};
						
						// IE9 does not support the autofocus attribute, focus the first visible input manually
						if(this.props.mode !== 'view' && !foundFocusControl && attr.visible === true && (this.props.mode === 'create' || attr.readOnly !== true)) {
							_.extend(controlProps, {focus: true});
							foundFocusControl = true;
						}
						
						var Control = ControlFactory(controlProps);
						if(attr.nillable === true && this.props.hideOptional === true) {
							Control = div({className: 'hide'}, Control);
						} else if(this.props.enableFormIndex === true && attr.fieldType === 'COMPOUND') {
							Control = div({id: this._getLinkId(attr)}, Control);
						}
						controls.push(Control);
					}
				}
			}
			return div({}, controls);
		},
		_getLinkId: function(attr) {
			return attr.name + '-link';
		}
	});
	var FormControlsFactory = React.createFactory(FormControls);
	
	/**
	 * @memberOf component
	 */
	var FormButtons = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'FormButtons',
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
	
	/**
	 * @memberOf component
	 */
	var FormIndex = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'FormIndex',
		propTypes: {
			entity: React.PropTypes.object.isRequired
		},
		render: function() {
			var IndexItems = [];
			var attrs = this.props.entity.attributes;
			for (var key in attrs) {
				if (attrs.hasOwnProperty(key)) {
					var attr = attrs[key];
					if(attr.fieldType === 'COMPOUND') {
						var IndexItem = (
							li({key: attr.name},
								a({href: this._getLinkName(attr)}, attr.label)
							)
						);
						IndexItems.push(IndexItem);
					}
				}
			}
			
			return (
				ol({style: {'list-style-type': 'none'}},
					IndexItems
				)
			);
		},
		_getLinkName: function(attr) {
			return '#' + attr.name + '-link';
		}
	});
	var FormIndexFactory = React.createFactory(FormIndex);
	
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        Form: React.createFactory(Form)
    });
}(_, React, molgenis));	