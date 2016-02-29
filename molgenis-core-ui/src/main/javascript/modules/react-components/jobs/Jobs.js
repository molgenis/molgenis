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
import React from 'react';
import { RunningJobs } from './RunningJobs';
import { JobTable } from './JobTable';
import { JobModal } from './JobModal';

var Jobs = React.createClass({
    displayName: 'Jobs',
    propTypes: {
        jobs: React.PropTypes.array.isRequired
    },
    getInitialState: function () {
        return {selectedJobID: null};
    },
    render: function () {
        const {jobs} = this.props;
        const {selectedJobID} = this.state;
        let selectedJob = jobs.find((job)=> job.identifier === selectedJobID);
        const runningJobs = jobs.filter(job => job.status === 'RUNNING');
        const finishedJobs = jobs.filter(job => job.status !== 'RUNNING');

        return <div>
            {runningJobs.length > 0 && <div className="row">
                <div className="col-md-12">
                    <RunningJobs jobs={runningJobs}
                                 onSelect={(id) => this.setState({selectedJobID:id})}/>
                </div>
            </div>}
            {finishedJobs.length > 0 && <div className="row">
                <div className="col-md-12">
                    <JobTable jobs={finishedJobs}
                              onSelect={(id) => this.setState({selectedJobID:id})}/>
                </div>
            </div>}
            {jobs.length === 0 && <div className="row">
                <div className="col-md-12"><p>No jobs found.</p></div>
            </div>}
            {selectedJob && <JobModal job={selectedJob}
                                      onClose={() => this.setState({selectedJobID: null})}/>}
        </div>
    }
});

export { Jobs };
export default React.createFactory(Jobs);