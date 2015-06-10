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
		var $bodyCells = $table.find('tbody tr:first').children(), colWidth;

		// Get the tbody columns width array
		colWidth = $bodyCells.map(function() {
			return $(this).width();
		}).get();

		// Set the width of thead columns
		$table.find('thead tr').children().each(function(i, v) {
			$(v).width(colWidth[i]);
		});
	}

	/**
	 * Searches the source attributes in an algorithm string.
	 * 
	 * @param algorithm
	 *            the algorithm string to search
	 */
	function getSourceAttrs(algorithm) {
		var regex = /\$\(['"]([^\$\(\)]+)['"]\)/g, match, result = [];

		while ((match = regex.exec(algorithm))) {
			if (match) {
				result.push(match[1]);
			}
		}
		return result;
	}

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
	}

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
		if ($('.distribution').length) {
			$('.distribution').bcgraph(data.results);
		}
	}

	/**
	 * Colors the backgrounds of the attributes mentioned in the algorithm
	 * 
	 * @param algorithm
	 *            the algorithm string
	 */
	function updateAttributeSelectionMarker(algorithm) {
		var sourceAttrs = getSourceAttrs(algorithm);
		$('#attribute-mapping-table').find('td').each(function(index, value) {
			
			var name = $(this).attr('class'), inArray = $.inArray(name, sourceAttrs), btn = $(this).find('button');
			
			if (inArray >= 0) { // If this is an attribute present in algorithm
				$(this).css('background-color', '#CCFFCC'); // set background color
				btn.removeClass('not-selected').addClass('selected'); // add selected class
				btn.find('span').removeClass('glyphicon-ok').addClass('glyphicon-remove'); // change icon
			} else { // Attribute is not present in algorithm
				$(this).css('background-color', ''); // remove background color
				btn.find('span').removeClass('glyphicon-remove').addClass('glyphicon-ok'); // change icon
			}
		});
	}
	
	/**
	 * Inserts attribute in the editor.
	 * 
	 * @param attribute
	 *            the name of the attribute
	 * @param editor
	 *            the ace algorithm editor to insert the attribute into
	 */
	function insertAttribute(attribute, editor) {
		var algorithm = "$('" + attribute + "').value()";
		editor.insert(algorithm, -1);
		$('#algorithm-container').append(algorithm);
	}

	/**
	 * Removes attribute from the editor
	 * 
	 * @param attribute
	 *            the name of the attribute
	 * @param editor
	 *            the ace algorithm editor to insert the attribute into
	 */
	function outsertAttribute(attribute, editor) {
		var algorithm = "$('" + attribute + "').value()", algorithmContainer = $('#algorithm-container');
		editor.replaceAll("", algorithm);
		algorithmContainer.html(algorithmContainer.text().split(algorithm).join(""));
	}
	
	$(function() {
		var $textarea = $("#edit-algorithm-textarea"), initialValue = $textarea.val(), editor, $scrollTable = $('table.scroll');

		// N.B. Always do this first cause it fiddles with the DOM and disrupts
		// listeners you may have placed on the table elements!
		$('#attribute-mapping-table').scrollTableBody({
			rowsToDisplay : 6
		});

		// buttons outside of forms to submit forms
		$('#preview-mapping-result-btn').on('click', function() {
			$('#preview-mapping-result-form').submit();
		});

		$('#save-attribute-mapping-btn').on('click', function() {
			$('#save-attribute-mapping-form').submit();
		});

		$('button.toggle-btn').on('click', function() {
			var sourceName = $(this).data('attribute'), btn = $(this); // attribute name / row indicator
			
			
			// Dynamicly update the preview table based on selected attributes
			$.ajax({
				url : molgenis.getContextUrl() + '/dynamicattributemappingfeedback',
				data : {
					mappingProjectId : $('input[name="mappingProjectId"]').val(),
					target : $('input[name="target"]').val(),
					source : $('input[name="source"]').val(),
					targetAttribute : $('input[name="targetAttribute"]').val(),
					algorithm : $('#algorithm-container').text()
				},
				type : 'POST',
				success : function(result) {
					for(var key in result) {
						$('#mapping-preview-container').html(result[key]);
					}
				}
			});
			
			if (btn.hasClass('not-selected')) { // switching to selected
				btn.removeClass('not-selected').addClass('selected'); // change class
				btn.find('span').toggleClass('glyphicon-ok').toggleClass('glyphicon-remove'); // toggle icons
				$('td[class='+sourceName+']').css('background-color', '#CCFFCC'); // change background
				insertAttribute(sourceName, editor); // add algorithm to editor
				
			} else if (btn.hasClass('selected')) { // switching to non-selected
				btn.removeClass('selected').addClass('not-selected'); // change class
				btn.find('span').toggleClass('glyphicon-remove').toggleClass('glyphicon-ok'); // toggle icons
				$('td[class='+sourceName+']').css('background-color', ''); // change background
				outsertAttribute(sourceName, editor); // remove algorithm from editor
			}
			
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
		
		$('#save-attribute-mapping-form').on('reset', function() {
			if (editor.getValue() === initialValue) {
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

		// TODO change the background color of selected attributes back to
		// normal when they get deselected
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