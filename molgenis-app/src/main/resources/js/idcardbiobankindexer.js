(function($, molgenis) {
	"use strict";

	function renderTable() {
		React.render(molgenis.ui.Table({
			entity: 'IdCardIndexingEvent',
//			enableAdd: false,
//			enableEdit: false,
//			enableDelete: false,
//			enableInspect: false
		}), $('#index-event-table-container')[0]);
	}
	
	$(function() {
		renderTable();
		
		React.render(molgenis.ui.Button({
			text : 'Reindex',
			size: 'small',
			onClick : function() {
				$.post(molgenis.getContextUrl() + '/reindex').done(function() {
					molgenis.createAlert([{'message': 'Reindexing ID-Card biobanks completed.'}], 'success');
					
					// refresh table
					React.unmountComponentAtNode($('#index-event-table-container')[0]);
					renderTable();
				});
			}
		}, 'Reindex'), $('#index-btn-container')[0]);
	});

}($, window.top.molgenis = window.top.molgenis || {}));