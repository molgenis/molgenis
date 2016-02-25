/**
 * The JobsContainer retrieves jobs from the server and renders them using the
 * Jobs component. Shows a spinner while fetching.
 * 
 * @module JobsContainer
 * 
 * @param username
 *            The username of the one viewing the jobs. Jobs can be filtered
 *            based on this username
 * 
 * @exports JobsContainer factory
 */
import React from 'react';
import RestClient from "rest-client/RestClientV2";
import { Spinner } from '../Spinner'
import $ from 'jquery';

import { Jobs } from './Jobs';

import SetIntervalMixin from '../mixin/SetIntervalMixin';

var JobsContainer = React.createClass({
	mixins: [SetIntervalMixin],
	displayName: 'JobsContainer',
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
		$.get('/plugin/jobs/latest', function(data) {
			self.setState({jobs: data})
		}); 
	}
});

export default React.createFactory(JobsContainer);