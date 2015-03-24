/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var span = React.DOM.span;
	
	/**
	 * @memberOf component
	 */
	var Icon = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'Icon',
		propTypes: {
			name: React.PropTypes.string.isRequired
		},
		render: function() {
			return span({className: 'glyphicon glyphicon-' + this.props.name});
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Icon: React.createFactory(Icon)
	});
}(_, React, molgenis));