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
        animation: React.PropTypes.boolean,
        container: React.PropTypes.oneOfType(React.PropTypes.string, React.PropTypes.bool),
        delay: React.PropTypes.oneOfType([React.PropTypes.number, React.PropTypes.object]),
        html: React.PropTypes.bool,
        placement: React.PropTypes.oneOfType(React.PropTypes.oneOf('top', 'bottom', 'left', 'right', 'auto'), React.PropTypes.func),
        template: React.PropTypes.string,
        title: React.PropTypes.string,
        viewport: React.PropTypes.oneOfType(React.PropTypes.string, React.PropTypes.object, React.PropTypes.func)
    },
    getDefaultProps: function () {
        return {
            trigger: 'hover click',
            placement: 'bottom',
            container: 'body'
        }
    },
    componentDidMount: function () {
        const {value, popoverValue, ...otherOptions} = this.props;
        $(this.getDOMNode()).popover({
            content: popoverValue,
            ...otherOptions
        });
    },
    componentWillUnmount: function () {
        $(this.getDOMNode()).popover('destroy');
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