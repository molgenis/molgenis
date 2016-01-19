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
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.I18nStringsMixin],
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
		getInitialState: function() {
			return {
				i18nStrings : null //loaded from server
			};
    	},
		render: function() {
			if(this.state.i18nStrings === null) {
				return molgenis.ui.Spinner();
			}
			
			var options = [{value: 'true', label: this.state.i18nStrings.form_bool_true}, {value: 'false', label: this.state.i18nStrings.form_bool_false}];
			if(!this.props.required && !this.props.multiple) {
				options = options.concat({value: '', label: this.state.i18nStrings.form_bool_missing});
			}
			
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
			this.props.onValueChange({value: this._eventToBool(e)});
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
		_eventToBool: function(e) {
			if(this.props.multiple) {
				// do not use $.map since it removes null values
				if(e.value !== undefined) {
					var value = [];
					for(var i = 0; i < e.value.slice(0).length; ++i)
						value[i] = value[i] === 'true' ? true : (value[i] === 'false' ? false : value[i]);
				}
				return value;
			} else if(e.checked !== undefined) {
				return e.checked;
			} else {
				return e.value === 'true' ? true : (e.value === 'false' ? false : e.value);
			}
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		BoolControl: React.createFactory(BoolControl)
	});
}(_, React, molgenis));