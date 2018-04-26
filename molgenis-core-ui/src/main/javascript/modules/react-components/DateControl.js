import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import React from "react";
import DateTimePicker from "./wrapper/DateTimePicker";
/**
 * @memberOf component
 */
var DateControl = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'DateControl',
    propTypes: {
        name: React.PropTypes.string,
        time: React.PropTypes.bool,
        placeholder: React.PropTypes.string,
        required: React.PropTypes.bool,
        disabled: React.PropTypes.bool,
        focus: React.PropTypes.bool,
        value: React.PropTypes.string,
        onValueChange: React.PropTypes.func.isRequired
    },
    render: function () {
        return DateTimePicker({
            name: this.props.name,
            time: this.props.time,
            placeholder: this.props.placeholder,
            required: this.props.required,
            disabled: this.props.disabled,
            readOnly: this.props.readOnly,
            focus: this.props.focus,
            value: this.props.value,
            onChange: this._handleChange
        });
    },
    _handleChange: function (value) {
        this.props.onValueChange({value: value});
    }
});

export default React.createFactory(DateControl);