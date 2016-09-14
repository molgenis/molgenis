import React from "react";
import DeepPureRenderMixin from "../mixin/DeepPureRenderMixin";
import $ from "jquery";
import Button from "../Button";
import _ from "underscore";
import "../css/wrapper/select2.css";

var input = React.DOM.input, span = React.DOM.span, div = React.DOM.div;

/**
 * React component for select box replacement Select2 (http://select2.github.io/)
 *
 * @memberOf component.wrapper
 */
var Select2 = React.createClass({
    displayName: 'Select2',
    mixins: [DeepPureRenderMixin],
    propTypes: {
        options: React.PropTypes.object,
        readOnly: React.PropTypes.bool,
        disabled: React.PropTypes.bool,
        focus: React.PropTypes.bool,
        addonBtn: React.PropTypes.bool,
        addonBtnIcon: React.PropTypes.string,
        addonBtnTitle: React.PropTypes.string,
        onAddonBtnClick: React.PropTypes.func,
        value: React.PropTypes.oneOfType([React.PropTypes.object, React.PropTypes.array]),
        onChange: React.PropTypes.func.isRequired
    },
    getDefaultProps: function () {
        return {
            addonBtn: false,
            addonBtnIcon: 'plus'
        };
    },
    getInitialState: function () {
        return {value: this.props.value};
    },
    componentWillReceiveProps: function (nextProps) {
        if (!_.isEqual(this.props.options, nextProps.options)) {
            this._destroySelect2();
            this._createSelect2(nextProps.options);
        }
        this.setState({value: nextProps.value});
    },
    componentDidMount: function () {
        this._createSelect2(this.props.options);
        this._updateSelect2();
    },
    componentWillUnmount: function () {
        this._destroySelect2();
    },
    render: function () {
        var inputControl = input({
            type: 'hidden', name: this.props.name, ref: 'select2', onChange: function () {
            }
        }); // empty onChange callback to suppress React warning
        if (this.props.addonBtn) {
            return (
                div({className: 'input-group select2-bootstrap-append'},
                    inputControl,
                    span({className: 'input-group-btn'},
                        Button({
                            icon: this.props.addonBtnIcon,
                            title: this.props.addonBtnTitle,
                            disabled: this.props.readOnly || this.props.disabled,
                            onClick: this.props.onAddonBtnClick
                        })
                    )
                )
            );
        } else {
            return inputControl;
        }
    },
    componentDidUpdate: function () {
        if (this.isMounted()) {
            this._updateSelect2();
        }
    },
    _handleChange: function (value) {
        this.setState({value: value});
        this.props.onChange(value);
    },
    _createSelect2: function (options) {
        var $container = $(this.refs.select2.getDOMNode());

        // create select2
        $container.select2($.extend({
            containerCssClass: 'form-control',
            placeholder: ' ', // cannot be an empty string
            width: '100%'
        }, options));

        $container.on('change', function () {
            this._handleChange($container.select2('data'));
        }.bind(this));
    },
    _updateSelect2: function () {
        var $container = $(this.refs.select2.getDOMNode());
        $container.select2('data', this.state.value);
        $container.select2('enable', !this.props.disabled);
        $container.select2('readonly', this.props.readOnly);
        if (this.props.focus) {
            $container.select2('focus');
        }
    },
    _destroySelect2: function () {
        var $container = $(this.refs.select2.getDOMNode());
        $container.off();
        $container.select2('destroy');
    }
});

export default React.createFactory(Select2);