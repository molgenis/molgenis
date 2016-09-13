import React from "react";
import ReactLayeredComponentMixin from "./mixin/ReactLayeredComponentMixin";
import Dialog from "./Dialog";

var ConfirmClick = React.createClass({
    mixins: [ReactLayeredComponentMixin],
    displayName: 'ConfirmClick',
    propTypes: {
        onClick: React.PropTypes.func,
        confirmMessage: React.PropTypes.string.isRequired
    },
    getInitialState: function () {
        return {
            dialog: false
        };
    },
    getDefaultProps: function () {
        return {
            onClick: function () {
            }
        };
    },
    render: function () {
        return React.cloneElement(this.props.children,
            {onClick: this._showDialog});
    },
    renderLayer: function () {
        return this.state.dialog ? Dialog({
            type: 'confirm',
            message: this.props.confirmMessage,
            onCancel: this._hideDialog,
            onConfirm: this._onConfirm
        }) : null;
    },
    _showDialog: function () {
        this.setState({
            dialog: true
        });
    },
    _hideDialog: function () {
        this.setState({
            dialog: false
        });
    },
    _onConfirm: function () {
        this._hideDialog();
        this.props.onClick();
    }
});

export {ConfirmClick}
export default React.createFactory(ConfirmClick);