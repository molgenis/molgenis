/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	/**
	 * @memberOf component
	 */
	var RadioGroup = React.createClass({
		displayName: 'RadioGroup',
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.GroupMixin],
		propTypes: {
			name: React.PropTypes.string.isRequired,
			layout: React.PropTypes.oneOf(['horizontal', 'vertical']),
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			options: React.PropTypes.arrayOf(React.PropTypes.shape({value: React.PropTypes.string, label: React.PropTypes.string})).isRequired,
			value: React.PropTypes.string,
			onValueChange: React.PropTypes.func.isRequired
		},
		getDefaultProps: function() {
			return {
				type: 'radio',
				layout: 'vertical'
			};
		},
		getInitialState: function() {
			return {
				value: this.props.value
			};
		},
		_handleChange: function(event) {			
			this.setState({value: event.value});
			this.props.onValueChange({value: event.value});
		},
		_isChecked: function(value) {
			return this.state.value === this._inputToValue(value.value);
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		RadioGroup: React.createFactory(RadioGroup)
	});
}(_, React, molgenis));