(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$(function() {
		$('#create-integrated-entity-btn').click(function() {
			$('#create-integrated-entity-form .submit').click();
		});
		
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
		
		$('textarea.ace.readonly').ace({
			mode: 'javascript',
			readOnly: true,
			showGutter: false,
			highlightActiveLine: false,
			theme: 'eclipse'
		});
	});
		
}($, window.top.molgenis = window.top.molgenis || {}));