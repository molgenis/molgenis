import React from "react";
import DeepPureRenderMixin from "../mixin/DeepPureRenderMixin";
import $ from "jquery";
import _ from "underscore";
import jqueryForm from "jquery-form";

var form = React.DOM.form;

/**
 * React component for jQuery Form Plugin (http://malsup.com/jquery/form/)
 *
 * @memberOf component.wrapper
 */
var JQueryForm = React.createClass({
    displayName: 'JQueryForm',
    mixins: [DeepPureRenderMixin],
    propTypes: {
        className: React.PropTypes.string,
        action: React.PropTypes.string.isRequired,
        method: React.PropTypes.string,
        noValidate: React.PropTypes.bool,
        beforeSubmit: React.PropTypes.func,
        success: React.PropTypes.func,
        error: React.PropTypes.func
    },
    submit: function () {
        var $form = $(this.refs.form.getDOMNode());
        $form.ajaxSubmit({
            resetForm: false,
            beforeSubmit: this.props.beforeSubmit,
            success: this.props.success,
            error: this.props.error
        });
    },
    componentDidMount: function () {
        this.refs.form.getDOMNode().submit = this.submit;
    },
    componentWillUnmount: function () {
        var $form = $(this.refs.form.getDOMNode());
        $form.ajaxFormUnbind();
    },
    render: function () {
        return (
            form(_.extend({}, this.props, {ref: 'form'}),
                this.props.children
            )
        );
    }
});

export default React.createFactory(JQueryForm);