(function($, molgenis) {
	"use strict";

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
	 * Load result table from view-attribute-mapping-feedback.ftl
	 * 
	 * @param algorithm
	 *            the algorithm that is send to the server to apply over the
	 *            submitted source values
	 */
	function loadAlgorithmResult(algorithm) {
		$("#result-table-container").load("attributemappingfeedback #algorithm-result-feedback-container", {
			mappingProjectId : $('input[name="mappingProjectId"]').val(),
			target : $('input[name="target"]').val(),
			source : $('input[name="source"]').val(),
			targetAttribute : $('input[name="targetAttribute"]').val(),
			algorithm : algorithm
		});
	}

	/**
	 * Selects the attributes mentioned in the algorithm
	 * 
	 * @param algorithm
	 *            the algorithm string
	 */
	function checkSelectedAttributes(algorithm) {
		var sourceAttrs = getSourceAttrs(algorithm);
		$('input:checkbox').each(function(index, value) {
			var name = $(this).attr('class'), inArray = $.inArray(name, sourceAttrs);
			$(this).prop('checked', inArray >= 0);
		});
	}

	/**
	 * Clears the editor and inserts selected attributes.
	 * 
	 * @param attribute
	 *            the name of the attribute
	 * @param editor
	 *            the ace algorithm editor to insert the attribute into
	 */
	function insertSelectedAttributes(selectedAttributes, editor) {
		editor.setValue(""); // clear the editor
		$(selectedAttributes).each(function() {
			editor.insert("$('" + this + "').value();", -1);
		});
	}

	$(function() {

		var editor, searchQuery, selectedAttributes, $textarea, initialValue, algorithm, feedBackRequest, row;

		// N.B. Always do this first cause it fiddles with the DOM and disrupts
		// listeners you may have placed on the table elements!
		$('#attribute-mapping-table').scrollTableBody({
			rowsToDisplay : 6
		});

		// TODO $.ajax / $.post or form submit?
		// TODO Load mapping options for initial selection of attributes

		$textarea = $("#ace-editor-text-area");
		initialValue = $textarea.val();

		$textarea.ace({
			options : {
				enableBasicAutocompletion : true
			},
			readOnly : $textarea.data('readonly') === true,
			theme : 'eclipse',
			mode : 'javascript',
			showGutter : true,
			highlightActiveLine : false
		});
		editor = $textarea.data('ace').editor;

		editor.getSession().on('change', function() {
			checkSelectedAttributes(editor.getValue());
			algorithm = editor.getSession().getValue();
		});
		checkSelectedAttributes(initialValue);
		algorithm = editor.getSession().getValue();

		$('#save-mapping-btn').on('click', function() {
			$.post(molgenis.getContextUrl() + "/saveattributemapping", {
				mappingProjectId : $('input[name="mappingProjectId"]').val(),
				target : $('input[name="target"]').val(),
				source : $('input[name="source"]').val(),
				targetAttribute : $('input[name="targetAttribute"]').val(),
				algorithm : algorithm
			}, function() {
				molgenis.createAlert([ {
					'message' : 'Succesfully saved the created mapping'
				} ], 'success');
			});
		});

		$('#test-mapping-btn').on('click', function() {
			selectedAttributes = [];
			$('#attribute-mapping-table').find('tr').each(function() {
				row = $(this);
				if (row.find('input[type="checkbox"]').is(':checked')) {
					selectedAttributes.push(row.attr('class'));
				}
			});

			// inserts the attributes into the editor
			insertSelectedAttributes(selectedAttributes, editor);
			algorithm = editor.getSession().getValue()

			loadAlgorithmResult(algorithm);
		});

		$('#test-algorithm-btn').on('click', function() {
			loadAlgorithmResult(algorithm);
		});

		// $('#reset-algorithm-changes-btn').on('click', function() {
		// if (editor.getValue() === initialValue) {
		// return false;
		// }
		// bootbox.confirm("Do you want to revert your changes?",
		// function(result) {
		// if (result) {
		// editor.setValue(initialValue, -1);
		// insertSelectedAttributes(initialValue, editor);
		// }
		// });
		// return false;
		// });

		$('#selected-only-checkbox').on('click', function() {
			if ($(this).is(':checked')) {
				// filter the attribute-mapping-table to only show selected
				// attributes
			} else {
				// show all attributes
			}
		});

		$('#attribute-search-btn').on('click', function() {
			searchQuery = $('#attribute-search-field').val();
			// use the value of attribute-search-field to apply a filter on the
			// attribute-mapping-table
		});

		$('a[href=#map]').on('shown.bs.tab', function() {
			$("#advanced-mapping-table").load("advancedmappingeditor #advanced-mapping-table", {
				mappingProjectId : $('input[name="mappingProjectId"]').val(),
				target : $('input[name="target"]').val(),
				source : $('input[name="source"]').val(),
				targetAttribute : $('input[name="targetAttribute"]').val(),
				sourceAttribute : selectedAttributes[0]
			});
		});
	});

}($, window.top.molgenis = window.top.molgenis || {}));