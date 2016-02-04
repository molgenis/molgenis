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
import { ProgressBarClass } from '../ProgressBar';

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
				progressMessage={this.props.job.progressInt !== undefined ? this._formatProgressMessage() : this.props.job.progressMessage} 
				progressPct={this.props.job.progressMax !== undefined ? this._getProgressPct() : 100}
				status={this._getCssClass() || 'primary'} 
				active={this._isActive()} 
			/>
		</div>
	},
	_getCssClass: function() {
		let cssTable = {
			'PENDING': 'info',
			'RUNNING' : 'primary',
			'SUCCESS' : 'success',
			'FAILED' : 'danger',
			'CANCELED' : 'warning',
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
		let calculatedWidth =  progressInt / progressMax * 100;
		return calculatedWidth; 
	},
	_formatProgressMessage: function() {
		return this.props.job.progressMessage.replace('%p', this.props.job.progressInt);
	}
});

export default Job;