/* global _: false, React: false, URI: false, molgenis: true */
(function(_, React, URI, molgenis) {
    "use strict";

    var div = React.DOM.div, span = React.DOM.span, label = React.DOM.label, strong = React.DOM.strong, a = React.DOM.a;
    
    var api = new molgenis.RestClient();
    
    /**
     * @memberOf component
     */
    var FormControl = React.createClass({
        mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.AttributeLoaderMixin],
        displayName: 'FormControl',
        propTypes: {
            entity: React.PropTypes.object.isRequired,
            entityInstance: React.PropTypes.object,
            attr: React.PropTypes.object.isRequired,
            formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
            mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
            saveOnBlur: React.PropTypes.bool,
            colOffset: React.PropTypes.number,
            validate: React.PropTypes.bool,
            focus: React.PropTypes.bool,
            value: React.PropTypes.any,
            onValueChange: React.PropTypes.func.isRequired
        },
        getInitialState: function() {
            return {
                attr: null,
                pristine: true
            };
        },
        getDefaultProps: function() {
			return {
				colOffset: 2,
				onAttrInit: this._onAttrInit
			};
		},
        componentWillReceiveProps: function(nextProps) {
            if(nextProps.validate === true) {
                // validate control
                this._validate(this._getValue(nextProps.value), function(validity) {
                    this.setState({
                        pristine: true,
                        validity: validity
                    });     
                }.bind(this));
            }
        },
        render: function() {
            if(this.state.attr === null) {
                // attribute not fetched yet
            	return molgenis.ui.Spinner();
            }
            
            var attr = this.state.attr;
            
            var lbl = attr.label;
            
            if(attr.nillable === false) {
                lbl += ' *';
            }
            
            // add validation error message
            var validate = this.state.pristine === false || this.props.validate === true;
            var errorMessageSpan = validate && this.state.valid === false ? span({className: 'help-block'}, strong({}, this.state.errorMessage)) : null;
            
            // determine success and error classes for control 
            var formGroupClasses = 'form-group';
            if(validate && this.state.valid === false) {
                formGroupClasses += ' has-error';
            }
                        
            var id = attr.name;
            
            var description = attr.description !== undefined ? FormControlDescriptionFactory({description: attr.description}) : undefined;
            var labelClasses = this.props.formLayout === 'horizontal' ? 'col-md-' + this.props.colOffset + ' control-label' : 'control-label';
            var labelElement = label({className: labelClasses, htmlFor: id}, lbl);
            
            var attributeControlProps = _.extend({}, this.props, {
                attr : attr,
                id : id,
                name : id,
                disabled: this.props.mode === 'view',
                focus: this.props.focus,
                formLayout : undefined,
                value: this._getValue(this.props.value),
                onValueChange : this._handleValueChange,
                onBlur : this._handleBlur
            });
            
            // allow editing readonly controls in create mode
            if(this.props.mode === 'create' && attr.readOnly === true) {
                _.extend(attributeControlProps, {readOnly: false, required: true});
            }
            // show hidden controls and create and edit form
            if((this.props.mode === 'create' || this.props.mode === 'edit') && attr.visible !== true) {
            	attributeControlProps.visible = true;
            }
            // show auto controls as readonly in edit mode
            if(this.props.mode === 'edit' && attr.auto === true) {
            	attributeControlProps.readOnly = true;
            }
            
            var control = molgenis.ui.AttributeControl(attributeControlProps);
            
            if(this.props.formLayout === 'horizontal') {
                return(
                    div({className: formGroupClasses},
                        labelElement,
                        div({className: 'col-md-' + (12 - this.props.colOffset)},
                        	control,
                            description,
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
        _onAttrInit: function() {
        	this._handleValueChange({value: this._getValue(this.props.value)});
        },
        _handleValueChange: function(e) {
            this._validate(this._getValue(e.value), function(validity) {
                this.setState({
                    value: this._getValue(e.value),
                    valid: validity.valid,
                    errorMessage: validity.errorMessage,
                    pristine: this.props.value === e.value // mark input as dirty
                });
                
                this.props.onValueChange({
                    attr: this.state.attr.name,
                    value: this._getValue(e.value),
                    valid: validity.valid,
                    errorMessage: validity.errorMessage
                });
                
                if(validity.valid === true && this._doPersistAttributeValue()) {
	                // persist changes for controls that do not have a blur event
	                switch(this.state.attr.fieldType) {
		                case 'BOOL':
		                case 'CATEGORICAL':
		                case 'CATEGORICAL_MREF':
		                case 'ENUM':
		                case 'MREF':
		                case 'XREF':
		                	this._persistAttributeValue(e.value);
		                	break;
		                default:
		                	break;
	                }
                }
            }.bind(this));
        },
        _handleBlur: function(e) {
            // only validate if control was touched
            if(this.state.pristine === true) {
                return;
            }
            
            this._validate(this._getValue(e.value), function(validity) {
            	if(validity.valid === true && this._doPersistAttributeValue()) {
            		this._persistAttributeValue(e.value);
                }
            	
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
            
            if(attr.nillable === false && type !== 'CATEGORICAL_MREF' && type !== 'MREF' && nullOrUndefinedValue) { // required value constraint
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

                api.getAsync(this.props.entity.hrefCollection, {q: {q: rules}}, function(data) {
                    if(data.total > 0) {
                        callback({valid: false, errorMessage: 'This ' + attr.label + ' already exists. It must be unique.'});
                    } else {
                        callback({valid: true, errorMessage: undefined});
                    }
                });
                return;
            }
            
            callback({valid: errorMessage === undefined, errorMessage: errorMessage});
        },
        _doPersistAttributeValue: function() {
        	return this.props.mode === 'edit' && this.props.saveOnBlur && !this.state.attr.readOnly;
        },
        _persistAttributeValue: function(value) {
        	// persist attribute
    		var val;
        	switch(this.state.attr.fieldType) {
        		case 'CATEGORICAL':
        		case 'XREF':
        			val = value !== null && value !== undefined ? value[this.state.attr.refEntity.idAttribute] : null;
        			break;
        		case 'CATEGORICAL_MREF':
        		case 'MREF':
        			val = _.map(value.items, function(item) {
        				return item[this.state.attr.refEntity.idAttribute];
        			}.bind(this));
        			break;
        		default:
        			val = value;
        			break;
        	}
        	
    		api.update(this.props.entityInstance.href + '/' + this.state.attr.name, val);
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
        _getValue: function(value) {
        	// workaround for required bool attribute with no value implying false value
        	// TODO replace with elegant solution
            if(value === undefined && this.state.attr.fieldType === 'BOOL' && !this.state.attr.nillable) {
            	return false;
            } else {
            	return value;
            }
        }
    });
    
    /**
     * @memberOf component
     */
    var FormControlDescription = React.createClass({
    	mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
        displayName: 'FormControlDescription',
        propTypes: {
        	description: React.PropTypes.string.isRequired
        },
        render: function () {
        	var text = this.props.description;
        	
        	var keyIdx = 0;
        	var idx = 0;
        	var DescriptionParts = [];
        	URI.withinString(text, function(url, start, end) {
        		if(start > idx) {
        			DescriptionParts.push(span({key: '' + keyIdx++}, text.substr(idx, start)));
        		}
        		DescriptionParts.push(a({href: url, key: '' + keyIdx++}, url));
        		
        		idx = end;
        		return url;
    		});
        	if(idx < text.length) {
        		DescriptionParts.push(span({key: '' + keyIdx++}, text.substr(idx)));
        	}
        	return span({className: 'help-block'}, DescriptionParts);
        }
    });
    var FormControlDescriptionFactory = React.createFactory(FormControlDescription);
    
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        FormControl: React.createFactory(FormControl)
    });
}(_, React, URI, molgenis));