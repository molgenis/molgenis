/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, span = React.DOM.span;
    var api = new molgenis.RestClient();
    
	/**
	 * @memberOf component
	 */
	var Form = React.createClass({
		mixins: [molgenis.ui.mixin.EntityLoaderMixin, molgenis.ui.mixin.EntityInstanceLoaderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'Form',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]).isRequired,
			entityInstance: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			modal: React.PropTypes.bool, // whether or not to render form in a modal dialog
			enableOptionalFilter: React.PropTypes.bool, // whether or not to show a control to filter optional form fields
			saveOnBlur:React.PropTypes.bool, // save form control values on blur
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
				colOffset: 3,
				saveOnBlur: false,
				onSubmitCancel: function() {},
				onSubmitSuccess: function() {},
				onSubmitError: function() {},
				onValueChange: function() {}
			};
		},
		componentWillReceiveProps : function(nextProps) { // FIXME reload entity and entityinstance when changed
			var newState = {
				//invalids : {},
				//validate: false,
				errorMessages: {},	
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
				errorMessages : {},
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
				validate: this.state.validate,
				onValueChange : this._handleValueChange,
				errorMessages: this.state.errorMessages
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
 					this.props.mode !== 'view' ? FormButtonsFactory({
 						mode : this.props.mode,
 						formLayout : this.props.formLayout,
						colOffset : this.props.colOffset,
						cancelBtn: this.props.modal === true,
						onCancelClick : this.props.modal === true ? this._handleCancel : undefined
					}) : null
				)
			);

			return (
				div(null,
					AlertMessage,
					Filter,
					Form
				)
			);
		},
		_validate: function(attr, entityInstance, callback) {
			 // apply validation rules, not that IE9 does not support constraint validation API 
            var errorMessage = undefined;
          
            if (entityInstance !== null) {
            	if (entityInstance === undefined) entityInstance = {};//TODO fix this, why undefined if user clicks 'create' button without entering a value
            	var value = this._getValue(attr, entityInstance[attr.name]);
            	var type = attr.fieldType;
                var nullOrUndefinedValue = value === null || value === undefined;
                
                if(attr.nillable === false && type !== 'CATEGORICAL_MREF' && type !== 'MREF' && nullOrUndefinedValue && attr.auto !== true) { // required value constraint
	                errorMessage = 'Please enter a value.';
	            }
	            else if(attr.nillable === false && (type === 'CATEGORICAL_MREF' || type === 'MREF') && (nullOrUndefinedValue || value.items.length === 0)) { // required value constraint
	                errorMessage = 'Please enter a value.';
	            }
	            else if(type === 'EMAIL' && !nullOrUndefinedValue && !this._statics.REGEX_EMAIL.test(value)) {
	                errorMessage = 'Please enter a valid email address.';
	            }
	            else if(type === 'HYPERLINK' && !nullOrUndefinedValue && !this._statics.REGEX_URL.test(value)) {
	                errorMessage = 'Please enter a valid URL.';
	            }
	            else if(!attr.range && (type === 'INT' || type === 'LONG') && !nullOrUndefinedValue && !this._isInteger(value)) {
	                errorMessage = 'Please enter an integer value.';
	            }
	            else if(!attr.range && type === 'INT' && !nullOrUndefinedValue && !this._inRange(value, {min: this._statics.INT_MIN, max: this._statics.INT_MAX})) {
	                errorMessage = 'Please enter a value between ' + this._statics.INT_MIN + ' and ' + this._statics.INT_MAX + '.';
	            }
	            else if(!attr.range && type === 'LONG' && !nullOrUndefinedValue && !this._inRange(value, {min: this._statics.LONG_MIN, max: this._statics.LONG_MAX})) {
	                errorMessage = 'Please enter a value between ' + this._statics.LONG_MIN + ' and ' + this._statics.LONG_MAX + '.';
	            }
	            else if(attr.range && (type === 'INT' || type === 'LONG') && !nullOrUndefinedValue && !this._inRange(value, attr.range)) {
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
	            else if(attr.unique === true && !nullOrUndefinedValue && (this.props.mode === 'create' || value !== this.props.value)) { // value uniqueness constraint
	                // determine query value
	                var queryValue;
	                switch(type) {
	                    case 'CATEGORICAL':
	                    case 'XREF':
	                        queryValue = value[attr.refEntity.idAttribute];
	                        break;
	                    case 'CATEGORICAL_MREF':
	                    case 'MREF':
	                        queryValue = _.map(value, function(item) {
								return item[attr.refEntity.idAttribute];
							});
	                        break;
	                    default:
	                        queryValue = value;
	                        break;
	                }
	
	                // check if value already exists for this attribute
	                var rules = [{field: attr.name, operator: 'EQUALS', value: queryValue}];
	
	                api.getAsync(this.state.entity.hrefCollection, {q: {q: rules}}, function(data) {
	                    if(data.total > 0) {
	                        callback({valid: false, errorMessage: 'This ' + attr.label + ' already exists. It must be unique.'});
	                    } else {
	                        callback({valid: true, errorMessage: undefined});
	                    }
	                });
	                return;
	            }
	            
	            if (attr.validationExpression) {
	            	var form = {};
	            	_.each(this.state.atomicAttributes, function(entityAttr) {
	            		var enityAttrValue = this._getValue(entityAttr, entityInstance[entityAttr.name]);
	            		
	            		if (enityAttrValue !== null && enityAttrValue !== undefined) {
	            			if (entityAttr.fieldType === 'XREF' || entityAttr.fieldType === 'CATEGORICAL') {
	            				form[entityAttr.name] = enityAttrValue[entityAttr.refEntity.idAttribute];
	            			} else {
	            				form[entityAttr.name] = enityAttrValue;
	            			}
	            		}
	            	}, this);
	            	
	            	if (evalScript(attr.validationExpression, form) === false) {
	            		errorMessage = 'Please enter a valid value.';
	            	}
	            }
            }
            
            callback({valid: errorMessage === undefined, errorMessage: errorMessage});
		},
		_getValue: function(attr, value) {
        	// workaround for required bool attribute with no value implying false value
        	// TODO replace with elegant solution, deduplicate code in FormControls
            if(value === undefined && attr.fieldType === 'BOOL' && !attr.nillable) {
            	return false;
            } else {
            	return value;
            }
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
        _isInteger: function(value) {
            return Number.isInteger(value);
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
        },
		_handleValueChange: function(e) {
			var attribute = this.state.entity.attributes[e.attr];
			if (attribute === undefined) return;//compound
			
			// update value in entity instance
			var entityInstance = _.extend({}, this.state.entityInstance);
			entityInstance[e.attr] = e.value;
			this.setState({entityInstance: entityInstance});
			
			this._validate(attribute, entityInstance, function(validationResult) {
				var errorMessages = _.extend({}, this.state.errorMessages);
				
				if (validationResult.valid === true) {
					if (_.has(errorMessages, e.attr)) {
						this.setState({errorMessages: _.omit(errorMessages, e.attr)});
					}
					this.props.onValueChange(e);
					//TODO persist if needed
				} else {
					//Render controls with error messages
					errorMessages[e.attr] = validationResult.errorMessage;
					this.setState({errorMessages: errorMessages});
				}
			}.bind(this));
		},
		_handleSubmit: function(e) {
			// determine if form is valid
			var errorMessages = {};
			var valid = true;
			var entityInstance = this.state.entityInstance;
			
			_.each(this.state.atomicAttributes, function(attr) {
				this._validate(attr, entityInstance, function(validationResult) {
					valid = valid && validationResult.valid;
					if (validationResult.valid === false) {
						errorMessages[attr.name] = validationResult.errorMessage;
					}
				}.bind(this));
			}, this);
			
			
			if (!valid) {
				e.preventDefault(); // do not submit form
			}
			
			this.setState({errorMessages: errorMessages});
			
			//if(_.size(this.state.invalids) > 0) {
			//	e.preventDefault(); // do not submit form
			//	this.setState({validate: true}); // render validated controls
			//}
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
			validate: React.PropTypes.bool,
			onValueChange: React.PropTypes.func.isRequired,
			errorMessages: React.PropTypes.object.isRequired
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
						
						if (attr.fieldType === 'COMPOUND') {
							controlProps['errorMessages'] = this.props.errorMessages;
						} else {
							controlProps['errorMessage'] = this.props.errorMessages[attr.name];
						}
						
						// IE9 does not support the autofocus attribute, focus the first visible input manually
						if(this.props.mode !== 'view' && !foundFocusControl && attr.visible === true && (this.props.mode === 'create' || attr.readOnly !== true)) {
							_.extend(controlProps, {focus: true});
							foundFocusControl = true;
						}
						
						var Control = ControlFactory(controlProps);
						if(attr.nillable === true && this.props.hideOptional === true) {
							Control = div({className: 'hide'}, Control);
						}
						controls.push(Control);
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