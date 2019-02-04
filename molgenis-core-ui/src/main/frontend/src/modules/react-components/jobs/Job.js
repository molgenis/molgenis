/**
 * The Job component translates information from the Job entity to a visual
 * representation of the progress of that job, i.e a progress bar
 *
 * @module Job
 *
 * @param job
 *            A key value object containing all the details of a job including
 *            its status and progress message
 *
 * @exports Job class
 */

import React from "react";
import {ProgressBar} from "../ProgressBar";
import DeepPureRenderMixin from "../mixin/DeepPureRenderMixin";

var Job = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'Job',
    propTypes: {
        job: React.PropTypes.object.isRequired,
        onClick: React.PropTypes.func
    },
    getInitialState: () => ({
        showLog: false
    }),
    render: function () {
        const {job, onClick} = this.props;
        const {showLog} = this.state;
        return <div>
            <p>{job.type} job</p>
            <div>
                {job.progressMessage}
                <ProgressBar
                    progressMessage={this._formatProgressMessage()}
                    progressPct={job.progressMax !== undefined ? this._getProgressPct() : 100}
                    status={this._getCssClass() || 'primary'}
                    active={this._isActive()}
                />
            </div>
            {job && showLog && <div>
                <pre>{job.log}</pre>
            </div>}
            <div className="btn-group" role="group">
                {onClick && <button type="button" className="btn btn-default"
                                    onClick={onClick}>Show details</button>}
                <button type="button" className="btn btn-default"
                        onClick={this._showLogClick}>{showLog ? 'Hide' : 'Show'} log
                </button>
                {job.resultUrl &&
                <a className="btn btn-default" role="button"
                   href={job.resultUrl}>Go to result</a>}
            </div>
        </div>
    },
    _showLogClick: function () {
        this.setState({showLog: !this.state.showLog})
    },
    _getCssClass: function () {
        let cssTable = {
            'PENDING': 'info',
            'RUNNING': 'primary',
            'SUCCESS': 'success',
            'FAILED': 'danger',
            'CANCELED': 'warning',
        }
        return cssTable[this.props.job.status];
    },
    _isActive: function () {
        let activeTable = {
            'PENDING': true,
            'RUNNING': true,
            'SUCCESS': false,
            'FAILED': false
        }
        return activeTable[this.props.job.status];
    },
    _getProgressPct: function () {
        let progressInt = this.props.job.progressInt;
        let progressMax = this.props.job.progressMax;
        let calculatedWidth = progressInt / progressMax * 100;
        return calculatedWidth;
    },
    _formatProgressMessage: function () {
        const {progressInt, progressMax, status} = this.props.job;
        if (progressInt === undefined) {
            return status;
        }
        if (progressMax === undefined) {
            return "" + progressInt
        } else {
            return progressInt + "/" + progressMax
        }
    }
});

export {Job};
export default React.createFactory(Job);