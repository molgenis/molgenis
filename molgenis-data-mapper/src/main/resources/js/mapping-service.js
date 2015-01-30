(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$(function() {
		$("#submit-new-mapping-project-btn").click(function() {
			$("#create-new-mapping-project-form .submit").click();
		});
		
		$("#submit-new-source-column-btn").click(function() {
			var newSourceEntity = $("#new-source-entity").val();
			$("#create-new-source-column-modal").modal('toggle');
			$("#target-mapping-table").append("<div class='col-md-3'>This is were the table will be</div>")
		});
		
		$( "#target-entity-select" ).change(function() {
			var selectedTarget = $("#target-entity").val();
			alert("Change!");
		});
		
	});
	
	
}($, window.top.molgenis = window.top.molgenis || {}));