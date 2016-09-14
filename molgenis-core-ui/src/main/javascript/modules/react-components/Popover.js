import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";

var span = React.DOM.span;

/**
 * @memberOf component
 */
var Popover = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'Popover',
    propTypes: {
        value: React.PropTypes.string.isRequired,
        popoverValue: React.PropTypes.string.isRequired,
    },
    componentDidMount: function () {
        var $container = $(this.refs.popover.getDOMNode());
        $container.popover({
            trigger: 'hover click',
            placement: 'bottom',
            container: 'body'
        });
    },
    componentWillUnmount: function () {
        var $container = $(this.refs.popover.getDOMNode());
        $container.popover('destroy');
    },
    render: function () {
        return span({
            'data-content': this.props.popoverValue,
            'data-toggle': 'popover',
            ref: 'popover'
        }, this.props.value);
    }
});

export default React.createFactory(Popover);