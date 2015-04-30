/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, p = React.DOM.p, fieldset = React.DOM.fieldset, legend = React.DOM.legend;
    
    /**
	 * @memberOf component
	 */
	var FormControlGroup = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.AttributeLoaderMixin],
		displayName: 'FormControlGroup',
		propTypes: {
			entity: React.PropTypes.object,
			entityInstance : React.PropTypes.object,
			attr: React.PropTypes.object.isRequired,
			value: React.PropTypes.object,
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			colOffset: React.PropTypes.number,
			hideOptional: React.PropTypes.bool,
			saveOnBlur: React.PropTypes.bool,
			validate: React.PropTypes.bool,
			focus: React.PropTypes.bool,
			onValueChange: React.PropTypes.func.isRequired
		},
		getInitialState: function() {
			return {
				attr: null
			};
		},
		render: function() {
			if (this.state.attr === null) {
				// attribute not available yet
				return molgenis.ui.Spinner();
			}
			var attributes = this.state.attr.attributes;
			
			// add control for each attribute
			var foundFocusControl = false;
			var controls = [];
			var hasVisible = false;
			for(var i = 0; i < attributes.length; ++i) {
				var attr = attributes[i];
				var ControlFactory = attr.fieldType === 'COMPOUND' ? molgenis.ui.FormControlGroup : molgenis.ui.FormControl;
				var controlProps = {
					entity : this.props.entity,
					attr : attr,
					value: this.props.entityInstance ? this.props.entityInstance[attr.name] : undefined,
					entityInstance: this.props.entityInstance,
					mode : this.props.mode,
					formLayout : this.props.formLayout,
					colOffset: this.props.colOffset,
					saveOnBlur: this.props.saveOnBlur,
					validate: this.props.validate,
					onValueChange : this.props.onValueChange,
					key : '' + i
				};
				if(attr.fieldType === 'COMPOUND') {
					_.extend(controlProps, {hideOptional: this.props.hideOptional});
					hasVisible = true; // TODO implement better solution for nested compounds
				}
				
				// IE9 does not support the autofocus attribute, focus the first visible input manually
				if(!foundFocusControl && attr.visible === true) {
					_.extend(controlProps, {focus: true});
					foundFocusControl = true;
				}
				
				var Control = ControlFactory(controlProps);
				if(attr.nillable === true && this.props.hideOptional === true) {
					Control = div({className: 'hide'}, Control);
				} else {
					hasVisible = true;
				}
				controls.push(Control);
			}
			
			var Fieldset = fieldset({},
					legend({}, this.props.attr.label),
					p({}, this.props.attr.description),
					div({className: 'row'},
						div({className: 'col-md-offset-1 col-md-11'},
							controls
						)
					)
			);
			
			if(!hasVisible) {
				Fieldset = div({className: 'hide'}, Fieldset);
			}
			return Fieldset;
		}
	});
	
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        FormControlGroup: React.createFactory(FormControlGroup)
    });
}(_, React, molgenis));