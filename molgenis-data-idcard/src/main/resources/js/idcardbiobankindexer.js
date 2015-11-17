(function($, molgenis) {
	"use strict";

	function renderTable() {
		React.render(molgenis.ui.Table({
			entity: 'IdCardIndexingEvent',
			sort: {
				attr: {
					name: 'date'
				},
				order: 'desc',
				path: []
			},
			enableAdd: false,
			enableEdit: false,
			enableDelete: false,
			enableInspect: false
		}), $('#index-event-table-container')[0]);
	}
	
	function refreshTable() {
		// refresh table
		React.unmountComponentAtNode($('#index-event-table-container')[0]);
		renderTable();
	}
	
	function updateJobStatus(job) {
		if(job.triggerStatus === 'NONE' || job.triggerStatus === 'SUCCESS' || job.triggerStatus === 'ERROR') {
			// TODO enable button
			molgenis.createAlert([{'message': 'Reindexing ID-Card completed'}], 'success');
			refreshTable();
		}
		else {
			molgenis.createAlert([{'message': 'Reindexing ID-Card biobanks in progress ...'}], 'info');
			setTimeout(function() {
				$.get(molgenis.getContextUrl() + '/status/' + job.triggerGroup + '/' + job.triggerName).done(function(job) {
					updateJobStatus(job);	
				});
			}, 1000);
		}
	}
	
	$(function() {
		renderTable();
		
		React.render(molgenis.ui.Button({
			text : 'Reindex',
			size: 'small',
			onClick : function() {
				// TODO disable button
				$.post(molgenis.getContextUrl() + '/reindex').done(function(job) {
					updateJobStatus(job);
				});
			}
		}, 'Reindex'), $('#index-btn-container')[0]);
	});

}($, window.top.molgenis = window.top.molgenis || {}));