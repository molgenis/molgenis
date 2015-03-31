/* global _: false, molgenis: true */
(function(_, molgenis) {
	"use strict";

	/**
	 * Only render components if their state or props changed
	 * 
	 * @memberOf component.mixin
	 */
	var DeepPureRenderMixin = {
		shouldComponentUpdate: function(nextProps, nextState) {
			return !_.isEqual(this.state, nextState) || !_.isEqual(this.props, nextProps);
		}
	};
	
	// export component
	molgenis.ui = molgenis.ui || {};
	molgenis.ui.mixin = molgenis.ui.mixin || {};
	_.extend(molgenis.ui.mixin, {
		DeepPureRenderMixin: DeepPureRenderMixin
	});
}(_, molgenis));