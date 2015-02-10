(function($, molgenis) {	
	$(function() {
		$('#attribute-mapping-table').scrollTableBody();
		
		$('.ace.readonly').each(function(){
			var id = $(this).attr('id');
			var editor = ace.edit(id);
			editor.setTheme("ace/theme/eclipse");
		    editor.getSession().setMode("ace/mode/javascript");
		    editor.setReadOnly(true);
            editor.renderer.setShowGutter(false);
            editor.setHighlightActiveLine(false);
		});
		
		$('form.verify').submit(function() {
	        var currentForm = this;
	        bootbox.confirm("Are you sure?", function(result) {
	            if (result) {
	                currentForm.submit();
	            }
	        });
	        return false;
	    });
		
		$('#submit-new-source-column-btn').click(function() {
			$('#create-new-source-form').submit();
		});
		
		$('#create-integrated-entity-btn').click(function(){
			$('#create-integrated-entity-form').submit();
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
	});
		
}($, window.top.molgenis = window.top.molgenis || {}));