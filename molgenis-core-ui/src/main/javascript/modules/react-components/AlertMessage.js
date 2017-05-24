import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import Icon from "./Icon";

var div = React.DOM.div, span = React.DOM.span, button = React.DOM.button, br = React.DOM.br;

function intersperse(arr, sep) {
    return arr.reduce((a, v) => [...a, v, sep], []).slice(0, -1)
}

/**
 * @memberOf component
 */
var AlertMessage = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'AlertMessage',
    propTypes: {
        type: React.PropTypes.oneOf(['success', 'info', 'warning', 'danger']),
        message: React.PropTypes.string.isRequired,
        onDismiss: React.PropTypes.func,
    },
    render: function () {
        let messages = null;
        if (this.props.message) {
            messages = this.props.message.split('\n');
        }
        return (
            div({className: 'alert alert-' + this.props.type + ' alert-dismissible', role: 'alert'},
                this.props.onDismiss ? button({
                        type: 'button',
                        className: 'close',
                        'aria-label': 'Close',
                        onClick: this.props.onDismiss
                    }, // TODO use Button
                    span({'aria-hidden': true,}, String.fromCharCode(215)) // &times;
                ) : null,
                this.props.type === 'danger' ? span(null, Icon({name: 'exclamation-sign'}), ' ') : null,
                ...intersperse(messages, br(null))
            )
        );
    }
});

export default React.createFactory(AlertMessage);