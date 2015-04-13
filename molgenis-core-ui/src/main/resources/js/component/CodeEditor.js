/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	/**
	 * @memberOf component
	 */
	var CodeEditor = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'CodeEditor',
		propTypes: {
			id : React.PropTypes.string,
			name: React.PropTypes.string,
			placeholder : React.PropTypes.string,
			required : React.PropTypes.bool,
			disabled : React.PropTypes.bool,
			readOnly : React.PropTypes.bool,
			mode: React.PropTypes.string,
			value : React.PropTypes.string,
			onValueChange : React.PropTypes.func.isRequired
		},
		render: function() {
			return molgenis.ui.wrapper.Ace({
				id : this.props.id,
				name: this.props.name,
				placeholder : this.props.placeholder,
				required : this.props.required,
				disabled : this.props.disabled,
				readOnly : this.props.readOnly,
				mode: this.props.language,
				value : this.props.value,
				onChange : this._handleChange
			});
		},
		_handleChange: function(value) {
			this.props.onValueChange({value: value !== '' ? value : null});
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		CodeEditor: React.createFactory(CodeEditor)
	});
}(_, React, molgenis));