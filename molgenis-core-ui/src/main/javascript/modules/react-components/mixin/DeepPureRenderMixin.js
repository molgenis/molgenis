import _ from "underscore";

/**
 * Only render components if their state or props changed
 *
 * @memberOf component.mixin
 */
const DeepPureRenderMixin = {
    shouldComponentUpdate: function (nextProps, nextState) {
        return !_.isEqual(this.state, nextState) || !_.isEqual(this.props, nextProps);
    }
};

export default DeepPureRenderMixin;