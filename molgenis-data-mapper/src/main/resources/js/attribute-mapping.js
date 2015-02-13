(function($, molgenis) {
	"use strict";

	/**
	 * Updates a table's column widths.
	 * 
	 * @param $table
	 *            the scrollable table whose header and body cells should be
	 *            realigned
	 */
	var updateColumnWidths = function($table) {
		var $bodyCells = $table.find('tbody tr:first').children();
		var colWidth;

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
	var insertAttribute = function(attribute, editor) {
		editor.insert("$('" + attribute + "')", -1);
	};

	/**
	 * Searches the source attributes in an algorithm string.
	 * 
	 * @param algorithm
	 *            the algorithm string to search
	 */
	var getSourceAttrs = function(algorithm) {
		var regex = /\$\(['"]([^\$\(\)]+)['"]\)/g;
		var match;
		var result = [];

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
	var updateCheckboxes = function(algorithm) {
		var sourceAttrs = getSourceAttrs(algorithm);
		$('input:checkbox').each(function(index, value) {
			var name = $(this).attr('name');
			var inArray = $.inArray(name, sourceAttrs);
			$(this).prop('checked', inArray >= 0);
		});
	};

	/**
	 * Sends an algorithm to the server for testing.
	 * 
	 * @param algorithm
	 *            the algorithm string to send to the server
	 */
	var testAlgorithm = function(algorithm) {
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
	var showStatistics = function(data) {
		if (data.results.length > 0) {
			$('#stats-total').text(data.totalCount);
			$('#stats-valid').text(data.results.length);
			$('#stats-mean').text(jStat.mean(data.results));
			$('#stats-median').text(jStat.median(data.results));
			$('#stats-stdev').text(jStat.stdev(data.results));

			$('#statistics-container').show();
			if($('.distribution').length){
				$('.distribution').bcgraph(data.results);
			}
		} else {
			$('#statistics-container').hide();
			molgenis.createAlert([ {
				'message' : 'There are no values generated for this algorithm'
			} ], 'error');
		}
	};

	$(function() {
		var $textarea = $("#edit-algorithm-textarea");
		var initialValue = $textarea.val();
		var editor;
		var $scrollTable = $('table.scroll');

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

		$('form').on('reset', function() {
			if(editor.getValue() === initialValue){
				return false;
			}
			bootbox.confirm("Do you want to revert your changes?", function(result) {
	            if (result) {
	                editor.setValue(initialValue, -1);
	                updateCheckboxes(initialValue);
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