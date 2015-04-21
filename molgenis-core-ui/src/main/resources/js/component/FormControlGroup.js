/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, p = React.DOM.p;
    
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
			for(var i = 0; i < attributes.length; ++i) {
				var attr = attributes[i];
				var Control = attr.fieldType === 'COMPOUND' ? molgenis.ui.FormControlGroup : molgenis.ui.FormControl;
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
				
				// IE9 does not support the autofocus attribute, focus the first visible input manually
				if(!foundFocusControl && attr.visible === true) {
					_.extend(controlProps, {focus: true});
					foundFocusControl = true;
				}
				controls.push(Control(controlProps));
			}
			
			return (
//					div({className: 'panel panel-default'},
//						div({className: 'panel-body'},
							React.DOM.fieldset({},
									React.DOM.legend({}, this.props.attr.label),
									p({}, this.props.attr.description),
									div({className: 'row'},
										div({className: 'col-md-offset-1 col-md-11'},
											controls
										)
									)
							)
//						)
//					)
			);
		}
	});
	
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        FormControlGroup: React.createFactory(FormControlGroup)
    });
}(_, React, molgenis));