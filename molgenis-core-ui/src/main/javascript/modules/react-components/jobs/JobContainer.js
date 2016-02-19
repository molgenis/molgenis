/**
 * The JobContainer retrieves all jobs from the Job entity on the server and
 * renders jobs in the view by calling the Jobs component.
 * 
 * @module JobContainer
 * 
 * @param username
 *            The username of the one viewing the jobs. Jobs can be filtered
 *            based on this username
 * 
 * @exports JobContainer factory
 */
import React from 'react';
import RestClient from "rest-client/RestClientV2";
import { Spinner } from '../Spinner'
import $ from 'jquery';

import { Jobs } from './Jobs';

import DeepPureRenderMixin from '../mixin/DeepPureRenderMixin'; 
import SetIntervalMixin from '../mixin/SetIntervalMixin';

const api = new RestClient();

var JobContainer = React.createClass({
	mixins: [DeepPureRenderMixin, SetIntervalMixin],
	displayName: 'JobContainer',
	propTypes: {
		username: React.PropTypes.string
	},
	getInitialState: function() {
		return {
			jobs : null
		}
	},
	componentDidMount: function() {
		this.retrieveJobs();
		this.setInterval(this.retrieveJobs, 1000);
	},
	render: function() {
		if(this.state.jobs === null) {
			return <Spinner  />;
		} else {
			return <div><Jobs jobs={this.state.jobs}/></div>
		}
	},
	retrieveJobs: function() {
		var self = this;
		console.log('pull from entity');
		$.get('/plugin/jobs/latest', function(data) {
			console.log(data);
			self.setState({jobs: data})
		}); 
	}
});

export default React.createFactory(JobContainer);