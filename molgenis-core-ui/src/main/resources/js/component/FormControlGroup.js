/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
    "use strict";

    var div = React.DOM.div, h4 = React.DOM.h4, p = React.DOM.p;
    
    /**
	 * @memberOf component
	 */
	var FormControlGroup = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'FormControlGroup',
		propTypes: {
			entity: React.PropTypes.object,
			attr: React.PropTypes.object.isRequired,
			value: React.PropTypes.object,
			mode: React.PropTypes.oneOf(['create', 'edit', 'view']),
			formLayout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			validate: React.PropTypes.bool,
			onValueChange: React.PropTypes.func.isRequired
		},
		render: function() {
			var attributes = this.props.attr.attributes;
			
			// add control for each attribute
			var controls = [];
			for(var i = 0; i < attributes.length; ++i) {
				var Control = attributes[i].fieldType === 'COMPOUND' ? molgenis.ui.FormControlGroup : molgenis.ui.FormControl;
				controls.push(Control({
					entity : this.props.entity,
					attr : attributes[i],
					value: this.props.value ? this.props.value[attributes[i].name] : undefined,
					mode : this.props.mode,
					formLayout : this.props.formLayout,
					validate: this.props.validate,
					onValueChange : this.props.onValueChange,
					key : '' + i
				}));
			}
			
			return (
				div({},
					h4({className: 'page-header'}, this.props.attr.label),
					p({}, this.props.attr.description),
					div({className: 'row'},
						div({className: 'col-md-offset-1 col-md-11'},
							controls
						)
					)
				)
			);
		}
	});
	
    // export component
    molgenis.ui = molgenis.ui || {};
    _.extend(molgenis.ui, {
        FormControlGroup: React.createFactory(FormControlGroup)
    });
}(_, React, molgenis));