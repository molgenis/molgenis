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

import Jobs from './Jobs';

import DeepPureRenderMixin from '../mixin/DeepPureRenderMixin'; 

const api = new RestClient();
const div = React.DOM.div;

var JobContainer = React.createClass({
	mixins: [DeepPureRenderMixin],
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
		this._retrieveJobs();
	}, 
	render: function() {
		if(this.state.jobs === null) {
			return <Spinner  />;
		} else {
			return <div><Jobs jobs={this.state.jobs} /></div>
		}
	},
	_retrieveJobs: function() {
		api.get('Job').done((data) => this.setState({jobs: data.items}));
	}
});

export default React.createFactory(JobContainer);