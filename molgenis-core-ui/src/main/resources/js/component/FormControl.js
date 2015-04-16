/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, span = React.DOM.span, label = React.DOM.label, strong = React.DOM.strong;
    
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
            //validate: React.PropTypes.bool,
            errorMessage: React.PropTypes.string,
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
           // if(nextProps.validate === true) {
                // validate control
            //    this._validate(this._getValue(nextProps.value), function(validity) {
             //       this.setState({
              //          pristine: true,
               //         validity: validity
               //     });     
               // }.bind(this));
            //}
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
          //  var validate = this.state.pristine === false || this.props.validate === true;
            var errorMessageSpan = this.props.errorMessage ? span({className: 'help-block'}, strong({}, this.props.errorMessage)) : null;
            
            // determine success and error classes for control 
            var formGroupClasses = 'form-group';
            if(this.props.errorMessage) {
                formGroupClasses += ' has-error';
            }
                        
            var id = attr.name;
            
            var description = attr.description !== undefined ? span({className: 'help-block'}, attr.description) : undefined;
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
        	//this._handleValueChange({value: this._getValue(this.props.value)});
        },
        _handleValueChange: function(e) {
        	 this.props.onValueChange({
        		 attr: this.state.attr.name,
        		 value: this._getValue(e.value),
        	 });
        	
//            this._validate(this._getValue(e.value), function(validity) {
//                this.setState({
//                    value: this._getValue(e.value),
//                    valid: validity.valid,
//                    errorMessage: validity.errorMessage,
//                    pristine: this.props.value === e.value // mark input as dirty
//                });
//                
//                this.props.onValueChange({
//                    attr: this.state.attr.name,
//                    value: this._getValue(e.value),
//                    valid: validity.valid,
//                    errorMessage: validity.errorMessage
//                });
//                
//                if(validity.valid === true && this._doPersistAttributeValue()) {
//	                // persist changes for controls that do not have a blur event
//	                switch(this.state.attr.fieldType) {
//		                case 'BOOL':
//		                case 'CATEGORICAL':
//		                case 'CATEGORICAL_MREF':
//		                case 'ENUM':
//		                case 'MREF':
//		                case 'XREF':
//		                	this._persistAttributeValue(e.value);
//		                	break;
//		                default:
//		                	break;
//	                }
//                }
//            }.bind(this));
        },
        _handleBlur: function(e) {
            // only validate if control was touched
//            if(this.state.pristine === true) {
//                return;
//            }
//            
//            this._validate(this._getValue(e.value), function(validity) {
//            	if(validity.valid === true && this._doPersistAttributeValue()) {
//            		this._persistAttributeValue(e.value);
//                }
//            	
//            	this.setState({
//                    valid: validity.valid,
//                    errorMessage: validity.errorMessage
//                });
//            }.bind(this));
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
    
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        FormControl: React.createFactory(FormControl)
    });
}(_, React, molgenis));