import _ from 'underscore';
/**
 * @module DeepPureRenderMixin
 */

/**
 * Only render components if their state or props changed
 * 
 * @memberOf DeepPureRenderMixin
 */
const DeepPureRenderMixin = {
	shouldComponentUpdate : function(nextProps, nextState) {
		return !_.isEqual(this.state, nextState) || !_.isEqual(this.props, nextProps);
	}
};

export default DeepPureRenderMixin;
