(function($, molgenis) {
	"use strict";

	$(function() {
		React.render(molgenis.ui.Button({
			text : 'Reindex',
			onClick : function() {
				$.post(molgenis.getContextUrl() + '/reindex').done(function() {
					molgenis.createAlert([{'message': 'Reindexing ID-Card biobanks completed.'}], 'success');
				});
			}
		}, 'Reindex'), $('#index-btn-container')[0]);
	});

}($, window.top.molgenis = window.top.molgenis || {}));