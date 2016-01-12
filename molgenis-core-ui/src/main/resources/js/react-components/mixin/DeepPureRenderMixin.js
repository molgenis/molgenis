define(function(require, exports, module) {
	/**
	 * @module DeepPureRenderMixin
	 */

	"use strict";

	/**
	 * Only render components if their state or props changed
	 * 
	 * @memberOf DeepPureRenderMixin
	 */
	var DeepPureRenderMixin = {
		shouldComponentUpdate : function(nextProps, nextState) {
			return !_.isEqual(this.state, nextState) || !_.isEqual(this.props, nextProps);
		}
	};

	module.exports = DeepPureRenderMixin;
});