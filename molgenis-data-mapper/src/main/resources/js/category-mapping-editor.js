(function($, molgenis) {
	"use strict";
	
	$(function() {
		$('#save-category-mapping-btn').on('click', function(){
			// should return an algorithm mapping source value to target value
			// which can be found by looking at tr class and the selected option
		});
		
		$('#cancel-category-mapping-btn').on('click', function(){
			bootbox.confirm("Are you sure you want to go back? All unsaved changes will be lost!", function(result) {
	            if (result) {
	            	window.history.back();
	            }
	        });
		});
		
	});
}($, window.top.molgenis = window.top.molgenis || {}));


