(function($, molgenis) {	
	"use strict";
	$(function() {
		var initialValue = $("#edit-algorithm-textarea").val();
		
		$("#edit-algorithm-textarea").ace({
			options: {
				enableBasicAutocompletion: true
			},
			readOnly: $("#edit-algorithm-textarea").data('readOnly'),
			theme: 'eclipse',
			mode: 'javascript',
			showGutter: true,
			highlightActiveLine: false
		});
		var editor = $('#edit-algorithm-textarea').data('ace').editor;

		$('#statistics-container').hide();

		$('#attribute-mapping-table').scrollTableBody();
		
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
		
		var showStatistics = function(data){
			if(data.results.length > 0) {
				$('#stats-total').text(data.totalCount);
				$('#stats-valid').text(data.results.length);
				$('#stats-mean').text(jStat.mean(data.results));
				$('#stats-median').text(jStat.median(data.results));
				$('#stats-stdev').text(jStat.stdev(data.results));
				
				$('#statistics-container').show();
				$('.distribution').bcgraph(data.results);
			} else {
				$('#statistics-container').hide();
				molgenis.createAlert([{'message':'There are no values generated for this algorithm'}],'error');
			}
		};
		
		$('button.select').click(function(){
			editor.setValue("$('"+$(this).data('attribute')+"');", -1);
			return false;
		});
		
		$('#attribute-table-container form').on('reset', function() {
			editor.setValue(initialValue,-1);
		});
		
		$('#btn-test').click(function(){
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/mappingattribute/testscript',
				async : false,
				data : JSON.stringify({
					targetEntityName : $('input[name="target"]').val(), 
					sourceEntityName : $('input[name="source"]').val(), 
					targetAttributeName : $('input[name="targetAttribute"]').val(),
					algorithm: editor.getValue()
				}),
				contentType : 'application/json',
				success : showStatistics
			});
			return false;
		});
	});
		
}($, window.top.molgenis = window.top.molgenis || {}));