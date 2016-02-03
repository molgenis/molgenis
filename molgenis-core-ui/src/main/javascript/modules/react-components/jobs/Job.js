/**
 * The Job component translates information from the Job entity to a visual
 * representation of the progress of that job, i.e a progress bar
 * 
 * @module Job
 * 
 * @param job
 *            A key value object containing all the details of a job including
 *            its status and progress message
 * 
 * @exports Job class
 */

import React from "react";
import {ProgressBarClass} from '../ProgressBar';

import DeepPureRenderMixin from '../mixin/DeepPureRenderMixin'; 

const div = React.DOM.div;

var Job = React.createClass({
	mixins: [DeepPureRenderMixin],
	displayName: 'Job',
	propTypes: {
		job: React.PropTypes.object.isRequired
	},
	render: function() {
		return <div>
			<ProgressBarClass 
				progressPct={this.props.job.progressMax !== undefined ? this._getProgressPct() : 100}
				progressMessage={this.props.job.progressMessage} 
				status={this._getCssClass()} 
				active={this._isActive()} 
			/>
		</div>
	},
	_getCssClass: function() {
		let cssTable = {
			'Pending': 'info',
			'Running' : 'primary',
			'Success' : 'success',
			'Failed' : 'danger'
		}
		return cssTable[this.props.job.status];
	},
	_isActive: function() {
		let activeTable = {
			'Pending': true,
			'Running' : true,
			'Success' : false,
			'Failed' : false
		}
		return activeTable[this.props.job.status];
	},
	_getProgressPct: function() {
		let progressInt = this.props.job.progressInt;
		let progressMax = this.props.job.progressMax;
		console.log(progressInt, progressMax);
		let calculatedWidth =  progressInt / progressMax * 100;
		console.log(calculatedWidth);
		return calculatedWidth; 
	}
});

export default Job;