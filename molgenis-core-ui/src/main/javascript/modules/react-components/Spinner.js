/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div, img = React.DOM.img;
	
	/**
	 * @memberOf component
	 */
	var Spinner = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'Spinner',
		render: function() {
			return (
				div(null,
					img({src: '/css/select2-spinner.gif', alt: 'Spinner', width: 16, height: 16})
				)
			);
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Spinner: React.createFactory(Spinner)
	});
}(_, React, molgenis));