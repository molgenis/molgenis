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
				targetEntityName : $('#target').val(),
				sourceEntityName : $('#source').val(),
				targetAttributeName : $('#targetAttribute').val(),
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
						targetEntityName : $('#target').val(),
						sourceEntityName : $('#source').val(),
						targetAttributeName : $('#targetAttribute').val(),
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
			mappingProjectId : $('#mappingProjectId').val(),
			target : $('#target').val(),
			source : $('#source').val(),
			targetAttribute : $('#targetAttribute').val(),
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
		if(algorithm){
			$("#advanced-mapping-table").load("advancedmappingeditor #advanced-mapping-editor", {
				mappingProjectId : $('#mappingProjectId').val(),
				target : $('#target').val(),
				source : $('#source').val(),
				targetAttribute : $('#targetAttribute').val(),
				sourceAttribute : getSourceAttrs(algorithm)[0],
				algorithm : algorithm
			});
		}
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
	 * Move suggested attributes to the top of the attribute table
	 */
	function createAttributeTable(explainedAttributes, resultTable, dataExplorerUri){
		//Remove the existing content of the result table
		resultTable.empty();
		//Add the header to the result table
		resultTable.append('<thead><tr><th>Select</th><th>Attribute</th><th>Algorithm value</th></tr></thead>');
		
		if(explainedAttributes != null){
			
			var tbody = $('<tbody />').appendTo(resultTable);
			var counter = 0;
			$.each(explainedAttributes, function(index, explainedAttribute){
				
				var attribute = explainedAttribute.attributeMetaData;
				var explainedQueryStrings = explainedAttribute.explainedQueryStrings;
				
				var row = $('<tr />').attr({
					'data-attribute-name' : attribute.name,
					'data-attribute-label' : attribute.label,
				});
				
				var attributeInfo = [];
				attributeInfo.push('<td><div class="checkbox"><label><input data-attribute-name="' + attribute.name + '" type="checkbox"></label></div></td>');
				attributeInfo.push('<td class="source-attribute-information"><b>' + attribute.label + '</b> (' + attribute.dataType + ')');
				
				if(attribute.nillable)
				{
					attributeInfo.push('<span class="label label-warning">nillable</span>');
				}
				
				if(attribute.unique)
				{
					attributeInfo.push('<span class="label label-default">unique</span>');
				}
				
				if(attribute.description)
				{
					attributeInfo.push('<br />' + attribute.description);
				}
				
				if(attribute.dataType === 'xref' || attribute.dataType === 'categorical' || attribute.dataType === 'mref')
				{
					attributeInfo.push('<br><a href="' + dataExplorerUri + '?entity=' + attribute.refEntity + '" target="_blank">category look up</a>');
				}
				
				attributeInfo.push('</td><td></td>');
				
				row.append(attributeInfo.join('')).appendTo(tbody);
				
				if(counter < 10)
				{
					if(explainedQueryStrings.length > 0)
					{
						var matchedWords = [];
						var attributeInfoElement = $(row).find('td.source-attribute-information');
						var attributeLabel = attribute.label;
						//Create a detailed explanation popover to show how the attributes get matched
						createPopoverExplanation(row, attributeInfoElement, attributeLabel, explainedQueryStrings);
						
						//Collect all matched words from all explanations
						$.each(explainedQueryStrings, function(index, explainedQueryString){
							var matchedWordsFromOneExplanation = extendPartialWord(attributeLabel, explainedQueryString.matchedWords.split(' '));
							$.each(matchedWordsFromOneExplanation, function(index, element){
								matchedWords.push(element);
							});
							
						});
						
						//Connect matched words and highlight them together
						$.each(connectNeighboredWords(attributeLabel, matchedWords), function(index, word){
							$(attributeInfoElement).highlight(word);
						});
					}
				}
				counter++;
			});
			
			checkSelectedAttributes(getAceEditor().getSession().getValue());
		}
		
		updateSearchResultMessage();
		
		bindTableCheckBoxesEvents();
	}
	
	function updateSearchResultMessage(){
		//Update the search result message above the table
		var numberOfVisibleAttributes = $('#attribute-mapping-table>tbody tr:visible').length;
		var totalNumberOfAttributes = $('#sourceAttributeSize').val();
		$('#attribute-search-result-message').empty().append(numberOfVisibleAttributes + ' attributes have been found out of ' + totalNumberOfAttributes);
		// hide/show the header of the table depending on whether or not there are any visiable attributes
		if(numberOfVisibleAttributes == 0){
			$('#attribute-mapping-table>thead tr').hide();
		}else{
			$('#attribute-mapping-table>thead tr').show();
		}
	}
	
	function getAceEditor(){
		
		var $textarea = $("#ace-editor-text-area");
		
		if(!$textarea.data('ace')){			
			// create ace editor
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
			
			$textarea.data('ace').editor.getSession().on('change', function(object) {		
				
				var algorithm = $textarea.data('ace').editor.getSession().getValue();
					// check attributes if manually added
				checkSelectedAttributes(algorithm);
				// update save buttons visibility
				disableEnableSaveButtons(algorithm);
				
				// validate mapping
				validateAttrMapping(algorithm);

				// preview mapping results
				loadAlgorithmResult(algorithm);
				
				$('#result-container').css('display', 'inline');
			});
		}	
		return $textarea.data('ace').editor;
	}
	
	function bindTableCheckBoxesEvents(){
		
		// on load use algorithm to set selected attributes and editor value
		var editor = getAceEditor();
		
		checkSelectedAttributes(editor.getSession().getValue());
		
		$('#attribute-mapping-table :checkbox').on('change', function(){
			
			var selectedAttributes = [];
			$('#attribute-mapping-table :checkbox:checked').each(function() {
				selectedAttributes.push($(this).data('attribute-name'));
			});

			// attributes into editor
			insertSelectedAttributes(selectedAttributes, editor);

			var algorithm = editor.getSession().getValue();
			
			// updates algorithm

			// events only fired when 1 or more attributes is selected
			if ($('#attribute-mapping-table :checkbox:checked').length > 0) {

				// on selection of an attribute, show all fields
				$('#result-container').css('display', 'inline');
				$('#map-tab').show();

				// generate result table
				loadAlgorithmResult(algorithm);

				// generate mapping editor if target attribute is an xref or
				// categorical
				var targetAttributeDataType = $('input[name="targetAttributeType"]').val();
				if (targetAttributeDataType === 'xref' || targetAttributeDataType === 'categorical') {
					loadMappingEditor(algorithm);
				}
			} else {
				// events when no attributes are selected
				$('#result-container').css('display', 'none');
				$('#map-tab').hide();
			}
		});
	}
	
	/**
	 * Create a boostrap popover message to show the explanation 
	 */
	function createPopoverExplanation(row, attributeInfoElement, attributeLabel, explainedQueryStrings){
		if(explainedQueryStrings.length > 0){
			var message = '', matchedWords, queryString, score;
			$.each(explainedQueryStrings, function(index, explainedQueryString){
				matchedWords = extendPartialWord(attributeLabel, explainedQueryString.matchedWords.split(' '));
				queryString = explainedQueryString.queryString;
				score = explainedQueryString.score;
				message += 'The query <strong>' + queryString + '</strong> derived from <strong>' + explainedQueryString.tagName;
				message += '</strong> is matched to the label on words <strong>' + matchedWords.join(' ').toLowerCase() + '</strong> with ' + score + '%<br><br>';
			});
			var option = {'title' : 'Explanation', 'content' : message, 'html' : true, 'placement' : 'top', 'container' : row, 'trigger' : 'hover'};
			$(attributeInfoElement).popover(option);
		}
	}
	
	/**
	 * connect the matched words that are neighbors in order to highlight them together
	 */
	function connectNeighboredWords(attributeLabel, matchedWords){
		var connectedPhrases = [], connectedPhrase, potentialConnectedPhrase, orderedMatchedWords;
		
		if(attributeLabel && matchedWords && matchedWords.length > 0){	
			attributeLabel = attributeLabel.toUpperCase();
			//Order the matched words
			orderedMatchedWords = orderMatchedWords(attributeLabel, matchedWords);
			if(orderedMatchedWords.length > 0){

				//Algorithm to connect the words that are sitting next each other
				connectedPhrase = orderedMatchedWords[0];
				for(var i = 1; i < orderedMatchedWords.length;i++){
					//Try to connect the next matched words with previous one
					potentialConnectedPhrase = connectedPhrase + ' ' + orderedMatchedWords[i];
					
					//See if the connected phrase can be found in the label string
					if(attributeLabel.indexOf(potentialConnectedPhrase) !== -1){
						connectedPhrase = connectedPhrase + ' ' + orderedMatchedWords[i];
					}else{
						connectedPhrases.push(connectedPhrase.trim());
						connectedPhrase = orderedMatchedWords[i];
					}
				}
				
				//Push the leftover phrase to the list
				if(connectedPhrase.length > 0){
					connectedPhrases.push(connectedPhrase);
				}
			}
		}
		return connectedPhrases;
	}
	/**
	 * Order the matched the words according to the order of words in the attribute label
	 */
	function orderMatchedWords(attributeLabel, matchedWords){
		var hash = {}, orderedMatchedWords = [], wordIndices = [];
		$.each(matchedWords, function(index, matchedWord){
			var index = attributeLabel.indexOf(matchedWord);
			hash[index] = matchedWord;
			wordIndices.push(index);
		});
		wordIndices.sort(function (a,b) {
		    return a - b;
		});
		$.each(wordIndices, function(i, wordIndex){
			if(hash[wordIndex]){				
				orderedMatchedWords.push(hash[wordIndex]);
			}
		});
		return orderedMatchedWords;
	}
	
	/**
	 * Explain API provides stemmed words, this method finds the 'original' word in the attribute label based the stemmed word.
	 */
	function extendPartialWord(attributeLabel, partialWords){
		var completeWords = [];
		if(attributeLabel && partialWords && partialWords.length > 0){
			$.each(partialWords, function(index, partialWord){
				if(partialWord.length > 2){
					attributeLabel = attributeLabel.toUpperCase();
					partialWord = partialWord.toUpperCase();
					var startIndex = attributeLabel.indexOf(partialWord);
					
					while(startIndex == -1 && partialWord.length > 0){
						partialWord = partialWord.substring(0, partialWord.length - 1);
						startIndex = attributeLabel.indexOf(partialWord);
					}
				
					if(startIndex != -1){
						var endIndex = startIndex + partialWord.length;
						while(attributeLabel.length > endIndex && attributeLabel.charAt(endIndex).match(/[A-Z0-9]/i)){
							endIndex++;
						}
						completeWords.push(attributeLabel.substring(startIndex, endIndex));
					}else{
						completeWords.push(partialWord)
					}
				}
			});
		}
		return completeWords;
	}
	
	//A helper function to perform post-redirect action
	function redirect(method, url, data){
		showSpinner();
		var form = '';
		if(data){
	        $.each(data, function(key, value) {
	            form += '<input type="hidden" name="'+key.replace('"', '\"')+'" value="'+value.replace('"', '\"')+'">';
	        });
		}
        $('<form action="'+url+'" method="'+ method +'">'+form+'</form>').appendTo('body').submit();
	}
	
	function saveAttributeMapping(algorithm, algorithmState) {
		$.post(molgenis.getContextUrl() + "/saveattributemapping", {
			mappingProjectId : $('#mappingProjectId').val(),
			target : $('#target').val(),
			source : $('#source').val(),
			targetAttribute : $('#targetAttribute').val(),
			algorithm : algorithm,
			algorithmState : algorithmState}, 
			function(data) {
				$('#algorithmState').empty().html(algorithmState);
				molgenis.createAlert([{message: 'This attribute mapping is saved with the state ' + algorithmState}], 'success');
				dislpayFindFirstNotCuratedAttributeMappingButton();
			}
		).fail(function() {
			$('.alerts').empty();
			molgenis.createAlert([{message: 'Error trying to save the attribuet mapping'}], 'error');
		});
	}
	
	/**
	 * The button only appears when there is a attribute to curate that has no status Discuss or Curated 
	 * find first not curated attribute mapping (Not curated and not to be discussed)
	 * If the data is unknown or similar to the displaed attribute mapping the the button will not be shown
	 */
	function dislpayFindFirstNotCuratedAttributeMappingButton(){
		$.post(molgenis.getContextUrl() + "/firstattributemapping", {
			mappingProjectId : $('#mappingProjectId').val(),
			target : $('#target').val(),
			'skipAlgorithmStates': ['DISCUSS', 'CURATED']
		}, function(data) {
			$('#find-first-to-curate-attribute-btn').remove();
			if(data.length !== 0 && ($('#targetAttribute').val() !== data.targetAttribute || $('#source').val() !== data.source)) {
				$('#attribute-mapping-toolbar').append($('<button id="find-first-to-curate-attribute-btn" type="btn" class="btn btn-default btn-xs">Next attribute to curate<span class="glyphicon glyphicon-chevron-right"></span></button>'));
				$('#find-first-to-curate-attribute-btn').on('click', function() {
					redirect('get', molgenis.getContextUrl() + '/attributeMapping', data);
				});
			}
		});
	}
	
	/**
	 * When the save buttons: 'save curated' and 'save to discuss' are clicked the algorithm must be filled in.
	 */
	function disableEnableSaveButtons(algorithm){
		if(null != algorithm && algorithm.length > 0){
			$('#save-mapping-btn').prop('disabled', false);
			$('#save-discuss-mapping-btn').prop('disabled', false);
		}else{
			$('#save-mapping-btn').prop('disabled', true);
			$('#save-discuss-mapping-btn').prop('disabled', true);
		}
	}
	
	/**
	 * Get the relevant attributes
	 * 
	 * searchTerms
	 * 		if empty uses tags
	 * 		if not empty uses key words
	 */
	function findRelevantAttributes(requestBody, resultTable, dataExplorerUri){
		requestBody["searchTerms"] = $('#attribute-search-field').val();
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/attributeMapping/semanticsearch',
			data : JSON.stringify(requestBody),
			contentType : 'application/json',
			success : function(relevantAttributes) {
				createAttributeTable(relevantAttributes, resultTable, dataExplorerUri);
			}
		});
	}

	$(function() {

		// Initialize all variables
		var aceEditor, algorithm, requestBody = {
			'mappingProjectId' : $('[name="mappingProjectId"]').val(),
			'target' : $('[name="target"]').val(),
			'source' : $('[name="source"]').val(),
			'targetAttribute' : $('[name="targetAttribute"]').val(),
			'searchTerms' : ""
		}, explainedAttributes, attributes = [];

		// Hide the map tab when no attributes are selected
		if ($('#attribute-mapping-table :checkbox:checked').length < 1) {
			$('#map-tab').hide();
		}
		
		// tooltip placement
		$("[rel=tooltip]").tooltip({
			placement : 'right'
		});
		
		$('.ontologytag-tooltip').css({'cursor':'pointer'}).popover({'html':true, 'placement':'right', 'trigger':'hover'});

		findRelevantAttributes(requestBody, $('#attribute-mapping-table'), $('#dataExplorerUri').val());
		
		aceEditor = getAceEditor();
		
		algorithm = aceEditor.getSession().getValue();
		
		// if there is an algorithm present on load, show the result table
		if (algorithm.trim()) {
			loadAlgorithmResult(algorithm);
		} else {
			// if no algorithm present hide the mapping and result containers
			$('#result-container').css('display', 'none');
		}
		
		// save button for saving generated mapping
		$('#save-mapping-btn').on('click', function() {saveAttributeMapping(aceEditor.getSession().getValue(), "CURATED")});
		
		// save button for discuss status generated mapping
		$('#save-discuss-mapping-btn').on('click', function() {saveAttributeMapping(aceEditor.getSession().getValue(), "DISCUSS")});
		
		// Update save buttons visibility
		disableEnableSaveButtons(algorithm);
		
		// Display next button
		dislpayFindFirstNotCuratedAttributeMappingButton();

		$('#js-function-modal-btn').on('click', function() {
			$('#js-function-modal').modal('show');
		});

		// Using the semantic search functionality from the server
		$('#attribute-search-field-button').on('click', function(e) {
			findRelevantAttributes(requestBody, $('#attribute-mapping-table'), $('#dataExplorerUri').val());
		});
		
		// Bind keydown and keyup event to the textField to prevend the form from being submitted
		$('#attribute-search-field').keydown(function(event) {
			if(event.keyCode === 13){
		      event.preventDefault();
		      $('#attribute-search-field-button').trigger('click');
		      return false;
		    }
		}).keyup(function(){
			if($(this).val() === ''){
				$('#attribute-search-field-button').trigger('click');
			}
		});
		
		// when the map tab is selected, load its contents
		// loading on page load will fail because bootstrap tab blocks it
		$('a[href=#map]').on('shown.bs.tab', function() {
			loadMappingEditor(aceEditor.getSession().getValue());
		});

		$('a[href=#script]').on('shown.bs.tab', function() {
		// when users switch back to the script tab, the value in the editor is not updated
		// until the editor is clicked. A workaround is to move the page by calling the method to
		// flush the changes. 
			aceEditor.scrollPageDown();
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

			var categoricalAlgorithm = generateAlgorithm(mappedCategoryIds, $('input[name="sourceAttribute"]').val(), defaultValue, nullValue);
			getAceEditor().setValue(categoricalAlgorithm);
			loadAlgorithmResult(categoricalAlgorithm);
		});
	});

}($, window.top.molgenis = window.top.molgenis || {}));