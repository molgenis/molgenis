/**
 * This module receives an array of job objects and renders a Job component for
 * each of the job objects.
 * 
 * @module Jobs
 * 
 * @param jobs
 *            An array of job objects
 * 
 * @exports Job class
 */
import React from 'react';
import Job from './Job';

import DeepPureRenderMixin from '../mixin/DeepPureRenderMixin'; 

const div = React.DOM.div;

var Jobs = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'Jobs',
	propTypes : {
		jobs : React.PropTypes.array.isRequired
	},
	render: function() {
		return <div>
			{this.props.jobs.map(function(job){	
				return <Job job={job} key={job.identifier} />;
			})}
		</div>;
	}	
});

export default Jobs;