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

var Jobs = React.createClass({
	displayName: 'Jobs',
	propTypes : {
		jobs : React.PropTypes.array.isRequired
	},
	render: function() {
		const {jobs} = this.props;	
		const runningJobs = jobs.filter(job => job.status === 'RUNNING');
		const finishedJobs = jobs.filter(job => job.status !== 'RUNNING');
		
		return <div>
		{runningJobs.length > 0 && <div className="row">
			<div className="col-md-12">
				<RunningJobs jobs={runningJobs} />
			</div>
		</div>}
		<div className="row">
			<div className="col-md-12">
				<JobTable jobs={finishedJobs} />
			</div>
		</div>
		</div>
	}
});

export { Jobs };
export default React.createFactory(Jobs);