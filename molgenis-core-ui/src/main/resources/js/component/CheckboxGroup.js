/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	/**
	 * @memberOf component
	 */
	var CheckboxGroup = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.GroupMixin],
		displayName: 'CheckboxGroup',
		propTypes: {
			name: React.PropTypes.string,
			layout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			options: React.PropTypes.arrayOf(React.PropTypes.shape({value: React.PropTypes.string, label: React.PropTypes.string})).isRequired,
			value: React.PropTypes.arrayOf(React.PropTypes.string),
			onValueChange: React.PropTypes.func.isRequired
		},
		getDefaultProps: function() {
			return {
				type: 'checkbox',
				layout: 'vertical'
			};
		},
		getInitialState: function() {
			return {
				value: this.props.value || []
			};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				value: nextProps.value || []
			});
		},
		_handleChange: function(event) {
			var value = this._inputToValue(event.value);
			
			var values = this.state.value;
			if(event.checked) {
				values = values.concat(value);
			} else {
				values = values.slice(0); 
				values.splice(values.indexOf(value), 1);
			}
			
			this.setState({value: values});
			this.props.onValueChange({value: values});
		},
		_isChecked: function(option) {
			return this.state.value && this.state.value.indexOf(this._inputToValue(option.value)) > -1;
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		CheckboxGroup: React.createFactory(CheckboxGroup)
	});
}(_, React, molgenis));