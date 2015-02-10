(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$(function() {
		$('form.verify').on('submit',function(e) {
	        var currentForm = this;
	        e.preventDefault();
	        bootbox.confirm("Are you sure?", function(result) {
	            if (result) {
	                currentForm.submit();
	            }
	        });
	    });

		$('#attribute-mapping-table').scrollTableBody();
	});
		
}($, window.top.molgenis = window.top.molgenis || {}));