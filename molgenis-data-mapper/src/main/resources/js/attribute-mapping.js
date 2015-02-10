(function($, molgenis) {	
	"use strict";
	$(function() {
		var $textarea = $("#edit-algorithm-textarea");
		var initialValue = $textarea.val();
		
		$('#attribute-mapping-table').scrollTableBody();
		
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
		
		$('button.insert').click(function(){
			editor.insert("$('"+$(this).data('attribute')+"')", -1);
			return false;
		});
		
		$('#attribute-table-container form').on('reset', function() {
			editor.setValue(initialValue,-1);
		});
		
		function getSourceAttrs(algorithm){
			var regex = /\$\(['"]([^\$\(\)]+)['"]\)/g;
			var match;
			var result = [];
			
			while ((match = regex.exec($textarea.val()))) {
				if(match){
					result.push(match[1]);
				}
			}
			return result;
		}
		
		editor.getSession().on('change', function(){
			var sourceAttrs = getSourceAttrs($textarea.val());
			$('input:checkbox').each(function(){
				var name = $(this).attr('name');
				var inArray = $.inArray(name,sourceAttrs);
				console.log(name);
				console.log(sourceAttrs);
				console.log(inArray);
				$(this).prop('checked', inArray != -1);
			});
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