(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$(function() {
		$('#submit-new-mapping-project-btn').click(function() {
			$('#create-new-mapping-project-form .submit').click();
		});
		
		$('#target-entity-select').change(function() {
			// TODO rerender page with different selectedTarget
		});
		
		$('#submit-new-source-column-btn').click(function() {
			$('#create-new-source-form .submit').click();
			
		});
	});
		
}($, window.top.molgenis = window.top.molgenis || {}));