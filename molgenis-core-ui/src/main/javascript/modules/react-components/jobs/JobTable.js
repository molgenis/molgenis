/**
 * Renders a Table with information for each job that is passed down from its
 * parent component
 *
 * @module JobTable
 *
 * @param jobs
 *            An array of job objects with status other then RUNNING
 *
 * @exports JobTable class
 */
import React from "react";
import moment from "moment";
import twix from 'twix';
import "moment-duration-format";
import {Button} from "../Button";

var JobTable = React.createClass({
    displayName: 'JobTable',
    propTypes: {
        jobs: React.PropTypes.array.isRequired,
        onSelect: React.PropTypes.func,
        customColumns: React.PropTypes.array
    },
    render: function () {
        const {jobs, customColumns} = this.props;
        return <div className="panel panel-primary">
            <div className="panel-heading">Finished Jobs</div>
            <div className="panel-body" style={{overflowX: 'auto'}}>
                <table className="table table-striped">
                    <thead>
                    <th></th>
                    <th>Status</th>
                    <th>When</th>
                    <th>Duration</th>
                    {customColumns && customColumns.map(cc => <th>{cc.th}</th>)}
                    <th>Result</th>
                    </thead>
                    <tbody>
                    {jobs.map((job, index) => <tr key={job.identifier}>
                        <td><Button icon="search" style="info" size="xsmall"
                                    onClick={() => this.props.onSelect(job.identifier)}/></td>
                        <td>{this._renderStatus(job.status)}</td>
                        <td>{this._getTwix(job)}</td>
                        <td>{this._getDuration(job)}</td>
                        {customColumns && customColumns.map(cc => <td>{cc.td(job)}</td>)}
                        <td>{job.resultUrl && <a href={job.resultUrl}>Go to result</a>}</td>
                    </tr>)}
                    </tbody>
                </table>
            </div>
        </div>
    },
    _renderStatus: function (status) {
        switch (status) {
            case 'FAILED':
                return <span className="label label-warning">Failed</span>
            case 'SUCCESS':
                return <span className="label label-success">Success</span>
            case 'PENDING':
                return <span className="label label-primary">Pending</span>
            case 'CANCELED':
                return <span className="label label-default">Canceled</span>
            case 'RUNNING':
                return <span className="label label-info">Running</span>
        }
    },
    _getTwix: function (job) {
        const startDate = moment(job.startDate);
        const endDate = moment(job.endDate);
        return startDate.twix(endDate).format() + " (" + endDate.fromNow() + ")";
    },
    _getDuration: function (job) {
        const startDate = moment(job.startDate);
        const endDate = moment(job.endDate);
        return moment.duration(endDate.diff(startDate), 'milliseconds').format("h[h], m[m], s[s]");
    }
});

export {JobTable};
export default React.createFactory(JobTable);