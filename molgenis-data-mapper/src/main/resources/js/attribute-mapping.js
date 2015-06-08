(function($, molgenis) {
	"use strict";

	/**
	 * Updates a table's column widths.
	 * 
	 * @param $table
	 *            the scrollable table whose header and body cells should be
	 *            realigned
	 */
	function updateColumnWidths($table) {
		var $bodyCells = $table.find('tbody tr:first').children(),
			colWidth;

		// Get the tbody columns width array
		colWidth = $bodyCells.map(function() {
			return $(this).width();
		}).get();

		// Set the width of thead columns
		$table.find('thead tr').children().each(function(i, v) {
			$(v).width(colWidth[i]);
		});
	};

	/**
	 * Inserts attribute in the editor.
	 * 
	 * @param attribute
	 *            the name of the attribute
	 * @param editor
	 *            the ace algorithm editor to insert the attribute into
	 */
	function insertAttribute(attribute, editor) {
		editor.insert("$('" + attribute + "').value()", -1);
	};
	
	/**
	 * Removes attribute from the editor
	 * 
	 * @param attribute
	 *            the name of the attribute
	 * @param editor
	 *            the ace algorithm editor to insert the attribute into
	 */
	function outsertAttribute(attribute, editor) {
		editor.remove("$('" + attribute + "').value()");
	}

	/**
	 * Searches the source attributes in an algorithm string.
	 * 
	 * @param algorithm
	 *            the algorithm string to search
	 */
	function getSourceAttrs(algorithm) {
		var regex = /\$\(['"]([^\$\(\)]+)['"]\)/g,
			match,
			result = [];

		while ((match = regex.exec(algorithm))) {
			if (match) {
				result.push(match[1]);
			}
		}
		return result;
	};

	/**
	 * Colors the backgrounds of the attributes mentioned in the algorithm
	 * 
	 * @param algorithm
	 *            the algorithm string
	 */
	function updateAttributeSelectionMarker(algorithm) {
		var sourceAttrs = getSourceAttrs(algorithm);
		$('#attribute-mapping-table').find('td').each(function(index, value) {
			var name = $(this).attr('class'),
				inArray = $.inArray(name, sourceAttrs);
			if(inArray >= 0){
				$(this).css('background-color', '#CCFFCC');				
			} else {
				$(this).css('background-color', '');
			}
		});
	};

	/**
	 * Sends an algorithm to the server for testing.
	 * 
	 * @param algorithm
	 *            the algorithm string to send to the server
	 */
	function testAlgorithm(algorithm) {
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/mappingattribute/testscript',
			data : JSON.stringify({
				targetEntityName : $('input[name="target"]').val(),
				sourceEntityName : $('input[name="source"]').val(),
				targetAttributeName : $('input[name="targetAttribute"]').val(),
				algorithm : algorithm
			}),
			contentType : 'application/json',
			success : showStatistics
		});
	};

	/**
	 * Shows statistics for the test results.
	 * 
	 * @param data
	 *            the results from the server
	 */
	function showStatistics(data) {
		if (data.results.length === 0) {
			$('#statistics-container').hide();
			molgenis.createAlert([ {
			'message' : 'No valid cases are produced by the algorithm. TIP: Maybe your data set is empty.'
			} ], 'warning');
		}
		
		$('#stats-total').text(data.totalCount);
		$('#stats-valid').text(data.results.length);
		$('#stats-mean').text(jStat.mean(data.results));
		$('#stats-median').text(jStat.median(data.results));
		$('#stats-stdev').text(jStat.stdev(data.results));

		$('#statistics-container').show();
		if($('.distribution').length){
			$('.distribution').bcgraph(data.results);
		}
	};

	$(function() {
		var $textarea = $("#edit-algorithm-textarea"),
			initialValue = $textarea.val(),
			editor,
			$scrollTable = $('table.scroll');

		// N.B. Always do this first cause it fiddles with the DOM and disrupts
		// listeners you may have placed on the table elements!
		$('#attribute-mapping-table').scrollTableBody({
			rowsToDisplay : 6
		});
		
		$('#mapping-result-preview-btn').on('click', function(){
			$('#mapping-result-preview-container').html("<h1>Preview table here</h1>");
		});
		
//		$('a[href="#advanced"]').on('shown.bs.tab', function() {});

		$("#edit-algorithm-textarea").ace({
			options : {
				enableBasicAutocompletion : true
			},
			readOnly : $("#edit-algorithm-textarea").data('readonly') === true,
			theme : 'eclipse',
			mode : 'javascript',
			showGutter : true,
			highlightActiveLine : false
		});
		editor = $('#edit-algorithm-textarea').data('ace').editor;

		$(window).resize(updateColumnWidths($scrollTable));
		updateColumnWidths($scrollTable);

		$('button.insert').on('click', function() {
			var sourceName = $(this).data('attribute');
			
			// On click, switch the classes to give it a 'click-to-remove' look
			$(this).find('span').toggleClass('glyphicon-ok').toggleClass('glyphicon-remove');
			$(this).toggleClass('insert').toggleClass('outsert');
			
			insertAttribute(sourceName, editor);
			
			return false;
		});
		
		$('button.outsert').on('click', function() {
			var sourceName = $(this).data('attribute');
			
		});

		$('#saveattributemapping-form').on('reset', function() {
			if(editor.getValue() === initialValue){
				return false;
			}
			bootbox.confirm("Do you want to revert your changes?", function(result) {
	            if (result) {
	                editor.setValue(initialValue, -1);
	                updateAttributeSelectionMarker(initialValue);
	                $('#statistics-container').empty();
	            }
	        });
			return false;
		});

		// TODO change the background color of selected attributes back to normal when they get deselected
		editor.getSession().on('change', function() {
			updateAttributeSelectionMarker(editor.getValue());
		});
		updateAttributeSelectionMarker(initialValue);

		$('#btn-test').click(function() {
			testAlgorithm(editor.getValue());
			return false;
		});

		$('#statistics-container').hide();
	});

}($, window.top.molgenis = window.top.molgenis || {}));