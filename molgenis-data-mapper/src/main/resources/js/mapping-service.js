(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$(function() {
		$("#submit-new-mapping-project-button").click(function() {
			$("#create-new-mapping-project-form .submit").click();
		});
		
//		$("a.mapping-project-name").click(function() {
//			// value of clicked anchor: $(this).attr('value')
//			
//		});
	});
	
	
}($, window.top.molgenis = window.top.molgenis || {}));