/**
 * This module receives an array of job objects and renders ProgressBar
 * components for RUNNING jobs and lists jobs with other status in a table.
 *
 * @module Jobs
 *
 * @param jobs
 *            An array of job objects
 *
 * @exports Job class
 */
import React from "react";
import {RunningJobs} from "./RunningJobs";
import {Modal} from "../Modal";
import {JobDetails} from "./JobDetails";

var Jobs = React.createClass({
    displayName: 'Jobs',
    propTypes: {
        jobs: React.PropTypes.array.isRequired,
        onSelect: React.PropTypes.func.isRequired,
        selectedJobId: React.PropTypes.string
    },
    render: function () {
        const {jobs, onSelect, selectedJobId} = this.props;
        const selectedJob = jobs.find((job)=> job.identifier === selectedJobId);
        const runningJobs = jobs.filter(job => job.status === 'RUNNING');
        const finishedJobs = jobs.filter(job => job.status !== 'RUNNING');

        return <div>
            {runningJobs.length > 0 && <div className="row">
                <div className="col-md-12">
                    <RunningJobs jobs={runningJobs}
                                 onSelect={onSelect}/>
                </div>
            </div>}
            {finishedJobs.length > 0 && <div className="row">
                <div className="col-md-12">
                    {React.cloneElement(this.props.children, {
                        jobs: finishedJobs,
                        onSelect: onSelect
                    })}
                </div>
            </div>}
            {jobs.length === 0 && <div className="row">
                <div className="col-md-12"><p>No jobs found.</p></div>
            </div>}
            {selectedJob && <Modal onHide={() => onSelect(null)}
                                   title="Job details" show={true} size={"large"}
                                   footer={true}>
                <JobDetails job={selectedJob}/>
            </Modal>}
        </div>
    }
});

export {Jobs};
export default React.createFactory(Jobs);