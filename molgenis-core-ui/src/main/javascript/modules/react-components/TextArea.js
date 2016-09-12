import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";

var textarea = React.DOM.textarea;

/**
 * @memberOf component
 */
var TextArea = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'TextArea',
    propTypes: {
        id: React.PropTypes.string,
        name: React.PropTypes.string,
        placeholder: React.PropTypes.string,
        required: React.PropTypes.bool,
        disabled: React.PropTypes.bool,
        readOnly: React.PropTypes.bool,
        maxLength: React.PropTypes.number,
        value: React.PropTypes.string,
        onValueChange: React.PropTypes.func.isRequired,
        onBlur: React.PropTypes.func
    },
    getInitialState: function () {
        return {value: this.props.value};
    },
    componentWillReceiveProps: function (nextProps) {
        this.setState({
            value: nextProps.value
        });
    },
    render: function () {
        return textarea({
            className: 'form-control',
            id: this.props.id,
            name: this.props.name,
            placeholder: this.props.placeholder,
            required: this.props.required,
            disabled: this.props.disabled,
            readOnly: this.props.readOnly,
            maxLength: this.props.maxLength,
            value: this.state.value,
            onChange: this._handleChange,
            onBlur: this._handleBlur
        });
    },
    _handleChange: function (event) {
        this._handleChangeOrBlur(event, true, this.props.onValueChange);
    },
    _handleBlur: function (event) {
        if (this.props.onBlur) {
            this._handleChangeOrBlur(event, false, this.props.onBlur);
        }
    },
    _handleChangeOrBlur: function (event, updateState, callback) {
        var value = event.target.value;
        // apply constraint: maximum number of characters allowed in input
        if (this.props.maxLength) {
            value = value.substr(0, this.props.maxLength);
        }
        if (updateState) {
            this.setState({value: value});
        }
        value = value !== '' ? value : null;
        callback({value: value});
    }
});

export default React.createFactory(TextArea);