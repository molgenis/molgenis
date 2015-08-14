/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, span = React.DOM.span, ol = React.DOM.ol, li = React.DOM.li, a = React.DOM.a;
    var api = new molgenis.RestClient();
    
	/**
	 * @memberOf component
	 */
	var Form = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin, molgenis.ui.mixin.EntityLoaderMixin, molgenis.ui.mixin.EntityInstanceLoaderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'Form',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]).isRequired,
			entityInstance: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number, React.PropTypes.object]),
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			modal: React.PropTypes.bool, // whether or not to render form in a modal dialog
			enableOptionalFilter: React.PropTypes.bool, // whether or not to show a control to filter optional form fields
			saveOnBlur: React.PropTypes.bool, // save form control values on blur
			enableFormIndex: React.PropTypes.bool, // whether or not to show a form index to navigate to form controls
			showHidden: React.PropTypes.bool, // whether or not to show not-visible attributes
			categorigalMrefShowSelectAll: React.PropTypes.bool, //whether to show 'select all' and 'hide all' links under the categorical mref checkboxes
			showAsteriskIfNotNillable: React.PropTypes.bool, //whether to show a '*' after the label when an attribute is not nillable
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
				enableFormIndex: true,
				colOffset: 3,
				saveOnBlur: false,
				showHidden: false,
				categorigalMrefShowSelectAll: true,
				showAsteriskIfNotNillable: true,
				beforeSubmit: function() {},
				onSubmitCancel: function() {},
				onSubmitSuccess: function() {},
				onSubmitError: function() {},
				onValueChange: function() {}
			};
		},
		componentWillReceiveProps : function(nextProps) {
			var newState = {
				errorMessages : {},
				validate: false,
				showModal: true,
				submitMsg: null
			};
			
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
		_willSetEntityInstance: function(entity, entityInstance) {
			_.each(entity.allAttributes, function(attr) {
				if (attr.visibleExpression) {
					attr.visible = this._resolveBoolExpression(attr.visibleExpression, entityInstance);
				}
			}, this);
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
				showHidden: this.props.showHidden,
				enableFormIndex: this.props.enableFormIndex,
				categorigalMrefShowSelectAll: this.props.categorigalMrefShowSelectAll,
				showAsteriskIfNotNillable: this.props.showAsteriskIfNotNillable,
				onValueChange : this._handleValueChange,
				onBlur: this._handleBlur,
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
 					this.props.mode !== 'view' && !(this.props.mode === 'edit' && this.props.saveOnBlur) ? FormButtonsFactory({
 						mode : this.props.mode,
 						formLayout : this.props.formLayout,
						colOffset : this.props.colOffset,
						cancelBtn: this.props.modal === true,
						onCancelClick : this.props.modal === true ? this._handleCancel : undefined,
						onSubmitClick: this.submit
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
						div({className: 'col-md-10'},
							FormWithMessageAndFilter
						),
						div({className: 'col-md-2'},
							FormIndexFactory({entity: this.state.entity})
						)
					)
				);
			} else {
				return FormWithMessageAndFilter;
			}
		},
		_handleBlur: function(e) {
			var attr = e.attr;
			var value = e.value;
			
			this._validate(attr, this._getValue(this.state.entityInstance, attr), function(validationResult) {
            	if(validationResult.valid === true && this._doPersistAttributeValue(attr)) {
            		this._persistAttributeValue(attr, value);
                }
            	
            	this.setState({
					errorMessages: this._updateErrorMessages(attr, validationResult)
				});
            	
            }.bind(this));
		},
		_doPersistAttributeValue: function(attr) {
			return this.props.mode === 'edit' && this.props.saveOnBlur && !attr.readOnly;
	    },
		_persistAttributeValue: function(attr, value) {
			// persist attribute
    		var val;
        	switch(attr.fieldType) {
        		case 'CATEGORICAL':
        		case 'XREF':
        			val = value !== null && value !== undefined ? value[attr.refEntity.idAttribute] : null;
        			break;
        		case 'CATEGORICAL_MREF':
        		case 'MREF':
        			val = _.map(value.items, function(item) {
        				return item[attr.refEntity.idAttribute];
        			});
        			break;
        		default:
        			val = value;
        			break;
        	}
        	
        	api.update(this.state.entityInstance.href + '/' + attr.name, val, {}, false);
	    },
		_handleValueChange: function(e) {
			var attribute = this.state.entity.allAttributes[e.attr];
			if (attribute === undefined) return;//compound
			
			// update value in entity instance
			var value = e.value;
			var entityInstance = _.extend({}, this.state.entityInstance);
			entityInstance[e.attr] = value;
			var attr = this.state.entity.allAttributes[e.attr];
			
			//Validate new value
			this._validate(attr, value, function(validationResult) {
				
				if(validationResult.valid === true) {
					// Resolve visible expressions
					_.each(this.state.entity.allAttributes, function(entityAttr) {
						if (entityAttr.visibleExpression) {
							entityAttr.visible = this._resolveBoolExpression(entityAttr.visibleExpression, entityInstance);
						}
					}, this);
				}
				
				this.setState({
					entityInstance: entityInstance,
					errorMessages: this._updateErrorMessages(attr, validationResult)
				});
				
				if (validationResult.valid) {
					this.props.onValueChange(e);
				}
				
				if(validationResult.valid === true && this._doPersistAttributeValue(attr)) {
                    // persist changes for controls that do not have a blur event
	                switch(attr.fieldType) {
		                case 'BOOL':
		                case 'CATEGORICAL':
		                case 'CATEGORICAL_MREF':
		                case 'ENUM':
		                case 'MREF':
		                case 'XREF':
		                case 'DATE':
		                case 'DATE_TIME':
		                	this._persistAttributeValue(attr, value);
		                	break;
		                default:
		                	break;
	                }
                }
			}.bind(this));
		},
		submit: function(e) {
			// determine if form is valid
			var errorMessages = {};
			var promises = [];
			var target = e.target;
			
			_.each(this.state.entity.allAttributes, function(attr) {
				if (attr.fieldType !== 'COMPOUND') {
					var p = new Promise(function(resolve, reject) {
						this._validate(attr, this._getValue(this.state.entityInstance, attr), function(validationResult) {
							if (validationResult.valid === false) {
								errorMessages[attr.name] = validationResult.errorMessage;
							}
							resolve(validationResult.valid);
						}.bind(this));
					}.bind(this));
				
					promises.push(p);
				}
			}, this);
			
			Promise.all(promises).done(function(results) {
				var valid = true;
				for (var i = 0; i < results.length && valid; i++) {
					valid = valid && results[i];
				}
			
				if (valid) {
					$(target).closest('form').submit();//TODO remove jquery form submit workaround, see also componentDidMount in JQueryForm.js
				} else {
					this.setState({errorMessages: errorMessages});
				}
			}.bind(this));
		},
		_handleCancel: function() {
			if(this.props.modal) {
				this.setState({showModal: false});
			}
			this.props.onSubmitCancel();
		},
		_handleSubmitSuccess: function(responseText, statusText, xhr, element) {
			var message = this.props.mode === 'create' ? 'has been created.' : 'changes have been saved.';
			var stateProps = {
				submitMsg: {type: 'success', message: this.state.entity.label + ' ' + message},
				errorMessages : {},
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
			
			var e = {};
			if (this.props.mode === 'create') {
				e.location = xhr.getResponseHeader('Location')
			}
			this.props.onSubmitSuccess(e);
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
		},
		_updateErrorMessages: function(attr, validationResult) {
			var errorMessages = this.state.errorMessages;
			
			if (validationResult.valid) {
				if (_.has(errorMessages, attr.name)) {
					delete errorMessages[attr.name];
				}
			} else {
				errorMessages[attr.name] = validationResult.errorMessage;
			}
			
			return errorMessages;
		},
		_validate: function(attr, value, callback) {
            // apply validation rules, not that IE9 does not support constraint validation API 
            var type = attr.fieldType;
            var nullOrUndefinedValue = value === null || value === undefined;
        	var entityInstance = _.extend({}, this.state.entityInstance);
            var errorMessage = undefined;
            var computed = (attr.expression !== undefined);
            
            if (!computed) {//Do not validate computed attributes
	            if(attr.nillable === false && type !== 'CATEGORICAL_MREF' && type !== 'MREF' && nullOrUndefinedValue && !attr.auto) { // required value constraint
	            	if (attr.visibleExpression === undefined || this._resolveBoolExpression(attr.visibleExpression, entityInstance) === true) {
	            		errorMessage = 'Please enter a value.';
	            	}
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
	            else if(attr.unique === true && !nullOrUndefinedValue) { // value uniqueness constraint
	            	
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
	                	var idAttribute = data.meta.idAttribute;
	                    if(data.total > 0 && ((this.props.mode === 'create') || (data.items[0][idAttribute] !== this.state.entityInstance[idAttribute]))) {
	                        callback({valid: false, errorMessage: 'This ' + attr.label + ' already exists. It must be unique.'});
	                    } else {
	                        callback({valid: true, errorMessage: undefined});
	                    }
	                }.bind(this));
	                return;
	            }
	            
	            if (attr.validationExpression) {
	            	entityInstance[attr.name] = value;
	            	if (this._resolveBoolExpression(attr.validationExpression, entityInstance) === false) {
	            		errorMessage = 'Please enter a valid value.';
	            	}
	            }
            }
            
            callback({valid: errorMessage === undefined, errorMessage: errorMessage});
        },
        _resolveBoolExpression: function(expression, entityInstance) {
        	//TODO make evalScript work with entities
        	var form = {};
        	_.each(this.state.entity.allAttributes, function(attr) {
        		var value = entityInstance[attr.name];
        		
        		if (value !== null && value !== undefined) {
        			 switch(attr.fieldType) {
                     case 'CATEGORICAL':
                     case 'XREF':
                    	 form[attr.name] = value[attr.refEntity.idAttribute];
                         break;
                     case 'CATEGORICAL_MREF':
                     case 'MREF':
                         form[attr.name] = _.map(value.items, function(item) {
                        	 return item[attr.refEntity.idAttribute]; 
                         }).join();
                         break;
                     case 'COMPOUND':
                    	 //nothing, no value
                    	 break;
                     default:
                    	 form[attr.name] = value;
                         break;
        			 }
        		}
        	}, this);
        	
        	return evalScript(expression, form);
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
        _getValue: function(entityInstance, attr) {
        	//Please don't manipulate the values here. It is not the place to do it!
        	return entityInstance[attr.name];
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
			showHidden: React.PropTypes.bool,
			categorigalMrefShowSelectAll: React.PropTypes.bool,
			showAsteriskIfNotNillable: React.PropTypes.bool,
			enableFormIndex: React.PropTypes.bool,
			errorMessages: React.PropTypes.object.isRequired,
			onValueChange: React.PropTypes.func.isRequired,
			onBlur: React.PropTypes.func.isRequired
		},
		render: function() {
			// add control for each attribute
			var foundFocusControl = false;
			var attributes = this.props.entity.attributes;
			var controls = [];
			for(var key in attributes) {
				if(attributes.hasOwnProperty(key)) {
					var attr = attributes[key];
					if((this.props.mode !== 'create' || (this.props.mode === 'create' && attr.auto !== true)) && 
						((attr.visibleExpression === undefined) || (this.props.entity.allAttributes[attr.name].visible === true))) {
						var ControlFactory = attr.fieldType === 'COMPOUND' ? molgenis.ui.FormControlGroup : molgenis.ui.FormControl;
						var controlProps = {
							entity : this.props.entity,
							entityInstance : this.props.value,
							attr : attr,
							value: attr.fieldType === 'COMPOUND' ? this.props.value : (this.props.value ? this.props.value[key] : undefined),
							mode : this.props.mode,
							formLayout : this.props.formLayout,
							colOffset: this.props.colOffset,
							onBlur: this.props.onBlur,
							categorigalMrefShowSelectAll: this.props.categorigalMrefShowSelectAll,
							showAsteriskIfNotNillable: this.props.showAsteriskIfNotNillable,
							onValueChange : this.props.onValueChange,
							key : key 
						};
						
						if (attr.fieldType === 'COMPOUND') {
							_.extend(controlProps, {
								errorMessages : this.props.errorMessages,
								hideOptional : this.props.hideOptional
							});
						} else {
							controlProps['errorMessage'] = this.props.errorMessages[attr.name];
						}
						
						// IE9 does not support the autofocus attribute, focus the first visible input manually
						if(this.props.mode !== 'view' && !foundFocusControl && attr.visible === true && (this.props.mode === 'create' || attr.readOnly !== true)) {
							_.extend(controlProps, {focus: true});
							foundFocusControl = true;
						}
						
						var Control = ControlFactory(controlProps);
						if(attr.nillable === true && this.props.hideOptional === true || (this.props.showHidden === false && attr.visible === false)) {
							Control = div({className: 'hide', key: key + '-hide'}, Control);
						} else if(this.props.enableFormIndex === true && attr.fieldType === 'COMPOUND') {
							controls.push(div({id: this._getLinkId(attr), className: 'anchor', key: key + '-link'}));
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
			onSubmitClick: React.PropTypes.func.isRequired
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
						molgenis.ui.Button({type: 'button', style: 'primary', css: {marginLeft: 5}, text: submitBtnText, onClick: this.props.onSubmitClick})
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
							li({key: attr.name, className: 'list-group-item'},
								a({href: this._getLinkName(attr)}, attr.label)
							)
						);
						IndexItems.push(IndexItem);
					}
				}
			}
			
			return (
				div({id: 'sidebar', className: 'affix'},
						ol({style: {listStyleType: 'none'}, className: 'list-group'},
								IndexItems
						)
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