/**
 * This react component renders a progress bar.
 *
 * @module ProgressBar
 *
 * @param progressPct
 *            The percentage that represents the shown width of the bar
 * @param progressMessage
 *            Message shown within the bar
 * @param status
 *            The status of the job, will affect bar color
 * @param active
 *            Whether the bar should be animated or not
 *
 * @exports ProgressBarClass, ProgressBar factory
 */

import React from "react";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";

var div = React.DOM.div;

var ProgressBar = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'ProgressBar',
    propTypes: {
        progressPct: React.PropTypes.number.isRequired,
        progressMessage: React.PropTypes.string.isRequired,
        status: React.PropTypes.oneOf(['info', 'danger', 'success', 'warning', 'primary']).isRequired,
        active: React.PropTypes.bool
    },
    render: function () {
        const {status, active, progressPct, progressMessage} = this.props;
        return <div className="progress background-lightgrey">
            <div className={'progress-bar progress-bar-' + status + (active ? ' progress-bar-striped active' : '')}
                 role="progressbar" style={{minWidth: '2em', 'width': progressPct + '%'}}>
                {progressMessage}
            </div>
        </div>
    }
});

export {ProgressBar};
export default React.createFactory(ProgressBar);