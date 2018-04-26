import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import GroupMixin from "./mixin/GroupMixin";
import "./css/Radio.css";

/**
 * @memberOf component
 */
var RadioGroup = React.createClass({
    displayName: 'RadioGroup',
    mixins: [DeepPureRenderMixin, GroupMixin],
    propTypes: {
        name: React.PropTypes.string.isRequired,
        layout: React.PropTypes.oneOf(['horizontal', 'vertical']),
        required: React.PropTypes.bool,
        disabled: React.PropTypes.bool,
        readOnly: React.PropTypes.bool,
        options: React.PropTypes.arrayOf(React.PropTypes.shape({
            value: React.PropTypes.string,
            label: React.PropTypes.string
        })).isRequired,
        focus: React.PropTypes.bool,
        value: React.PropTypes.string,
        onValueChange: React.PropTypes.func.isRequired
    },
    getDefaultProps: function () {
        return {
            type: 'radio',
            layout: 'vertical'
        };
    },
    getInitialState: function () {
        return {
            value: this.props.value
        };
    },
    componentWillReceiveProps: function (nextProps) {
        if (this.state.value !== nextProps.value) {
            this.setState({value: nextProps.value});
        }
    },
    _handleChange: function (event) {
        this.setState({value: event.value});
        this.props.onValueChange({value: event.value});
    },
    _isChecked: function (option) {
        var value = this.state.value === undefined && !this.props.required ? null : this.state.value;
        return value === this._inputToValue(option.value);
    }
});

export {RadioGroup};
export default React.createFactory(RadioGroup);