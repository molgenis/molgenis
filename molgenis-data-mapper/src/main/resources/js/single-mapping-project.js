(function($, molgenis) {	
	$(function() {
		$('#attribute-mapping-table').scrollTableBody();
		
		$('form.verify').submit(function(e) {
	        var currentForm = this;
	        e.preventDefault();
	        bootbox.confirm("Are you sure?", function(result) {
	            if (result) {
	                currentForm.submit();
	            }
	        });
	    });
		
		$('#submit-new-source-column-btn').click(function() {
			console.log('click!');
			$('#create-new-source-form').submit();
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