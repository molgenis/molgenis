import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import _ from "underscore";

var span = React.DOM.span;

/**
 * @memberOf component
 */
var Icon = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'Icon',
    propTypes: {
        name: React.PropTypes.string.isRequired,
        onClick: React.PropTypes.func,
        style: React.PropTypes.object
    },
    render: function () {
        var style = this.props.onClick ? _.extend({cursor: 'pointer'}, this.props.style) : this.props.style;
        return (
            span({onClick: this.props.onClick, style: style},
                span({className: 'glyphicon glyphicon-' + this.props.name, 'aria-hidden': true}),
                span({className: 'sr-only'}, this.props.name)
            )
        );
    }
});

export default React.createFactory(Icon);