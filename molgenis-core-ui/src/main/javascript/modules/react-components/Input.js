import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import _ from "underscore";
import React from "react";

var input = React.DOM.input, div = React.DOM.div, span = React.DOM.span;

/**
 * @memberOf component
 */
var Input = React.createClass({
    displayName: 'Input',
    mixins: [DeepPureRenderMixin],
    propTypes: {
        type: React.PropTypes.string.isRequired,
        onValueChange: React.PropTypes.func.isRequired,
        id: React.PropTypes.string,
        name: React.PropTypes.string,
        placeholder: React.PropTypes.string,
        required: React.PropTypes.bool,
        disabled: React.PropTypes.bool,
        readOnly: React.PropTypes.bool,
        step: React.PropTypes.string,
        min: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
        max: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
        maxLength: React.PropTypes.number,
        focus: React.PropTypes.bool,
        value: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
        checked: React.PropTypes.bool,
        onBlur: React.PropTypes.func
    },
    getInitialState: function () {
        return this._isRadioOrCheckbox() ? {checked: this.props.checked} : {value: this.props.value};
    },
    componentDidMount: function () {
        this._focus();
    },
    componentWillReceiveProps: function (nextProps) {
        this.setState(this._isRadioOrCheckbox() ? {checked: nextProps.checked} : {value: nextProps.value});
    },
    render: function () {
        var props = this.props;

        var inputProps = {
            type: props.type,
            className: this._isRadioOrCheckbox() || this._isFile() ? undefined : 'form-control',
            id: props.id,
            name: props.name,
            placeholder: props.placeholder,
            required: props.required,
            disabled: props.disabled,
            readOnly: props.readOnly,
            step: props.step,
            min: props.min,
            max: props.max,
            maxLength: props.maxLength,
            value: this._isRadioOrCheckbox() ? this.props.value : this.state.value,
            checked: this._isRadioOrCheckbox() ? this.state.checked : undefined,
            onChange: this._handleChange,
            onBlur: this._handleBlur,
            ref: this.props.focus || this._isFile() ? 'input' : undefined
        };

        if (props.readOnly && this._isRadioOrCheckbox()) {
            // readonly attribute doesn't work on checkboxes and radio buttons:
            // http://stackoverflow.com/questions/155291/can-html-checkboxes-be-set-to-readonly
            _.extend(inputProps, {name: undefined, disabled: true});

            if (this.state.checked) {
                // for checked checkboxes/radio buttons submit the value of a hidden input,
                // disable visible input to display a readonly view to the user
                return div({},
                    input({
                        type: 'hidden',
                        name: props.name,
                        value: props.value
                    }),
                    input(inputProps)
                );
            } else {
                // values of unchecked checkboxes/radio buttons are never submitted:
                // http://www.w3.org/TR/html401/interact/forms.html#h-17.13.2
                return input(inputProps);
            }
        } else {
            if (this._isFile()) {
                if (inputProps.value && !this.state.setByUser) {
                    return (
                        div({className: 'input-group'},
                            span({className: 'input-group-btn'},
                                span({
                                        onClick: this._handleClearButtonClick,
                                        className: 'btn btn-primary',
                                        style: {position: 'relative', overflow: 'hidden'}
                                    }
                                    , 'Change'
                                )
                            ),
                            input(_.extend(inputProps, {
                                type: 'text',
                                className: 'form-control',
                                readOnly: true,
                                style: {backgroundColor: 'white !important', cursor: 'text !important'},
                                value: this.state.value ? this.state.value : null,
                                ref: 'fileTextInput'
                            }))
                        )
                    );
                }
                else {
                    // see http://www.abeautifulsite.net/whipping-file-inputs-into-shape-with-bootstrap-3/
                    return (
                        div({className: 'input-group'},
                            span({className: 'input-group-btn'},
                                span({
                                        className: 'btn btn-primary',
                                        style: {position: 'relative', overflow: 'hidden'}
                                    }, 'Browse...',
                                    input(_.extend(inputProps, {
                                        onChange: this._handleFileBrowseClick,
                                        style: {
                                            position: 'absolute',
                                            top: 0,
                                            right: 0,
                                            minWidth: '100%',
                                            minHeight: '100%',
                                            fontSize: 100,
                                            textAlign: 'right',
                                            opacity: 0,
                                            outline: 'none',
                                            background: 'white',
                                            cursor: 'inherit',
                                            display: 'block'
                                        }
                                    }))
                                )
                            ),
                            input({
                                type: 'text',
                                className: 'form-control',
                                readOnly: true,
                                style: {backgroundColor: 'white !important', cursor: 'text !important'},
                                value: this.state.value ? this.state.value.filename : null,
                                ref: 'fileTextInput'
                            })
                        )
                    );
                }
            }
            else {
                return input(inputProps);
            }
        }
    },
    _handleChange: function (event) {
        var value = event.target.value;
        var newState;
        if (this._isRadioOrCheckbox()) {
            newState = {checked: event.target.checked};
        } else {
            // apply constraint: maximum number of characters allowed in input
            if (this.props.maxLength) {
                value = value.substr(0, this.props.maxLength);
            }

            // number change event with invalid number value (e.g. '1abc') will trigger a change event with value '',
            // use input validation status to determine is the value was cleared or if we are dealing with an invalid value.
            // Note: this does not apply to IE9 which does not support the input number type and the validation API.
            if (this.props.type === 'number' && event.target.validity && event.target.validity.badInput === true) {
                value = this.state.value;
            }
            newState = {value: value};
        }
        this.setState(newState);
        this._handleChangeOrBlur(value, event.target.checked, this.props.onValueChange);
    },
    _handleBlur: function (event) {
        if (this.props.onBlur) {
            this._handleChangeOrBlur(event.target.value, event.target.checked, this.props.onBlur);
        }
    },
    _handleChangeOrBlur: function (value, checked, callback) {
        var valueEvent;
        if (this._isRadioOrCheckbox()) {
            valueEvent = {value: this._emptyValueToNull(value), checked: checked};
        } else {
            var val = this._emptyValueToNull(value);

            if (this.props.type === 'number' && val !== null) {
                val = parseFloat(val); // convert js string to js number
            }
            valueEvent = {value: val};
        }
        callback(valueEvent);
    },
    _handleFileBrowseClick: function () {
        var input = $(this.refs.input.getDOMNode()),
            value = input.val(),
            label = this._toFileLabel(value);
        $(this.refs.fileTextInput.getDOMNode()).val(label);

        this.setState({value: value, setByUser: true});
        this._handleChangeOrBlur(value, undefined, this.props.onValueChange);
    },
    _handleClearButtonClick: function () {
        this.state.value = null;
        this.state.setByUser = false;
        this._handleChangeOrBlur(undefined, undefined, this.props.onValueChange);
    },
    _toFileLabel: function (value) {
        return value.replace(/\\/g, '/').replace(/.*\//, '');
    },
    _isRadioOrCheckbox: function () {
        return this.props.type === 'radio' || this.props.type === 'checkbox';
    },
    _isFile: function () {
        return this.props.type === 'file';
    },
    _emptyValueToNull: function (value) {
        return value !== '' ? value : null;
    },
    _focus: function () {
        if (this.props.focus) {
            this.refs.input.getDOMNode().focus();
        }
    }
});

export {Input};
export default React.createFactory(Input);