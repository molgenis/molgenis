/* global _: false, React: false, URI: false, molgenis: true */
(function(_, React, URI, molgenis) {
    "use strict";

    var div = React.DOM.div, span = React.DOM.span, label = React.DOM.label, strong = React.DOM.strong, a = React.DOM.a;
    
    
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
            colOffset: React.PropTypes.number,
            focus: React.PropTypes.bool,
            value: React.PropTypes.any,
            errorMessage: React.PropTypes.string,
            onValueChange: React.PropTypes.func.isRequired,
            onBlur: React.PropTypes.func.isRequired
        },
        getInitialState: function() {
            return {
                attr: null,
                pristine : true
            };
        },
        getDefaultProps: function() {
			return {
				colOffset: 2,
			};
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
            var errorMessage = this.props.errorMessage;
            var showErrorMessage = (errorMessage !== undefined) && (errorMessage !== null);
            var errorMessageSpan = showErrorMessage ? span({className: 'help-block'}, strong({}, this.props.errorMessage)) : null;
            
            // determine success and error classes for control 
            var formGroupClasses = 'form-group';
            if (showErrorMessage) {
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

        _handleValueChange: function(e) {
        	this.setState({pristine: false});
        	
        	this.props.onValueChange({
        		attr: this.state.attr.name,
                value: this._getValue(e.value),
            });
        },
        
        _handleBlur: function(e) {
        	// only validate if control was touched
            if(this.state.pristine === true || this.props.onBlur === undefined) {
                return;
            }
            
            e.attr = this.state.attr;
            this.props.onBlur(e);
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
        		DescriptionParts.push(a({href: url, target: '_blank', key: '' + keyIdx++}, url));
        		
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
