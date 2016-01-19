/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div;
	
	/**
	 * @memberOf component
	 */
	var Dialog = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'Dialog',
		propTypes: {
			type: React.PropTypes.oneOf(['alert', 'confirm']),
			message: React.PropTypes.string.isRequired,
			onCancel: React.PropTypes.func,  // confirm dialogs
			onConfirm: React.PropTypes.func, // alert and confirm dialogs
		},
		render: function() {
			return molgenis.ui.Modal({title: this.props.message, show: true},
				div({className: 'row', style: {textAlign: 'right'}},
					div({className: 'col-md-12'},
						this.props.type === 'confirm' ? molgenis.ui.Button({text: 'Cancel', onClick: this.props.onCancel}, 'Cancel') : null,
						molgenis.ui.Button({text: 'Ok', style: 'primary', css: {marginLeft: 5}, onClick: this.props.onConfirm}, 'Ok')
					)
				)
			);
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Dialog: React.createFactory(Dialog)
	});
}(_, React, molgenis));