(function($, molgenis) {
	"use strict";

	/**
	 * Generate an algorithm based on category selections
	 * 
	 * @param mappedCategoryIds
	 *            a list of category identifiers
	 * @param attribute
	 *            the source attribute
	 * @param defaultValue
	 *            The value used as a default value
	 * @param nullValue
	 *            The value used for missing
	 */
	function generateAlgorithm(mappedCategoryIds, attribute, defaultValue, nullValue) {
		var algorithm;
		if (nullValue !== undefined) {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ", " + JSON.stringify(defaultValue) + ", " + JSON.stringify(nullValue)
					+ ").value();";
		} else if (defaultValue !== undefined) {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ", " + JSON.stringify(defaultValue) + ").value();";
		} else {
			algorithm = "$('" + attribute + "').map(" + JSON.stringify(mappedCategoryIds) + ").value();";
		}
		return algorithm;
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

	var timeoutId, isValidating;

	/**
	 * Validate algorithm on source entities, render UI component that displays
	 * the number of total/success/errors and corresponding error messages.
	 * Stops after a max. number of validation errors.
	 */
	function validateAttrMapping(algorithm) {
		var validationDelay = 2000;
		var validationBatchSize = 500;
		var validationMaxErrors = 100;

		isValidating = false;
		if (timeoutId) {
			clearTimeout(timeoutId);
		}

		$('#mapping-validation-container').html('<span>Pending ...</span>');
		$('#validation-error-messages-table-body').empty();
		timeoutId = setTimeout(
				function() {
					isValidating = true;

					var request = {
						targetEntityName : $('input[name="target"]').val(),
						sourceEntityName : $('input[name="source"]').val(),
						targetAttributeName : $('input[name="targetAttribute"]').val(),
						algorithm : algorithm
					};

					var items = [];
					items.push('<img id="validation-spinner" src="/css/select2-spinner.gif">&nbsp;');
					items.push('<span class="label label-default">Total: <span id="validation-total">?</span></span>&nbsp;');
					items.push('<span class="label label-success">Success: <span id="validation-success">0</span></span>&nbsp;');
					items
							.push('<span class="label label-danger"><a class="validation-errors-anchor" href="#validation-error-messages-modal" data-toggle="modal" data-target="#validation-error-messages-modal">Errors: <span id="validation-errors">0</span></a></span>&nbsp;');
					items.push('<em class="hidden" id="max-errors-msg">(Validation aborted, encountered too many errors)</em>');
					$('#mapping-validation-container').html(items.join(''));
					validateAttrMappingRec(request, 0, validationBatchSize, 0, 0, validationMaxErrors);
				}, validationDelay);

	}

	function validateAttrMappingRec(request, offset, num, nrSuccess, nrErrors, validationMaxErrors) {
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/validateAttrMapping',
			data : JSON.stringify(_.extend({}, request, {
				offset : offset,
				num : num
			})),
			showSpinner : false,
			contentType : 'application/json'
		}).done(function(data) {
			nrSuccess += data.nrSuccess;
			nrErrors += data.nrErrors;

			if (offset + num >= data.total || nrErrors >= validationMaxErrors) {

				$('#validation-spinner').hide();
			}
			$('#validation-total').html(data.total);
			$('#validation-success').html(nrSuccess);
			$('#validation-errors').html(nrErrors);

			if (nrErrors > 0) {

				_.each(data.errorMessages, function(message, id) {
					$('#validation-error-messages-table-body').append('<tr><td>' + id + '</td><td>' + message + '</td></tr>');
				});
			}

			if (nrErrors >= validationMaxErrors) {
				$('#max-errors-msg').removeClass('hidden');
				return;
			}

			if (offset + num < data.total) {
				if (isValidating) {

					validateAttrMappingRec(request, offset + num, num, nrSuccess, nrErrors, validationMaxErrors);
				} else {
					$('#mapping-validation-container').html('<span>Pending ...</span>');
				}
			}
		});
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
		}, function() {
			$('.show-error-message').on('click', function() {
				$('#algorithm-error-message-container').html($(this).data('message'));
			});
		});
	}

	/**
	 * Load mapping table from view-advanced-mapping-editor.ftl
	 * 
	 * @param algorithm
	 *            The algorithm to set presets when opening the editor a second
	 *            time
	 */
	function loadMappingEditor(algorithm) {
		$("#advanced-mapping-table").load("advancedmappingeditor #advanced-mapping-editor", {
			mappingProjectId : $('input[name="mappingProjectId"]').val(),
			target : $('input[name="target"]').val(),
			source : $('input[name="source"]').val(),
			targetAttribute : $('input[name="targetAttribute"]').val(),
			sourceAttribute : getSourceAttrs(algorithm)[0],
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
			var name = $(this).data('attribute-name'), inArray = $.inArray(name, sourceAttrs);
			$(this).prop('checked', inArray >= 0);
		});
	}

	/**
	 * Clears the editor and inserts selected attributes.
	 * 
	 * @param selectedAttributes
	 *            all the selected attributes
	 * @param editor
	 *            the ace algorithm editor to insert the attribute into
	 */
	function insertSelectedAttributes(selectedAttributes, editor) {
		var existingAlgorithm = editor.getSession().getValue(), newAttributes = [], existingAttributes = getSourceAttrs(existingAlgorithm);
		$(selectedAttributes).each(function() {
			if (existingAlgorithm.indexOf(this) === -1) {
				insertAttribute(this, editor);
			}
		});

		$(existingAttributes).each(function() {
			if (selectedAttributes.indexOf(this) === -1) {
				removeAttribute(this, editor);
			}
		});
	}

	/**
	 * Inserts a single attribute
	 * 
	 * @param attribute
	 *            One attribute to insert into the editor
	 * @param editor
	 *            the ace algorithm editor to insert the attribute into
	 */
	function insertAttribute(attribute, editor) {
		editor.insert("$('" + attribute + "').value();");
	}

	/**
	 * Removes a single attribute
	 * 
	 * @param attribute
	 *            One attribute to remove from the editor
	 * @param editor
	 *            the ace algorithm editor to remove the attribute from
	 */
	function removeAttribute(attribute, editor) {
		// TODO Fix removing algorithms that contain more then just .value()
		// (like .map())
		editor.replaceAll("", {
			needle : "$('" + attribute + "').value();"
		});
		editor.replaceAll("", {
			needle : "$('" + attribute + "')"
		});
	}

	/**
	 * Hides rows of the table if atrribute source labels, names, descriptions
	 * and tags have nothing to do with the query, hide the row
	 */
	function filterAttributeTable(explainedAttributes, attributes) {
		var searchQuery = $('#attribute-search-field').val().toLowerCase(), attrLabel, attrName, attrDescription, explainedQueryStrings, words, row;
		if (searchQuery === '') {
			$('#attribute-mapping-table>tbody').find('tr').each(function() {
				row = $(this);
				if (attributes !== null) {
					if (attributes.indexOf($(this).data('attribute-name').toLowerCase()) > -1) {
						explainedQueryStrings = explainedAttributes[row.data('attribute-name')];
						row.show();
						$(explainedQueryStrings).each(function() {
							words = this.matchedWords.split(' ');
							$(words).each(function() {
								$(row).find('td.source-attribute-information').highlight(this);
							});
						});
					} else {
						row.hide();
					}
				} else {
					row.show();
				}
			});
		} else {
			$('#attribute-mapping-table>tbody').find('tr').each(function() {
				attrLabel = $(this).data('attribute-label').toLowerCase();
				attrName = $(this).data('attribute-label').toLowerCase();
				attrDescription = $(this).find('td.source-attribute-information').text().toLowerCase();

				$(this).show();

				if (attrLabel.indexOf(searchQuery) < 0 && attrName.indexOf(searchQuery) < 0 && attrDescription.indexOf(searchQuery) < 0) {
					$(this).hide();
				}
			});
		}
	}

	$(function() {

		// Initialize all variables
		var editor, searchQuery, selectedAttributes, initialValue, algorithm, targetAttributeDataType, $textarea, requestBody = {
			'mappingProjectId' : $('[name="mappingProjectId"]').val(),
			'target' : $('[name="target"]').val(),
			'source' : $('[name="source"]').val(),
			'targetAttribute' : $('[name="targetAttribute"]').val()
		}, explainedAttributes, attributes = [];

		// tooltip placement
		$("[rel=tooltip]").tooltip({
			placement : 'right'
		});

		// Get the explained attributes
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/attributeMapping/explain',
			data : JSON.stringify(requestBody),
			contentType : 'application/json',
			success : function(result) {
				explainedAttributes = result;

				// Create an array of attributes which are explained
				for ( var key in explainedAttributes) {
					attributes.push(key.toLowerCase());
				}

				// Set attributes array to null if it is empty
				if (attributes.length < 1) {
					attributes = null;
				}

				// Call the filterAttributeTable to only show the attributes
				// that are explained
				filterAttributeTable(explainedAttributes, attributes);
			}
		});

		// create ace editor
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
			highlightActiveLine : true
		});
		editor = $textarea.data('ace').editor;

		// on load use algorithm to set selected attributes and editor value
		checkSelectedAttributes(initialValue);
		algorithm = editor.getSession().getValue();

		editor.getSession().on('change', function() {
			// check attributes if manually added
			checkSelectedAttributes(editor.getValue());

			// update algorithm
			algorithm = editor.getSession().getValue();

			// validate mapping
			validateAttrMapping(algorithm);

			// preview mapping results
			loadAlgorithmResult(algorithm);
		});

		// if there is an algorithm present on load, show the result table
		if (algorithm.trim()) {
			loadAlgorithmResult(algorithm);
		} else {
			// if no algorithm present hide the mapping and result containers
			$('#attribute-mapping-container').css('display', 'none');
			$('#result-container').css('display', 'none');
		}

		// page update on attribute selection / deselection
		$('#attribute-mapping-table :checkbox').on('change', function() {
			selectedAttributes = [];

			$('#attribute-mapping-table :checkbox:checked').each(function() {
				selectedAttributes.push($(this).data('attribute-name'));
			});

			// attributes into editor
			insertSelectedAttributes(selectedAttributes, editor);

			// updates algorithm
			algorithm = editor.getSession().getValue();

			// events only fired when 1 or more attributes is selected
			if ($('#attribute-mapping-table :checkbox:checked').length > 0) {

				// on selection of an attribute, show all fields
				$('#result-container').css('display', 'inline');
				$('#attribute-mapping-container').css('display', 'inline');

				// generate result table
				loadAlgorithmResult(algorithm);

				// generate mapping editor if target attribute is an xref or
				// categorical
				targetAttributeDataType = $('input[name="targetAttributeType"]').val();
				if (targetAttributeDataType === 'xref' || targetAttributeDataType === 'categorical') {
					loadMappingEditor(algorithm);
				}
			} else {
				// events when no attributes are selected
				$('#result-container').css('display', 'none');
				$('#attribute-mapping-container').css('display', 'none');
			}
		});

		// save button for saving generated mapping
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

		$('#js-function-modal-btn').on('click', function() {
			$('#js-function-modal').modal('show');
		});

		// look for attributes in the attribute table
		$('#attribute-search-field').on('onkeydown onpaste oninput change keyup', function(e) {
			filterAttributeTable(explainedAttributes, attributes);
		});

		// when the map tab is selected, load its contents
		// loading on page load will fail because bootstrap tab blocks it
		$('a[href=#map]').on('shown.bs.tab', function() {
			loadMappingEditor(algorithm);
		});

		$('a[href=#script]').on('shown.bs.tab', function() {
			// Clearing the editor will empty the algorithm
			var newAlgorithm = algorithm;
			editor.setValue("");
			editor.insert(newAlgorithm, -1);
		});

		$('#advanced-mapping-table').on('change', function() {
			var mappedCategoryIds = {}, defaultValue = undefined, nullValue = undefined, key, val;

			// for each source xref value, check which target xref value
			// was chosen
			$('#advanced-mapping-table > tbody > tr').each(function() {
				key = $(this).attr('id');
				val = $(this).find('option:selected').val();
				if (key === 'nullValue') {
					if (val !== 'use-default-option') {
						if (val === 'use-null-value') {
							nullValue = null;
						} else {
							nullValue = val;
						}
					}
				} else {
					if (val !== 'use-default-option') {
						if (val === 'use-null-value') {
							mappedCategoryIds[$(this).attr('id')] = null;
						} else {
							mappedCategoryIds[$(this).attr('id')] = val;
						}
					}
				}
			});

			if (nullValue !== undefined) {
				defaultValue = null;
			}

			if ($('#default-value').is(":visible")) {
				defaultValue = $('#default-value').find('option:selected').val();
				if (defaultValue === 'use-null-value') {
					defaultValue = null;
				}
			}

			algorithm = generateAlgorithm(mappedCategoryIds, $('input[name="sourceAttribute"]').val(), defaultValue, nullValue);
			loadAlgorithmResult(algorithm);
		});
	});

}($, window.top.molgenis = window.top.molgenis || {}));