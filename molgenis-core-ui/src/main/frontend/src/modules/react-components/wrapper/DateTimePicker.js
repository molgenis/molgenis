import React from "react";
import DeepPureRenderMixin from "../mixin/DeepPureRenderMixin";
import "../css/wrapper/DateTimePicker.css";

var div = React.DOM.div, input = React.DOM.input, span = React.DOM.span;

/**
 * React component for Datepicker (http://eonasdan.github.io/bootstrap-datetimepicker/)
 *
 * @memberOf component.wrapper
 */
var DateTimePicker = React.createClass({ // FIXME should use controlled input
    displayName: 'DateTimePicker',
    mixins: [DeepPureRenderMixin],
    propTypes: {
        id: React.PropTypes.string,
        name: React.PropTypes.string,
        time: React.PropTypes.bool,
        placeholder: React.PropTypes.string,
        required: React.PropTypes.bool,
        disabled: React.PropTypes.bool,
        readonly: React.PropTypes.bool,
        focus: React.PropTypes.bool,
        value: React.PropTypes.string,
        onChange: React.PropTypes.func.isRequired
    },
    componentDidMount: function () {
        var props = this.props;

        var format = props.time === true ? 'YYYY-MM-DDTHH:mm:ssZZ' : 'YYYY-MM-DD';

        var $container = $(this.refs.datepicker.getDOMNode());
        $container.datetimepicker({
            format: format
        });

        $container.on('dp.change', function (event) {
            this._handleValueChange(event.date ? event.date.format(format) : null);
        }.bind(this));

        if (!this.props.required) {
            var $clearBtn = $(this.refs.clearbtn.getDOMNode());
            $clearBtn.on('click', function () {
                this._handleValueChange(null);
            }.bind(this));
        }

        this._focus();
    },
    componentWillUnmount: function () {
        var $container = $(this.refs.datepicker.getDOMNode());
        $container.datetimepicker('destroy');
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
        var placeholder = this.props.placeholder;
        var required = this.props.required;
        var disabled = this.props.disabled;
        var readOnly = this.props.readOnly;

        return (
            div({className: 'input-group date group-append', ref: 'datepicker'},
                input({
                    type: 'text',
                    className: 'form-control',
                    id: this.props.id,
                    name: this.props.name,
                    value: this.state.value,
                    placeholder: placeholder,
                    required: required,
                    disabled: disabled,
                    readOnly: readOnly,
                    onChange: this._handleChange,
                    ref: this.props.focus ? 'input' : undefined
                }), // FIXME use Input
                !required ? span({className: 'input-group-addon'},
                    span({className: 'glyphicon glyphicon-remove empty-date-input', ref: 'clearbtn'})
                ) : null,
                span({className: 'input-group-addon datepickerbutton'},
                    span({className: 'glyphicon glyphicon-calendar'})
                )
            )
        );
    },
    componentDidUpdate: function () {
        this._focus();
    },
    _handleChange: function (event) {
        this.setState({value: event.target.value});
    },
    _handleValueChange: function (value) {
        this.setState({value: value});
        this.props.onChange(value);
    },
    _focus: function () {
        if (this.props.focus) {
            this.refs.input.getDOMNode().focus();
        }
    }
});

export default React.createFactory(DateTimePicker);