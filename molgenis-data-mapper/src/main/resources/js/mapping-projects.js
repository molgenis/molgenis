(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$(function() {
		$('#submit-new-mapping-project-btn').click(function() {
			$('#create-new-mapping-project-form .submit').click();
		});
		
		$('#submit-new-source-column-btn').click(function() {
			$('#create-new-source-form .submit').click();
		});
		
		$('#create-new-mapping-project-form').validate();
		
		var $table = $('table.scroll');
		var $bodyCells = $table.find('tbody tr:first').children();
	    var colWidth;
		// Adjust the width of thead cells when window resizes
		$(window).resize(function() {
		    // Get the tbody columns width array
		    colWidth = $bodyCells.map(function() {
		        return $(this).width();
		    }).get();
		    
		    // Set the width of thead columns
		    $table.find('thead tr').children().each(function(i, v) {
		        $(v).width(colWidth[i]);
		    });    
		}).resize(); // Trigger resize handler
		
		
		
	});
		
}($, window.top.molgenis = window.top.molgenis || {}));