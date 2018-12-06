import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import JQRangeSlider from "./wrapper/JQRangeSlider";

/**
 * Range slider control for number types
 *
 * @memberOf component
 */
var RangeSlider = React.createClass({ // FIXME support readOnly
    mixins: [DeepPureRenderMixin],
    displayName: 'RangeSlider',
    propTypes: {
        id: React.PropTypes.string,
        range: React.PropTypes.shape({
            min: React.PropTypes.number.isRequired,
            max: React.PropTypes.number.isRequired
        }).isRequired,
        value: React.PropTypes.arrayOf(React.PropTypes.number),
        step: React.PropTypes.string,
        disabled: React.PropTypes.bool,
        onValueChange: React.PropTypes.func.isRequired
    },
    render: function () {
        var range = this.props.range;
        var value = this.props.value;

        var fromValue = value && value[0] ? value[0] : range.min;
        var toValue = value && value[1] ? value[1] : range.max;
        var options = {
            symmetricPositionning: true,
            bounds: {min: range.min, max: range.max},
            defaultValues: {min: fromValue, max: toValue},
            step: this.props.step,
            type: 'number'
        };

        return JQRangeSlider({
            id: this.props.id,
            options: options,
            disabled: this.props.disabled,
            value: [fromValue, toValue],
            onChange: this._handleChange
        });
    },
    _handleChange: function (value) {
        this.props.onValueChange({value: value});
    }
});

export default React.createFactory(RangeSlider);