(function($, molgenis) {	
	$(function() {
		var $table = $('table.scroll'),
			$bodyCells = $table.find('tbody tr:first').children(),
			colWidth;
		
		$('#attribute-mapping-table').scrollTableBody({rowsToDisplay:10});
		
		$('.ace.readonly').each(function(){
			var id = $(this).attr('id'),
				editor = ace.edit(id);
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