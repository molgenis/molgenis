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
	 * Checks those checkboxes that are mentioned in an algorithm.
	 * 
	 * @param algorithm
	 *            the algorithm string
	 */
	function updateCheckboxes(algorithm) {
		var sourceAttrs = getSourceAttrs(algorithm);
		$('input:checkbox').each(function(index, value) {
			var name = $(this).attr('name'),
				inArray = $.inArray(name, sourceAttrs);
			$(this).prop('checked', inArray >= 0);
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

		$('button.insert').click(function() {
			insertAttribute($(this).data('attribute'), editor);
			return false;
		});

		$('#saveattributemapping-form').on('reset', function() {
			if(editor.getValue() === initialValue){
				return false;
			}
			bootbox.confirm("Do you want to revert your changes?", function(result) {
	            if (result) {
	                editor.setValue(initialValue, -1);
	                updateCheckboxes(initialValue);
	                $('#statistics-container').empty();
	            }
	        });
			return false;
		});

		editor.getSession().on('change', function() {
			updateCheckboxes(editor.getValue());
		});
		updateCheckboxes(initialValue);

		$('#btn-test').click(function() {
			testAlgorithm(editor.getValue());
			return false;
		});

		$('#statistics-container').hide();
	});

}($, window.top.molgenis = window.top.molgenis || {}));