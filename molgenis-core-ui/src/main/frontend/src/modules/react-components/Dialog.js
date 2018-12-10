import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import React from "react";
import Button from "./Button";
import Modal from "./Modal";

var div = React.DOM.div;

/**
 * @memberOf component
 */
var Dialog = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'Dialog',
    propTypes: {
        type: React.PropTypes.oneOf(['alert', 'confirm']),
        message: React.PropTypes.string.isRequired,
        onCancel: React.PropTypes.func,  // confirm dialogs
        onConfirm: React.PropTypes.func, // alert and confirm dialogs
    },
    render: function () {
        return Modal({title: this.props.message, show: true},
            div({className: 'row', style: {textAlign: 'right'}},
                div({className: 'col-md-12'},
                    this.props.type === 'confirm' ? Button({
                        text: 'Cancel',
                        onClick: this.props.onCancel
                    }, 'Cancel') : null,
                    Button({text: 'Ok', style: 'primary', css: {marginLeft: 5}, onClick: this.props.onConfirm}, 'Ok')
                )
            )
        );
    }
});

export default React.createFactory(Dialog);