import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";

var div = React.DOM.div, img = React.DOM.img;

/**
 * @memberOf component
 */
var Spinner = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'Spinner',
    render: function () {
        return (
            div(null,
                img({src: require('./css/wrapper/select2-spinner.gif'), alt: 'Spinner', width: 16, height: 16})
            )
        );
    }
});

export {Spinner};

export default React.createFactory(Spinner);