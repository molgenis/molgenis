/**
 * Renders a ProgressBar component for each job passed down from its parent
 * component
 *
 * @module RunningJobs
 *
 * @param jobs
 *            An array of job objects with status RUNNING
 *
 * @exports RunningJobs class
 */
import React from "react";
import {Job} from "./Job";
import DeepPureRenderMixin from "../mixin/DeepPureRenderMixin";

var RunningJobs = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'RunningJobs',
    propTypes: {
        jobs: React.PropTypes.array.isRequired,
        onSelect: React.PropTypes.func
    },
    render: function () {
        const {jobs, onSelect} = this.props;
        return <div className="panel panel-primary">
            <div className="panel-heading">Running Jobs</div>
            <div className="panel-body">
                {jobs.map(function (job) {
                    return <Job job={job}
                                key={job.identifier}
                                onClick={() => onSelect(job.identifier)}/>;
                })}
            </div>
        </div>;
    }
});

export {RunningJobs};
export default React.createFactory(RunningJobs);