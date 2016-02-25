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
import React from 'react';
import DeepPureRenderMixin from '../mixin/DeepPureRenderMixin'; 
import moment from 'moment';
import twix from 'twix';

var JobTable = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'JobTable',
	propTypes: {
		jobs: React.PropTypes.array.isRequired
	},
	render: function() {
		const {jobs} = this.props;
		return 	<div className="panel panel-primary">
			<div className="panel-heading">Finished Jobs</div>
			<div className="panel-body">
				<table className="table table-striped">
					<thead>
						<th>Status</th>
						<th>Runtime</th>
						<th>Type</th>
						<th>Message</th>
						<th>When</th>
						<th>Link to entity</th>
					</thead>
					<tbody>
					{jobs.map((job) => <tr key={job.identifier}>
						<td>{job.status}</td>
						<td>{this._getTwix(job).length('seconds')}s</td>
						<td>{job.type}</td>
						<td>{job.progressMessage}</td>
						<td>{this._getTwix(job).format()}</td>
						<td>{job.resultUrl && <a href={job.resultUrl}>Go to result</a>}</td>
						</tr>)}
					</tbody>
				</table>
			</div>
		</div>
	},
	_getTwix: function(job) {
		let startDate = moment(job.startDate);
		let endDate = moment(job.endDate);
		return startDate.twix(endDate);
	}
});

export { JobTable };
export default React.createFactory(JobTable);