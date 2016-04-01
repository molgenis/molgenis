import React from 'react';
import { UploadForm } from './UploadForm';
import $ from 'jquery';
import AlertMessage from './AlertMessage';
import { JobContainer } from './jobs/JobContainer';

var UploadContainer = React.createClass({
	displayName:'UploadContainer',
	propTypes: {
		url: React.PropTypes.string.isRequired,
		width: React.PropTypes.oneOf(['1','2','3','4','5','6','7','8','9','10','11','12'])
	},
	getInitialState: function() {
		return {
			job : null
		}
	},
	render: function() {
		return <div>
			{this.state.job ? <JobContainer 
					jobHref={this.state.job} 
					onCompletion={this._onCompletion} 
			/> : <UploadForm
				width={this.props.width} 
				onSubmit={this._onSubmit}
			/>}	
		</div>
	},
	_onSubmit: function(form) {
		var self = this;
		var data = new FormData();
		data.append('file', form.file);
		data.append('entityName', form.fileName);
		data.append('action', form.action);
		data.append('notify', false);
		
		$.ajax({
			url: this.props.url,
			type: 'POST',
			data: data,
			cache: false,
			dataType: 'json',
			// Don't process the files
			processData: false, 
			// Set content type to false as jQuery will tell the server its a
			// query string request
			contentType: false, 
			success: function(data, textStatus, jqXHR) {
				// TODO data = import job url, do some success fail check stuff with it
				// TODO ImportRun should extend JobExecution
				self.setState({job:data});
				React.render(AlertMessage({
					'type' : 'success',
					'message' : 'Your file was succesfully uploaded, import has started',
					'onDismiss' : function(){}
				}), $('#instant-import-alert')[0]);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				React.render(AlertMessage({
					'type' : 'danger',
					'message' : 'Something went wrong during your file upload: ' + jqXHR.responseText,
					'onDismiss' : function(){}
				}), $('#instant-import-alert')[0]);
			}
		});
	},
	_onCompletion: function(job) {
		if(job.status === 'FINISHED' || job.status === 'SUCCESS') {
			React.render(AlertMessage({
				'type' : 'success',
				'message' : 'Import has succeeded! ' + job.message,
				'onDismiss' : function(){}
			}), $('#instant-import-alert')[0]);
		} else {
			React.render(AlertMessage({
				'type' : 'danger',
				'message' : 'Something went wrong during your import: ' + job.message,
				'onDismiss' : function(){}
			}), $('#instant-import-alert')[0]);
		}
		this.setState({job:null});
	}
});

export { UploadContainer };
export default React.createFactory(UploadContainer);