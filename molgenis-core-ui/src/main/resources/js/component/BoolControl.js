/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div, label = React.DOM.label;
	
	/**
	 * Input control for BOOL type with checkbox or radio buttons
	 * 
	 * @memberOf component
	 */
	var BoolControl = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'BoolControl',
		propTypes: {
			id: React.PropTypes.string,
			name: React.PropTypes.string,
			label: React.PropTypes.string,
			layout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			type: React.PropTypes.oneOf(['single', 'group']),
			multiple: React.PropTypes.bool,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			focus: React.PropTypes.bool,
			value: React.PropTypes.oneOfType([React.PropTypes.bool, React.PropTypes.array]),
			onValueChange: React.PropTypes.func.isRequired
		},
		getDefaultProps: function() {
			return {
				type: 'single',
				layout: 'horizontal',
				required: true
			};
		},
		render: function() {
			var options = [{value: 'true', label: 'Yes'}, {value: 'false', label: 'No'}];
			var Element = this.props.multiple ? molgenis.ui.CheckboxGroup : molgenis.ui.RadioGroup;
			return Element({
				id: this.props.id,
				name: this.props.name,
				options : options,
				required : this.props.required,
				disabled : this.props.disabled,
				readOnly: this.props.readOnly,
				layout : this.props.layout,
				focus: this.props.focus,
				value : this._boolToString(this.props.value),
				onValueChange : this._handleValueChange
			});
		},
		_handleValueChange: function(e) {
			this.props.onValueChange({value: this._stringToBool(e.value)});
		},
		_boolToString: function(value) {
			if(this.props.multiple) {
				// do not use $.map since it removes null values
				if(value !== undefined) {
					value = value.slice(0);
					for(var i = 0; i < value.length; ++i)
						value[i] = value[i] === true ? 'true' : (value[i] === false ? 'false' : value[i]);
				}
				return value;
			} else {
				return value === true ? 'true' : (value === false ? 'false' : value);
			}
		},
		_stringToBool: function(value) {
			if(this.props.multiple) {
				// do not use $.map since it removes null values
				if(value !== undefined) {
					value = value.slice(0);
					for(var i = 0; i < value.length; ++i)
						value[i] = value[i] === 'true' ? true : (value[i] === 'false' ? false : value[i]);
				}
				return value;
			} else {
				return value === 'true' ? true : (value === 'false' ? false : value);
			}
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		BoolControl: React.createFactory(BoolControl)
	});
}(_, React, molgenis));