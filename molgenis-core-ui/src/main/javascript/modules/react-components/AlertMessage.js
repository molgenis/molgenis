/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div, span = React.DOM.span, button = React.DOM.button;
	
	/**
	 * @memberOf component
	 */
	var AlertMessage = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'AlertMessage',
		propTypes: {
			type: React.PropTypes.oneOf(['success', 'info', 'warning', 'danger']),
			message: React.PropTypes.string.isRequired,
			onDismiss: React.PropTypes.func,
		},
		render: function() {
			return (
				div({className: 'alert alert-' + this.props.type + ' alert-dismissible', role: 'alert'},
					this.props.onDismiss ? button({type: 'button', className: 'close', 'aria-label': 'Close', onClick: this.props.onDismiss}, // TODO use molgenis.ui.Button
						span({'aria-hidden': true,}, String.fromCharCode(215)) // &times;
					) : null,
					this.props.type === 'danger' ? molgenis.ui.Icon({name: 'exclamation-sign'}) : null,
					this.props.type === 'danger' ? ' ' + this.props.message : this.props.message  
				)
			);
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		AlertMessage: React.createFactory(AlertMessage)
	});
}(_, React, molgenis));