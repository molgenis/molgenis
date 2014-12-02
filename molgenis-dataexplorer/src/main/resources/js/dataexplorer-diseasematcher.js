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

	var infoPanel = $('#diseasematcher-infopanel');
	
	/**
	 * Regular expression containing all line break flavors, used for replacing line breaks.
	 */
	var lineBreaks = /(?:\r\n|\r|\n)/g;

	/**
	 * Types of selection modes, Enum style.
	 */
	var SelectionMode = {
		DISEASE : 'diseases',
		GENE : 'genes'
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
	
	
	/**
	 * Listens for data explorer query changes and updates the selected gene/disease lists.
	 */
	$(document).on('changeQuery', function(e, query) {	
		updateSelectionList(currentSelectionMode);
	});

	
	/**
	 * Listens for attribute selection changes and updates the variant table.
	 */
	$(document).on('changeAttributeSelection', function(e, data) {
		var panel = $('#diseasematcher-variant-panel');
		if (!panel.is(':empty')) {
			panel.table('setAttributes', data.attributes);
		}
	});

	
	/**
	 * Checks if the current state of Molgenis meets the requirements of this tool. 
	 * Checks if the needed datasets are loaded (DiseaseMapping and Disease) and if the user's dataset has 
	 * a gene symbol column. All calls are async to avoid hanging of the application.
	 *
	 * @param entityUri the URI of the entity to check for a gene symbol column
	 */
	function checkToolAvailable(entityUri) {
		checkDatasetAvailable('Disease');
		checkDatasetAvailable('DiseaseMapping');

		// if an entity is selected, check if it has a gene symbol column and show a warning if it does not
		restApi.getAsync(entityUri + '/meta', {}, function(data) {
			if (data === null || !data.attributes.hasOwnProperty(geneSymbolColumn)) {
				
				var warningSrc = $("#hb-column-warning").html();
				var template = Handlebars.compile(warningSrc);
				var warning = template({column: geneSymbolColumn});
				infoPanel.append(warning);
				toolAvailable = false;
		
				// TODO determine which annotators to propose
			}
		});
	}

	
	/**
	 * Checks if a dataset is loaded. Shows a warning when it is not. 
	 * 
	 * @param dataset the dataset to check for
	 */
	function checkDatasetAvailable(dataset) {
		restApi.getAsync('/api/v1/' + dataset, {'num' : 1},	function(data){
			if (data.total === 0) {
				var warningSrc = $('#hb-dataset-warning').html();
				var template = Handlebars.compile(warningSrc);
				var warning = template({dataset: dataset});
				infoPanel.append(warning);
				toolAvailable = false;
			}
		});
	}

	/**
	 * Switches the layout for the info panel by using different Handlebars templates.
	 */
	function setInfoPanelLayout(){
		var layoutSrc = $('#hb-layout-variant').html();
		var template = Handlebars.compile(layoutSrc);
		infoPanel.html(template({}));
	}
	
	/**
	 * Click action for the navigation buttons in the Disease Matcher selection panel.
	 * 
	 * @param element the selected link
	 * @selectionMode the appropriate selection mode to use
	 */
	function onClickNavBtn(element, selectionMode){
		if (!toolAvailable) return;
		
		setInfoPanelLayout();
		
		//reset navbar and activate the clicked button
		$('#diseasematcher-selection-navbar li').attr('class', '');
		$(element).parent().attr("class", "active");
		
		// update title of selection list
		$('#diseasematcher-selection-title').html(selectionMode.charAt(0).toUpperCase() + selectionMode.slice(1));

		currentSelectionMode = selectionMode;
		updateSelectionList(selectionMode);
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
		})

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
	 */
	function populateSelectionList(list, selectionMode) {
		$('#diseasematcher-selection-list').empty();
		
		if (list === null || list.length === 0) return;

		if (selectionMode === SelectionMode.DISEASE) {
			$.each(list, function(index, item) {
				var name;
				if (item.diseaseName === null) {
					name = item.diseaseId;
				} else {
					name = item.diseaseName;
				}
				$('#diseasematcher-selection-list').append(
						'<li><a href="#" class="diseasematcher-disease-listitem" id="'
								+ item.diseaseId + '">' + name + '</a></li>');
			});
		} else if (selectionMode === SelectionMode.GENE) {
			$.each(list, function(index, item) {
				$('#diseasematcher-selection-list').append(
						'<li><a href="#" class="diseasematcher-gene-listitem" id="'
								+ item + '">' + item + '</a></li>');
			});
		}

		$('#diseasematcher-selection-list a').click(function(e) {
			e.preventDefault(); // stop jumping to top of page
			onSelectListItem($(this), currentSelectionMode);
		});

		// pre-select top-most disease
		onSelectListItem($('#diseasematcher-selection-list li a').first(), currentSelectionMode);
	}

	
	/**
	 * Initializes a pager for the currently selected diseases. 
	 * Needs a reference to selectionMode to pass to onPageChange().
	 * 
	 * @param total the total amount of items 
	 * @param selectionMode the selection mode for this list
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
			
			for (var propt in clinicalSynopsis){
				// remove links, id's between { and }
				var phenotype = clinicalSynopsis[propt].replace(/ *\{[^}]*\} */g, '');
				
				// give each phenotype in one string it's own line
				phenotype = replaceAll(phenotype, ';', '');
				phenotype = phenotype.replace(lineBreaks, '<br />');
				
				if (propt == 'Inheritance' || propt == 'inheritance'){
					synopsisParagraph.prepend('<span class="diseasematcher label label-success">' + phenotype + '</span><br />');		
				}else{							
					synopsisParagraph.append(phenotype + '<br />');
				}
			}
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
									subPart.replace('.', '#');
									linkedText += '<a href="http://www.omim.org/entry/' + subPart + '" target="_blank">' + subPart + '</a>';
									
								} else {
									linkedText += subPart;
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
					
					showOmimObject(omimObject);
				}
			});
		}
	}

	
	/**
	 * Called when a selection list item is selected. Refreshes the variant table and the disease tabs.
	 * 
	 * @param link the item that was selected
	 * @param selectionMode the current selection mode
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
				'attributes' : [ 'geneSymbo' ]
			}, function(diseaseGenes) {
				// get unique genes for this disease
				var uniqueGenes = [];
				$.each(diseaseGenes.items, function(index, disease) {
					if ($.inArray(diseaseGenes.geneSymbol, uniqueGenes) === -1) {
						uniqueGenes.push(disease.geneSymbol);
					}
				});

				showVariants(uniqueGenes)
				showDiseases(diseaseId, selectionMode);
			});
		} else if (selectionMode == SelectionMode.GENE) {
			// make it an array for showVariants()
			var geneId = [ link.attr('id') ];
			showVariants(geneId);
			showDiseases(geneId, selectionMode);
		}
	}
	
	
	/**
	 * Sets readable names for disease tabs (replace the default OMIM identifier).
	 * 
	 * @param diseaseIds the OMIM identifiers to get names for
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

				setDiseaseTabNames(uniqueDiseases);
				onSelectDiseaseTab(first.children().attr('id'));

			});
		}
	}

	
	/**
	 * Initializes or refreshes the variants table based on a set of genes. 
	 * The variant table shows variants in the user's dataset in a certain gene or genes.
	 * 
	 * @param genes the genes to get variants
	 */
	function showVariants(genes) {
		var queryRules = [];
		$.each(genes, function(index, gene) {
			if (queryRules.length > 0) {
				queryRules.push({
					'operator' : 'OR'
				});
			}
			queryRules.push({
				'field' : geneSymbolColumn,
				'operator' : 'EQUALS',
				'value' : gene
			});
		});
		
		var query = {'q' : queryRules};

		// show associated variants in info panel
		
		if (tableEditable) {
			tableEditable = molgenis.hasWritePermission(molgenis.dataexplorer.getSelectedEntityMeta().name);
		}
	
		if ($('#diseasematcher-variant-panel').has('table').length){
			$('#diseasematcher-variant-panel').table('setQuery', query);
		}else{
			$('#diseasematcher-variant-panel').table({
				'entityMetaData' : getEntity(),
				'attributes' : getAttributes(),
				'query' : query,
				'editable': tableEditable
			});
		}
	}

	
	/**
	 * Escapes regular expression characters within a string.
	 * 
	 * @param string the string in which to escape regular expression characters
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
	 */
	function replaceAll(string, find, replace) {
		  return string.replace(new RegExp(escapeRegExp(find), 'g'), replace);
	}
	
	
	/**
	 * Returns the selected entity from the data explorer.
	 * 
	 * @returns the selected entity
	 */
	function getEntity() {
		return molgenis.dataexplorer.getSelectedEntityMeta();
	}
	
	
	/**
	 * Returns the currently active data explorer query.
	 * 
	 * @returns a data explorer query
	 */
	function getQuery() {
		return molgenis.dataexplorer.getEntityQuery();
	}

	
	/**
	 * Returns the currently selected attributes.
	 * 
	 * @returns attributes
	 */
	function getAttributes() {
		return molgenis.dataexplorer.getSelectedAttributes();
	}
	
	
	/**
	 * Checks if a browser supports local storage.
	 * 
	 * @returns boolean telling if a browser supports local storage
	 */
	function browserSupportsLocalStorage() {
		try {
			return 'localStorage' in window && window['localStorage'] !== null;
		} catch (e) {
			return false;
		}
	}

})($, window.top.molgenis = window.top.molgenis || {});