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

var JobTable = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'JobTable',
	propTypes: {
		jobs: React.PropTypes.array.isRequired
	},
	render: function() {
		const {jobs} = this.props;
		return <div>
			<div className="panel panel-primary">
				<div className="panel-heading">Finished Jobs</div>
				<div className="panel-body">
					<table className="table table-striped">
						<thead>
							<th>Status</th>
							<th>Runtime</th>
							<th>Type</th>
							<th>Message</th>
							<th>Started</th>
							<th>Finished</th>
							<th>Link to entity</th>
						</thead>
						<tbody>
						{jobs.map((job) => <tr key={job.identifier}>
							<td>{job.status}</td>
							<td>{this._getRunTime(job)}</td>
							<td>{job.type}</td>
							<td>{job.progressMessage}</td>
							<td>{moment(job.startDate).format('llll')}</td>
							<td>{moment(job.endDate).fromNow()}</td>
							<td>URL to {job.entityName}</td>
							</tr>)}
						</tbody>
					</table>
				</div>
			</div>
		</div>
	},
	_getRunTime: function(job) {
		let startDate = moment(job.startDate);
		let endDate = moment(job.endDate);
		return endDate.from(startDate, true);
	}
});

export { JobTable };
export default React.createFactory(JobTable);