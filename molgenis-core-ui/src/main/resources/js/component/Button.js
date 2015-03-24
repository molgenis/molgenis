/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var button = React.DOM.button;
	
	/**
	 * @memberOf component
	 */
	var Button = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'Button',
		propTypes: {
			id : React.PropTypes.string,
			type: React.PropTypes.string,
			text: React.PropTypes.string,
			icon: React.PropTypes.string,
			name: React.PropTypes.string,
			value: React.PropTypes.string,
			disabled : React.PropTypes.bool,
			onClick: React.PropTypes.func,
		},
		getDefaultProps: function() {
			return {
				type: 'button'
			};
		},
		render: function() {
			var buttonProps = {
				className: 'btn btn-default',
				id : this.props.id,
				type : this.props.type,
				name: this.props.name,
				placeholder : this.props.placeholder,
				required : this.props.required,
				disabled : this.props.disabled,
				readOnly : this.props.readOnly,
				mode: this.props.language,
				value : this.props.value,
				onClick : this.props.onClick
			};
			
			return (
				button(buttonProps,
					this.props.icon ? molgenis.ui.Icon({name: this.props.icon}) : null,
					this.props.text ? (this.props.icon ? ' ' + this.props.text : this.props.text) : null
				)
			);
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Button: React.createFactory(Button)
	});
}(_, React, molgenis));