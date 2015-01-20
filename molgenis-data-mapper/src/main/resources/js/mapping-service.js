(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$(function() {
		$("#submit-new-mapping-project-btn").click(function() {
			$("#create-new-mapping-project-form .submit").click();
		});
		
		$("#submit-new-source-column-btn").click(function() {
			var newSourceEntity = $("#new-source-entity").val();
			alert(newSourceEntity);
		});
	});
	
	
}($, window.top.molgenis = window.top.molgenis || {}));