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

import DeepPureRenderMixin from '../mixin/DeepPureRenderMixin';

var Jobs = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'Jobs',
	propTypes : {
		jobs : React.PropTypes.array.isRequired
	},
	render: function() {
		const {jobs} = this.props;	
		const runningJobs = jobs.filter(job => job.status === 'RUNNING');
		const finishedJobs = jobs.filter(job => job.status !== 'RUNNING');
		
		return <div>
			<div className="row">
				<div className="col-md-5">
					<RunningJobs jobs={runningJobs} />
				</div>
				<div className="col-md-7">
					<JobTable jobs={finishedJobs} />
				</div>
			</div>			
		</div>
	}
});

export { Jobs };
export default React.createFactory(Jobs);