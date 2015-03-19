/**
 * Disease Matcher.
 * A module for the Data Explorer of Molgenis to enable easy browsing through diseases within a dataset with variants or 
 * other data that have gene symbols associated with them. Disease information is retrieved from the OMIM API and the referencing is
 * made possible by the DiseaseMapping and Disease datasets (see org.molgenis.diseasematcher.data.preprocess for more information on
 * these datasets, where to get them and how to process them).
 * 
 * @author Tommy de Boer
 */

(function($, molgenis) {

	var restApi = new molgenis.RestClient();

	/**
	 * Regular expression containing all line break flavors, used for replacing line breaks.
	 */
	var lineBreaks = /(?:\r\n|\r|\n)/g;

	/**
	 * Types of selection modes, Enum style.
	 */
	var SelectionMode = {
		DISEASE : 'diseases',
		GENE : 'genes',
		PATIENT: 'patient'
	};
	
	/**
	 * Global for keeping track of the current selection mode.
	 */
	var currentSelectionMode = SelectionMode.GENE;
	
	/**
	 * Sets an upper limit to the amount of items visible in the selection list.
	 */
	var listMaxLength = 10;
	
	/**
	 * True by default and set to false when one of the tool availability checks fail.
	 */
	var toolAvailable = true;
	
	/**
	 * Global keeping storing if a browser support local storage.
	 */
	var localStorageSupported = false;
	
	/**
	 * Sets the name of the column that contains gene symbols. In the future this column should be dynamically determined per dataset.
	 */
	var geneSymbolColumn = 'geneSymbol';
	
	var phenotipsFilteredQuery;
	
	var currentQuery;
	
	/**
	 * Listens for data explorer query changes and updates the selected gene/disease lists.
	 */
	$(document).on('changeQuery', function(e, query) {	
		updateSelectionList(currentSelectionMode);
	});
	
	$(document).on('dataChange.data', function(e) {
		//TODO: implement table refresh
		$('#diseasematcher-variant-panel').table('setQuery', currentQuery);
	});
	
	//disease matcher parts
	var infoPanel = $('#diseasematcher-infopanel');
	var patientPanel = $('#diseasematcher-patientpanel');
	var selectionContainer = $('#disease-selection-container');
	var selectionList = $('#diseasematcher-selection-list');
	var selectionNav = $("#diseasematcher-selection-navbar-nav");
	var selectionTitle = $('#diseasematcher-selection-title');
	
	//handlebars templates
	var hbSelectionList = $('#hb-selection-list');
	var hbClinicalSynopsis = $('#hb-clinical-synopsis');
	var hbClinicalSynopsisComp = Handlebars.compile(hbClinicalSynopsis.html());

	
	/**
	 * Listens for attribute selection changes and updates the variant table.
	 */
	$(document).on('changeAttributeSelection', function(e, data) {
		variantPanel = $('#diseasematcher-variant-panel');
		if (!variantPanel.is(':empty')) {	
			variantPanel.table('setAttributes', data.attributes);
		}
	});
	
	/**
	 * Checks if the current state of Molgenis meets the requirements of this tool. 
	 * Checks if the needed datasets are loaded (DiseaseMapping and Disease) and if the user's dataset has 
	 * a gene symbol column. All calls are async to avoid hanging of the application.
	 *
	 * @param entityUri the URI of the entity to check for a gene symbol column
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function checkToolAvailable(entityUri) {
		checkDatasetAvailable('Disease');
		checkDatasetAvailable('DiseaseMapping');

		// if an entity is selected, check if it has a gene symbol column and show a warning if it does not
		restApi.getAsync(entityUri + '/meta', {}, function(data) {
			if (data === null || !data.attributes.hasOwnProperty(geneSymbolColumn)) {			
				molgenis.createAlert([{
					message: 'No geneSymbol column found!</strong> For this tool to work, make sure your dataset has a <em>geneSymbol</em> column.'}], 
					'warning',
					infoPanel);
				
				toolAvailable = false;
			}
		});
	}

	
	/**
	 * Checks if a dataset is loaded. Shows a warning when it is not. 
	 * 
	 * @param dataset the dataset to check for
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function checkDatasetAvailable(dataset) {
		restApi.getAsync('/api/v1/' + dataset, {'num' : 1},	function(data){
			if (data.total === 0) {
				molgenis.createAlert([{
					message: '<strong>' + dataset + ' not loaded!</strong> For this tool to work, please upload a valid <em>' + dataset + '</em> dataset.'}], 
					'warning',
					infoPanel);
				
				toolAvailable = false;
			}
		});
	}

	/**
	 * Click action for the navigation buttons in the Disease Matcher selection panel.
	 * 
	 * @param element the selected link
	 * @selectionMode the appropriate selection mode to use
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function onClickNavBtn(element, selectionMode){
		if (!toolAvailable) return;
		
		//reset navbar and activate the clicked button
		selectionNav.find('li').removeClass('active');
		$(element).parent().attr("class", "active");
		
		if (selectionMode === SelectionMode.PATIENT){
			selectionContainer.hide();
			infoPanel.hide();
			patientPanel.show();
		}else{		
			selectionContainer.show();
			patientPanel.hide();
			infoPanel.show();
			
			// update title of selection list
			selectionTitle.html(selectionMode.charAt(0).toUpperCase() + selectionMode.slice(1));
	
			currentSelectionMode = selectionMode;
			updateSelectionList(selectionMode);
		}
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function getPhenotipsSuggestions(terms, callback){
		var queryString = '';
		$.each(terms, function(index, t){
			if (index > 0){
				queryString += '&';
			}else{
				queryString += '?';
			}
			t = encodeURIComponent(t);
			queryString += 'symptom=' + t;
		});
			
		$.ajax({
			type : 'GET',
			contentType : 'text/html',
			url : '/phenotips' + queryString,
			success : function(data) {
				callback(data);
			}
		});
		
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function filterPhenotipsOutputComplete(terms){
		var entityName = getEntity().name;
		var request = {
				'datasetName' : entityName,
				'num' : 1000,
				'start' : 0
			};
			$.extend(request, getQuery());
		
		// get diseases from dataset
		$.ajax({
			type : 'POST',
			contentType : 'application/json',
			url : '/diseasematcher/diseases',
			data : JSON.stringify(request),
			success : function(diseases) {
				
				if (diseases.total === 0) return;
				
				//call Phenotips and put the suggestions in a hidden div
				getPhenotipsSuggestions(terms, function(suggestions){		
					
					var hiddenOutput = $('#diseasematcher-phenotips-hiddenoutput');			
					hiddenOutput.hide();
					hiddenOutput.empty();
					hiddenOutput.html(suggestions);
					
					// reference the list of suggestions so we can look through them
					var suggestionItems = $('#diseasematcher-phenotips-hiddenoutput ul li');
					
					// find each omim id and omim name from the list of suggestions
					var suggestionObjects = [];
					$.each(suggestionItems, function(i, suggestedDisorder){
						var omimId = $(suggestedDisorder).find('.id')[0];
						omimId = $(omimId).attr('title');
						
						//extract omim name from the item and remove the omim id from it
						//also prepare all names for string matching (removing disease types)
						var omimName = $(suggestedDisorder).find('.title');
						var originalName = $(omimName).html();
						matchingName = originalName.replace(/^[^a-zA-Z]+/, '').replace(/\b\S{1,3}\b/g, '');
						
						var suggestion = {
								'originalName' : originalName,
								'matchingName' : matchingName
								}
						
						suggestionObjects[omimId] = suggestion;
					});
					
					if (suggestionObjects.length === 0){
						molgenis.createAlert([{
							message: 'PhenoTips is offline or (one of) the HPO terms you entered is not valid!'}], 
							'warning',
							$('.diseasematcher-warnings'));
						return;
					}
					
					// group diseases in perfect/similar matches and diseases that will be excluded 
					var exclude = [];
					var includePerfect = [];
					var includeSimilar = [];
					$.each(diseases.diseases, function(i, dis){
						
						if (dis.diseaseId.substring(5) in suggestionObjects){
							// perfect match, include this disease
							includePerfect.push(dis);
						}else{
							// no perfect match found, try to find similar diseases
							var disNameBroad = dis.diseaseName.replace(/\b\S{1,3}\b/g, '').toUpperCase();
							var found = false;
							dis['matches'] = [];
							for(var k in suggestionObjects) {
							    if(suggestionObjects[k].matchingName === disNameBroad) {
							        dis.matches.push(suggestionObjects[k].originalName);
							        found = true;							        
							    } 
							}
							
							if (found === false){
								exclude.push(dis);
							}else{
								includeSimilar.push(dis);
							}
						}
					});
					
					includeIds = [];
					
					// print output
					var outputDivPerfect = $('#diseasematcher-filter-perfect');
					var outputDivSimilar = $('#diseasematcher-filter-similar');
					var outputDivNo = $('#diseasematcher-filter-no');
					outputDivPerfect.empty();
					outputDivSimilar.empty();
					outputDivNo.empty();
					includePerfect.forEach(function(dis){
						var itemLine = '<font color="green"><strong>' + dis.diseaseId + '</strong> ' + dis.diseaseName + '</font></br>';
						outputDivPerfect.append(itemLine);
						includeIds.push(dis.diseaseId);
					});
					$('#perfectMatchTitle').text("Perfect matches (" + includePerfect.length + ")");
					
					outputDivSimilar.css("color", "orange");
					outputDivSimilar.append('<ul class="rootlist" style="list-style-type:none;"></ul>');
					list = outputDivSimilar.find('ul');
					
					includeSimilar.forEach(function(dis){
						var itemLine = '<li><strong>' + dis.diseaseId + '</strong> ' + dis.diseaseName + '<ul style="list-style-type:none; color: grey;"></ul></li>';
						list.append(itemLine);
						
						lastItem = $('ul.rootlist > li ul').last();
						for (m in dis.matches){
							lastItem.append('<li><em>' + dis.matches[m] + '</em></li>');
						}
						includeIds.push(dis.diseaseId);
					});					
					$('#similarMatchTitle').text("Similar matches (" + includeSimilar.length + ")");
					
					exclude.forEach(function(dis){
						var itemLine = '<font color="red"><strong>' + dis.diseaseId + '</strong> ' + dis.diseaseName + '</font></br>';
						outputDivNo.append(itemLine);
					});
					$('#noMatchTitle').text("No matches (" + exclude.length + ")");
					
					// get genes from DiseaseMapping for every OMIM id and use these genes to filter the patient variants
					var queryRules = [];
					$.each(includeIds, function(index, omimId) {
						if (queryRules.length > 0) {
							queryRules.push({
								'operator' : 'OR'
							});
						}
						queryRules.push({
							'field' : 'diseaseId',
							'operator' : 'EQUALS',
							'value' : omimId
						});
					});
					
					restApi.getAsync('/api/v1/DiseaseMapping', {
						'q' : {
							'q' : queryRules,
							'num' : 10000
						},
						'attributes' : [ 'geneSymbol']
						
					}, function(diseaseGenes) {
						
						// get unique genes for this disease
						var uniqueGenes = [];
						$.each(diseaseGenes.items, function(index, disease) {
							if ($.inArray(disease.geneSymbol, uniqueGenes) === -1) {
								uniqueGenes.push(disease.geneSymbol);
							}
						});
						
						showVariants(uniqueGenes, $('#diseasematcher-filtered-variants'), getQuery());
						
						var filterRules = [];
						$.each(uniqueGenes, function(index, gene) {
							if (filterRules.length > 0) {
								filterRules.push({
									'operator' : 'OR'
								});
							}
							filterRules.push({
								'field' : geneSymbolColumn,
								'operator' : 'EQUALS',
								'value' : gene
							});
						});
						
						phenotipsFilteredQuery = [filterRules];
						
						$('#diseasematcher-download-button').show();
						$('#diseasematcher-filter-output').show();
						
						
					});
				});
				
			}
		});
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function downloadFilteredVariants() {
		$.download(molgenis.getContextUrl() + '/download', {
			// Workaround, see http://stackoverflow.com/a/9970672
			'dataRequest' : JSON.stringify(createDiseaseMatcherDownloadDataRequest())
		});
		
		$('#downloadModal').modal('hide');
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function createDiseaseMatcherDownloadDataRequest() {
		var entityQuery = getQuery();
		
		var dataRequest = {
			entityName : getEntity().name,
			attributeNames: [],
			query : {
				rules : phenotipsFilteredQuery
			},
			colNames : 'ATTRIBUTE_LABELS'
		};

		//dataRequest.query.sort = $('#data-table-container').table('getSort');	
		var colAttributes = molgenis.getAtomicAttributes(getAttributes(), restApi);
		
		$.each(colAttributes, function() {
			var feature = this;
			dataRequest.attributeNames.push(feature.name);
			if (feature.fieldType === 'XREF' || feature.fieldType === 'MREF' || feature.fieldType === 'CATEGORICAL' || feature.fieldType === 'CATEGORICAL_MREF')
				dataRequest.attributeNames.push("key-" + feature.name);
		});

		return dataRequest;
	}
	
	/**
	 * On document ready.
	 */
	$(function() {
		checkToolAvailable('/api/v1/' + getEntity().name);
		
		// bind button actions
		$('#diseasematcher-diseases-select-button').click(function(e) {
			e.preventDefault();
			onClickNavBtn(this, SelectionMode.DISEASE);
			
		});
		
		var genesBtn = $('#diseasematcher-genes-select-button').click(function(e) {
			e.preventDefault();
			onClickNavBtn(this, SelectionMode.GENE);
		});
		
		$('#diseasematcher-patient-select-button').click(function(e){
			e.preventDefault();
			onClickNavBtn(this, SelectionMode.PATIENT);
		});
		
		$('#btn-filter-phenotips').click(function(e){
			e.preventDefault();
			filterPhenotipsOutput();
		});
		
		$('#btn-filter-phenotips-string').click(function(e){
			e.preventDefault();
			filterPhenotipsOutputStringMatched();
		});
		
		$('#btn-filter-phenotips-complete').click(function(e){
			e.preventDefault();
			var terms = $('#hpoTermsInput').val();
						
			if (/^(HP:\d{7})(,HP:\d{7})*$/.test(terms) === false){
				molgenis.createAlert([{
					message: 'Incorrect input. Make sure to use HPO terms and separate them with a comma (without spaces).'}], 
					'warning',
					$('.diseasematcher-warnings'));	
			}else{
				terms = terms.split(',');		
				filterPhenotipsOutputComplete(terms);
			}
		});
		
		$('#diseasematcher-download-button').click(function() {
			downloadFilteredVariants();
		});

		// init tabs
		$('#diseasematcher-disease-panel-tabs').tab();

		// TODO: local storage
		localStorageSupported = browserSupportsLocalStorage();
		if (localStorageSupported) localStorage.clear();
		localStorageSupported = false; // switched off for now
		
		// TODO: make this process nicer (multiple async calls are confusing)
		// wait till the checkToolAvailable calls are done. if tool is available pre-select the genes button
		$(document).ajaxStop(function() {
		    $(this).unbind("ajaxStop"); //prevent running again when other calls finish
		    
		    if (toolAvailable) onClickNavBtn($('#diseasematcher-genes-select-button'), SelectionMode.GENE);
		    
		});
	});

	
	/**
	 * Gets diseases/genes from the server for this dataset based on the current data explorer query and triggers 
	 * refreshing of the selection list.
	 * 
	 * @param selectionMode 
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function updateSelectionList(selectionMode) {
		var entityName = getEntity().name;
		var request = {
			'datasetName' : entityName,
			'num' : listMaxLength,
			'start' : 0
		};
		$.extend(request, getQuery());
		$.ajax({
			type : 'POST',
			contentType : 'application/json',
			url : '/diseasematcher/' + selectionMode,
			data : JSON.stringify(request),
			success : function(data) {
			
				// add the diseases/genes (if any) to the list and init the pager
				if (selectionMode === SelectionMode.DISEASE) {				
					populateSelectionList(data.diseases, selectionMode);
				} else if (selectionMode === SelectionMode.GENE) {
					populateSelectionList(data.genes, selectionMode);		
				}
				
				initPager(data.total, selectionMode);
			}
		});
	}

	
	/**
	 * Adds items to the selection list and sets the click actions for each item. 
	 * 
	 * @param list the list of diseases or genes
	 * @param selectionMode the current selection mode
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function populateSelectionList(list, selectionMode) {
		//TODO handle nulls
		
		var template = Handlebars.compile(hbSelectionList.html());
		var selectionListFilled = template(list);
		selectionList.html(selectionListFilled);

		if (list === null || list.length === 0) {
			// no genes/diseases found, empty the view, disable phenotips filter button
			$('#btn-filter-phenotips-complete').prop('disabled', true);
			$('#diseasematcher-variant-panel').empty();
			$('#diseasematcher-disease-panel').hide();
			return;
		}else{
			$('#btn-filter-phenotips-complete').prop('disabled', false);
			$('#diseasematcher-disease-panel').show();
		}

		selectionList.find('a').click(function(e) {
			e.preventDefault(); // stop jumping to top of page
			onSelectListItem($(this), currentSelectionMode);
		});

		// pre-select top-most disease
		onSelectListItem(selectionList.find('li a').first(), currentSelectionMode);
	}

	
	/**
	 * Initializes a pager for the currently selected diseases. 
	 * Needs a reference to selectionMode to pass to onPageChange().
	 * 
	 * @param total the total amount of items 
	 * @param selectionMode the selection mode for this list
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function initPager(total, selectionMode) {
		var container = $('#diseasematcher-selection-pager');
		if (total > listMaxLength) {
			container.pager({
				'nrItems' : total,
				'nrItemsPerPage' : listMaxLength,
				'onPageChange' : function(page) {
					onPageChange(page.start, page.end, selectionMode);
				}
			});
			container.show();
		} else
			container.hide();
	}

	
	/**
	 * Called when the selection list page changes. Retrieves a slice of the list from the server and 
	 * triggers an update of the selection list.
	 * 
	 * @param pageStart index of the first item of this page
	 * @param pageEnd index of the last item of this page
	 * @param selectionMode the selection mode for this list
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function onPageChange(pageStart, pageEnd, selectionMode) {
		var entityName = getEntity().name;
		var request = {
				'datasetName' : entityName,
				'num' : pageEnd - pageStart,
				'start' : pageStart
			};
			$.extend(request, getQuery());
		
		$.ajax({
			type : 'POST',
			contentType : 'application/json',
			url : '/diseasematcher/' + selectionMode,
			data : JSON.stringify(request),
			success : function(data) {
				
				if (selectionMode === SelectionMode.DISEASE) {
					populateSelectionList(data.diseases, selectionMode);
				} else if (selectionMode === SelectionMode.GENE) {
					populateSelectionList(data.genes, selectionMode);
				}
			}
		});
	}

	
	/**
	 * Takes an OMIM object and transforms the data to content which is shown in a disease tab.
	 * 
	 * @param omimObject the OMIM object to show
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function showOmimObject(omimObject){
		
		// clear disease panel to make room for this object
		var diseasePanel = $('#diseasematcher-disease-tab-content');
		diseasePanel.empty();
		
		// show a warning when omim object is empty (500 from OMIM API or other problem) and return
		if (omimObject === null){
			var warning = '<div class="alert alert-warning" id="no-clinical-synopsis-warning">'
				+ '<strong>OMIM service unavailable!</strong> Please visit <a href="http://www.omim.org/entry/' + diseaseId + '" target="_blank">the OMIM website.</a></div>';
			diseasePanel.append(warning);
			return;
		}
		
		var entry = omimObject.omim.entryList[0].entry;
		
		// CLINICAL SYNOPSIS
		diseasePanel.append('<h3>Clinical Synopsis</h3>');
		var synopsisParagraph = $('<p></p>').appendTo(diseasePanel);
		
		if ('clinicalSynopsis' in entry){
			var clinicalSynopsis = entry.clinicalSynopsis;
			
			// in some cases phenotypes are wrapped in an 'oldFormat' object
			if ('oldFormat' in clinicalSynopsis){
				clinicalSynopsis = clinicalSynopsis.oldFormat;
			}
			var allPhenotypes = [];
			var inheritancePhenotypes = [];
			for (var propt in clinicalSynopsis){
				// remove links, id's between { and }, and linebreaks
				var phenotypes = clinicalSynopsis[propt].replace(/ *\{[^}]*\} */g, '');
				phenotypes = phenotypes.split(";");
				
				if (propt.toUpperCase() === 'INHERITANCE'){
					for (var phen in phenotypes){
						inheritancePhenotypes.push(phenotypes[phen]);
					}
				}else{
					allPhenotypes.push.apply(allPhenotypes, phenotypes);
				}
			}
			
			var cs = hbClinicalSynopsisComp({all: allPhenotypes, inheritance: inheritancePhenotypes});
			diseasePanel.append(cs);
			
		}else{
			// no clinicalSynopsis: this might belong to phenotypic series, for example http://omim.org/phenotypicSeries/249000
			// TODO what to do with phenotypic series? 
			var warning = '<div class="alert alert-warning" id="no-clinical-synopsis-warning">'
				+ '<strong>No clinical synopsis found!</strong> This reference might belong to a phenotypic series. ' 
				+ 'Please visit <a href="http://www.omim.org/entry/' + entry.mimNumber + '" target="_blank">the OMIM website.</a></div>';
			diseasePanel.append(warning);
		}
		
		// add space
		diseasePanel.append('<br />');
		
		// TEXT
		diseasePanel.append('<h3>Text</h3>');
		textParagraph = $('<p></p>').appendTo(diseasePanel);
		if ('textSectionList' in entry){
			$.each(entry.textSectionList, function(index, textObject){
				var text = textObject.textSection.textSectionContent;
				var title = textObject.textSection.textSectionTitle;

				var textParts = text.split(lineBreaks);
				$.each(textParts, function(index, part) {
					if (part.substring(0, 9) === '<Subhead>') {
						// OMIM <Subhead> tag to <strong> title </strong>
						var part = part.substring(9);
						textParts[index] = '<strong>' + part + '</strong>';
					} else {
						// split text on { and } to find references to other OMIM ids

						var subParts = part.split(/[{}]/);
						var linkedText = "";
						
						if (subParts.length > 1) {
							$.each(subParts, function(index, subPart) {
								if (!isNaN(subPart)) {
									// OMIM id, add a link
									link = subPart.replace('.', '#');
									linkedText += '<a href="http://www.omim.org/entry/' + window.htmlEscape(link) + '" target="_blank">' + subPart + '</a>';
									
								} else {
									linkedText += window.htmlEscape(subPart);
								};
							})
							textParts[index] = linkedText;
						}
					}
				});
				// stitch parts back together to form one text section
				finalText = textParts.join('<br />');

				// every text section but the first gets a title
				if (index > 0) textParagraph.append('<h4>' + title + '</h4>');
				textParagraph.append('<p>' + finalText + '</p>');
			});
		}
	}
	
	
	/**
	 * Disease tab selection action. Retrieves an OMIM object from the OMIM API or local storage and 
	 * calls showOmimObject() to show it in the tab.
	 * 
	 * @param diseaseId
	 * @returns
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function onSelectDiseaseTab(diseaseId) {
		if (localStorageSupported && diseaseId in localStorage){
			showOmimObject(JSON.parse(localStorage.getItem(diseaseId)));
		}else{
			// don't have this one yet, get it from OMIM API
			$.ajax({
				type : 'GET',
				contentType : 'application/json',
				url : '/omim/' + diseaseId.substring(5), //remove OMIM: part from string
				success : function(omimObject) {
					
					if (localStorageSupported && omimObject !== null){
						localStorage.setItem(diseaseId, JSON.stringify(omimObject));
					}
					
					if (omimObject != undefined){
						showOmimObject(omimObject);
					}else{
						//TODO
					}
					
				}
			});
		}
	}

	
	/**
	 * Called when a selection list item is selected. Refreshes the variant table and the disease tabs.
	 * 
	 * @param link the item that was selected
	 * @param selectionMode the current selection mode
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function onSelectListItem(link, selectionMode) {
		// visually select the right item in the list
		$('#diseasematcher-selection-list li').attr('class', '');
		link.parent().attr('class', 'active');

		entityName = getEntity().name;

		if (selectionMode == SelectionMode.DISEASE) {
			diseaseId = link.attr('id');
			restApi.getAsync('/api/v1/DiseaseMapping', {
				'q' : {
					'q' : [ {
						field : 'diseaseId',
						operator : 'EQUALS',
						value : diseaseId
					} ],
					'num' : 10000
				},
				'attributes' : [ 'geneSymbol' ]
			}, function(diseaseGenes) {
				// get unique genes for this disease
				var uniqueGenes = [];
				$.each(diseaseGenes.items, function(index, disease) {
					if ($.inArray(diseaseGenes.geneSymbol, uniqueGenes) === -1) {
						uniqueGenes.push(disease.geneSymbol);
					}
				});

				showVariants(uniqueGenes, $('#diseasematcher-variant-panel'))
				showDiseases(diseaseId, selectionMode);
			});
		} else if (selectionMode == SelectionMode.GENE) {
			// make it an array for showVariants()
			var geneId = [ link.attr('id') ];
			showVariants(geneId, $('#diseasematcher-variant-panel'));
			showDiseases(geneId, selectionMode);
		}
	}
	
	
	/**
	 * Sets readable names for disease tabs (replace the default OMIM identifier).
	 * 
	 * @param diseaseIds the OMIM identifiers to get names for
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function setDiseaseTabNames(diseaseIds){
		var requestParams = '?';
		$.each(diseaseIds, function(index, diseaseId){
			if (index > 0){
				requestParams += '&';
			}
			requestParams += 'diseaseId=' + diseaseId;
		});
		
		$.ajax({
			type : 'POST',
			contentType : 'application/json',
			url : '/diseasematcher/diseaseNames' + requestParams,
			success : function(data) {
				$.each(diseaseIds, function(index, diseaseId){
					$("[href='#" + diseaseId + "']").html(data[diseaseId][0]);
				});
			}
		});
	}
	
	
	/**
	 * Refreshes and sets the disease tabs based on the currently selected gene or disease(s).
	 * 
	 * @param id OMIM id or gene symbol
	 * @param selectionMode selection mode for the id
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function showDiseases(id, selectionMode) {
		// empty disease tabs
		$('#diseasematcher-disease-tab-content').empty();
		$('#diseasematcher-disease-panel-tabs').empty();

		if (selectionMode === SelectionMode.DISEASE) {
			// add 1 tab for current disease
			$('#diseasematcher-disease-panel-tabs').append('<li class="active"><a href="#' + id	+ '" data-toggle="tab">' + id + '</a></li>');
			$('#diseasematcher-disease-tab-content').append('<div class="tab-pane active" id="' + id + '">' + '</div>');

				setDiseaseTabNames([diseaseId]);
				onSelectDiseaseTab(diseaseId);		

		} else if (selectionMode === SelectionMode.GENE) {
			// add tabs for each disease this gene is known for
			restApi.getAsync('/api/v1/DiseaseMapping', {
				'q' : {
					'q' : [ {
						field : 'geneSymbol',
						operator : 'EQUALS',
						value : id
					} ],
					'num' : 10000
				},
				'attributes' : [ 'diseaseId' ]
			}, function(diseases) {
				// get unique diseases
				var uniqueDiseases = [];
				$.each(diseases.items, function(index, disease) {
					if ($.inArray(disease.diseaseId, uniqueDiseases) === -1) {
						uniqueDiseases.push(disease.diseaseId);
						$('#diseasematcher-disease-panel-tabs').append(
								'<li><a href="#' + disease.diseaseId + '" id="'
										+ disease.diseaseId
										+ '" data-toggle="tab">'
										+ disease.diseaseId + '</a></li>');
					}
				});

				$('#diseasematcher-disease-panel-tabs li a').click(
						function(e) {
							e.preventDefault();
							$('#diseasematcher-disease-panel-tabs li')
									.removeClass('active');
							$(this).parent().addClass('active');
							onSelectDiseaseTab($(this).attr('id'));
						});

				var first = $('#diseasematcher-disease-panel-tabs li').first();
				first.addClass('active');

				if (uniqueDiseases.length === 0){
					var diseasePanel = $('#diseasematcher-disease-tab-content');
					var warning = '<div class="alert alert-warning" id="no-OMIM-for-this-gene">'
						+ '<strong>No OMIM disorders found for this gene!</strong></div>';
					diseasePanel.append(warning);
					return;
				}else{
					setDiseaseTabNames(uniqueDiseases);
					onSelectDiseaseTab(first.children().attr('id'));
				}

			});
		}
	}

	
	/**
	 * Initializes or refreshes the variants table based on a set of genes. 
	 * The variant table shows variants in the user's dataset in a certain gene or genes.
	 * 
	 * @param genes the genes to get variants for
	 * @param panel the panel to add/update
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function showVariants(genes, panel, itemFilterQuery) {
		var geneRules = [];
		$.each(genes, function(index, gene) {
			if (geneRules.length > 0) {
				geneRules.push({
					'operator' : 'OR'
				});
			}
			geneRules.push({
				'field' : geneSymbolColumn,
				'operator' : 'EQUALS',
				'value' : gene
			});
		});
		
		if (typeof itemFilterQuery != 'undefined'){	
			if (itemFilterQuery.q.length === 0){
				renderVariantTable(panel, {q: geneRules});
			}else{
				q = {q: [{
	    	        	operator: "NESTED",
	    	        	nestedRules: itemFilterQuery.q
	    	    	}, {
	    	    		operator: "AND"
	    	    	}, {
	    	    		operator: "NESTED",
	    	    		nestedRules: geneRules
	    	    	}]
				};
				renderVariantTable(panel, q);
			}				
		}else{
			currentQuery = {'q' : geneRules};
			renderVariantTable(panel, currentQuery);	
		}
	}
	
	function renderVariantTable(panel, query){
		// show associated variants in info panel	
		if (tableEditable) {
			tableEditable = molgenis.hasWritePermission(molgenis.dataexplorer.getSelectedEntityMeta().name);
		}
	
		if (panel.has('table').length){
			panel.table('setQuery', query);
		}else{
			var table = panel.table({
				'entityMetaData' : getEntity(),
				'attributes' : getAttributes(),
				'query' : query,
				'editable': tableEditable,
				'onDataChange' : function(){
					$(document).trigger('dataChange.diseasematcher');
				}
			});
		}
	}

	/**
	 * Escapes regular expression characters within a string.
	 * 
	 * @param string the string in which to escape regular expression characters
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function escapeRegExp(string) {
	    return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
	}
	
	
	/**
	 * Replaces all instances of a substring within a string with something else.
	 * 
	 * @param string the string in which to replace
	 * @param find the substring(s) to be replaced
	 * @param replace the value to replace the targets with
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function replaceAll(string, find, replace) {
		  return string.replace(new RegExp(escapeRegExp(find), 'g'), replace);
	}
	
	
	/**
	 * Returns the selected entity from the data explorer.
	 * 
	 * @returns the selected entity
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function getEntity() {
		return molgenis.dataexplorer.getSelectedEntityMeta();
	}
	
	
	/**
	 * Returns the currently active data explorer query.
	 * 
	 * @returns a data explorer query
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function getQuery() {
		return molgenis.dataexplorer.getEntityQuery();
	}

	
	/**
	 * Returns the currently selected attributes.
	 * 
	 * @returns attributes
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function getAttributes() {
		return molgenis.dataexplorer.getSelectedAttributes();
	}
	
	
	/**
	 * Checks if a browser supports local storage.
	 * 
	 * @returns boolean telling if a browser supports local storage
	 * @memberOf molgenis.dataexplorer.diseasematcher
	 */
	function browserSupportsLocalStorage() {
		try {
			return 'localStorage' in window && window['localStorage'] !== null;
		} catch (e) {
			return false;
		}
	}
	
})($, window.top.molgenis = window.top.molgenis || {});