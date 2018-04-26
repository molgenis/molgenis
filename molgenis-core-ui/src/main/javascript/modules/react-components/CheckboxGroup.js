import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import GroupMixin from "./mixin/GroupMixin";
import React from "react";
import "./css/Checkbox.css";

/**
 * @memberOf component
 */
var CheckboxGroup = React.createClass({
    mixins: [DeepPureRenderMixin, GroupMixin],
    displayName: 'CheckboxGroup',
    propTypes: {
        name: React.PropTypes.string,
        layout: React.PropTypes.oneOf(['horizontal', 'vertical']),
        required: React.PropTypes.bool,
        disabled: React.PropTypes.bool,
        readOnly: React.PropTypes.bool,
        selectAll: React.PropTypes.bool, // add select all and deselect all options for checkbox group
        options: React.PropTypes.arrayOf(React.PropTypes.shape({
            value: React.PropTypes.string,
            label: React.PropTypes.string
        })).isRequired,
        focus: React.PropTypes.bool,
        value: React.PropTypes.arrayOf(React.PropTypes.string),
        onValueChange: React.PropTypes.func.isRequired
    },
    getDefaultProps: function () {
        return {
            type: 'checkbox',
            layout: 'vertical',
            selectAll: true
        };
    },
    getInitialState: function () {
        return {
            value: this.props.value || []
        };
    },
    componentWillReceiveProps: function (nextProps) {
        this.setState({
            value: nextProps.value || []
        });
    },
    _handleChange: function (event) {
        var value = this._inputToValue(event.value);

        var values = this.state.value;
        if (event.checked) {
            values = values.concat(value);
        } else {
            values = values.slice(0);
            values.splice(values.indexOf(value), 1);
        }

        this.setState({value: values});
        this.props.onValueChange({value: values});
    },
    _isChecked: function (option) {
        return this.state.value && this.state.value.indexOf(this._inputToValue(option.value)) > -1;
    },
    _selectAll: function (e) {
        e.preventDefault(); // do not scroll to top of page

        var values = [];
        for (var i = 0; i < this.props.options.length; ++i) {
            values.push(this.props.options[i].value);
        }
        this.setState({value: values});
        this.props.onValueChange({value: values});
    },
    _deselectAll: function (e) {
        e.preventDefault(); // do not scroll to top of page

        this.setState({value: []});
        this.props.onValueChange({value: []});
    },
});

export default React.createFactory(CheckboxGroup);