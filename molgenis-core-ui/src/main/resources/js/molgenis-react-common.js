/**
 * MOLGENIS react mixins
 * 
 * Dependencies:
 * - underscore-min.js
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";

	/**
	 * Only render components if their state or props changed
	 */
	molgenis.DeepPureRenderMixin = {
		shouldComponentUpdate: function(nextProps, nextState) {
			return !_.isEqual(this.state, nextState) || !_.isEqual(this.props, nextProps);
		}
	};
}($, window.top.molgenis = window.top.molgenis || {}));