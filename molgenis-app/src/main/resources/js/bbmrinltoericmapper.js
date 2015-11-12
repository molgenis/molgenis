(function($, molgenis) {
	"use strict";
	
	$(function() {
		React.render(molgenis.ui.Button({
			text : 'Enable Scheduler',
			size: 'small',
			onClick : function() {
				$.post(molgenis.getContextUrl() + '/scheduleMappingJob').done(function(job) {
					molgenis.createAlert([{'message': 'Scheduled mapping job.'}], 'success');
				});
			}
		}, 'Reindex'), $('#enable-mapper-scheduler-btn-container')[0]);
	});
}($, window.top.molgenis = window.top.molgenis || {}));