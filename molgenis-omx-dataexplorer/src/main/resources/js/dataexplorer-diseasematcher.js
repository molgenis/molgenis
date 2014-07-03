(function($, molgenis) {
	var restApi = new molgenis.RestClient();

	var lineBreaks = /(?:\r\n|\r|\n)/g;

	var SelectionMode = {
		DISEASE : 'diseases',
		GENE : 'genes'
	};
	var currentSelectionMode = SelectionMode.GENE;
	var listMaxLength = 10;
	var toolAvailable = true;
	var localStorageSupported = false;
	
	
	/**
	 * Listen for data explorer query changes.
	 */
	$(document).on('changeQuery', function(e, query) {
		
		updateSelectionList(currentSelectionMode);
	});

	
	/**
	 * Listen for attribute selection changes.
	 */
	$(document).on('changeAttributeSelection', function(e, data) {
		var panel = $('#diseasematcher-variant-panel');
		if (!panel.is(':empty')) {
			panel.table('setAttributes', data.attributes);
		}
	});

	
	/**
	 * Listen for entity change events. Also gets called when page is loaded.
	 */
	$(document).on('changeEntity', function(e, entityUri) {

		
	});
	
	
	/**
	 * Checks if the current state of Molgenis meets the requirements of this
	 * tool. Shows warnings and instructions when it does not.
	 */
	function checkToolAvailable(entityUri) {
		checkDatasetAvailable('Disease');
		checkDatasetAvailable('DiseaseMapping');

		// if an entity is selected, check if it has a 'geneSymbol' column and show a warning if it does not
		// TODO determine which columns to check for, and which annotators to propose
		restApi.getAsync(entityUri + '/meta', {}, function(data) {
			if (data === null || !data.attributes.hasOwnProperty('geneSymbol')) {
				var warning = '<div class="alert alert-warning" id="gene-symbol-warning">'
						+ '<strong>No gene symbols found!</strong> For this tool to work, make sure '
						+ 'your dataset has a \'geneSymbol\' column.</div>';
				if (!$('#diseasematcher-variant-panel #gene-symbol-warning').length) {
					$('#diseasematcher-variant-panel').append(warning);
					toolAvailable = false;
				}
			} else {
				$(('#diseasematcher-variant-panel #gene-symbol-warning')).remove();
			}
		});
	}

	
	/**
	 * Checks if a dataset is loaded. Shows a warning when it is not. Returns true or false depending on check.
	 * 
	 * @param dataset
	 *            the dataset to check for
	 */
	function checkDatasetAvailable(dataset) {
		// check if a DiseaseMapping dataset is loaded, show a warning when it
		// is not
		restApi.getAsync('/api/v1/' + dataset, {'num' : 1},	function(data){
			if (data.total === 0) {
				var warning = '<div class="alert alert-warning" id="' + dataset + '-warning"><strong>' + dataset + ' not loaded!'
						+ '</strong> For this tool to work, a valid ' + dataset	+ ' dataset should be uploaded.</div>';
				if (!$('#diseasematcher-variant-panel #' + dataset + '-warning').length) {
					$('#diseasematcher-variant-panel').append(warning);
					toolAvailable = false;
				}
			} else {
				$('#diseasematcher-variant-panel #'+ dataset + '-warning').remove();
			}
		});
	}

	/**
	 * On document ready.
	 */
	$(function() {

		// selection nav bar diseases:
		$('#diseasematcher-diseases-select-button').click(function(e) {
			e.preventDefault();
			if (!toolAvailable) return;
			
			$('#diseasematcher-selection-navbar li').attr('class', '');
			$(this).parent().attr("class", "active");

			$('#diseasematcher-selection-search').attr('placeholder',
					'Search diseases');
			$('#diseasematcher-selection-title').html('Diseases');

			currentSelectionMode = SelectionMode.DISEASE;
			updateSelectionList(SelectionMode.DISEASE);
		});
		
		// selection nav bar genes:
		var genesBtn = $('#diseasematcher-genes-select-button').click(function(e) {
			e.preventDefault();
			if (!toolAvailable) return;
			
			$('#diseasematcher-selection-navbar li').attr('class', '');
			$(this).parent().attr("class", "active");
	
			$('#diseasematcher-selection-search').attr('placeholder', 'Search genes');
			$('#diseasematcher-selection-title').html('Genes');
	
			currentSelectionMode = SelectionMode.GENE;
			updateSelectionList(SelectionMode.GENE);
		})

		// init tabs
		$('#diseasematcher-disease-panel-tabs').tab();

		localStorageSupported = browserSupportsLocalStorage();
		if (localStorageSupported) localStorage.clear();
		
		toolAvailable = true;
		checkToolAvailable('/api/v1/' + getEntity().name);

		// TODO: make this process nicer (multiple async calls are confusing)
		// wait till the checkToolAvailable calls are done. if tool is available pre-select the first button
		$(document).ajaxStop(function() {
		    $(this).unbind("ajaxStop"); //prevent running again when other calls finish
		    if (toolAvailable) genesBtn.click();
		});
		
	});

	
	/**
	 * 
	 */
	function updateSelectionList(selectionType) {
		var entityName = getEntity().name;
		var request = {
			'datasetName' : entityName,
			'num' : listMaxLength,
			'start' : 0
		};
		$.extend(request, getQuery());
		console.log(request);
		$.ajax({
			type : 'POST',
			contentType : 'application/json',
			url : '/diseasematcher/' + selectionType,
			data : JSON.stringify(request),
			success : function(data) {
			
				if (selectionType === SelectionMode.DISEASE) {
					// add the diseases (if any) to the disease list and init
					// the pager
					populateSelectionList(data.diseases, selectionType);
					initPager(data.total, selectionType);
				} else if (selectionType === SelectionMode.GENE) {
					populateSelectionList(data.genes, selectionType);
					initPager(data.total, selectionType);
				}
			}
		});
	}

	
	/**
	 * 
	 */
	function populateSelectionList(list, selectionType) {
		$('#diseasematcher-selection-list').empty();
		
		if (list === null || list.length === 0) return;

		if (selectionType === SelectionMode.DISEASE) {
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
		} else if (selectionType === SelectionMode.GENE) {
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
	 */
	function initPager(total, selectionType) {
		var container = $('#diseasematcher-selection-pager');
		if (total > listMaxLength) {
			container.pager({
				'nrItems' : total,
				'nrItemsPerPage' : listMaxLength,
				'onPageChange' : function(page) {
					onPageChange(page.start, page.end, selectionType);
				}
			});
			container.show();
		} else
			container.hide();
	}

	
	/**
	 * 
	 */
	function onPageChange(pageStart, pageEnd, selectionType) {
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
			url : '/diseasematcher/' + selectionType,
			data : JSON.stringify(request),
			success : function(data) {
				
				if (selectionType === SelectionMode.DISEASE) {
					populateSelectionList(data.diseases, selectionType);
				} else if (selectionType === SelectionMode.GENE) {
					populateSelectionList(data.genes, selectionType);
				}
			}
		});
	}

	
	/**
	 * 
	 * @param omimObject
	 * @returns
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
		
		console.log(omimObject);
		
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
	 * 
	 * @param
	 */
	function onSelectListItem(link, selectionType) {
		// visually select the right item in the list
		$('#diseasematcher-selection-list li').attr('class', '');
		link.parent().attr('class', 'active');

		entityName = getEntity().name;

		if (selectionType == SelectionMode.DISEASE) {
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
					if ($.inArray(diseaseGenes.geneSymbol,
							uniqueGenes) === -1) {
						uniqueGenes.push(disease.geneSymbol);
					}
				});

				showVariants(uniqueGenes)
				showDiseases(diseaseId, selectionType);
			});
		} else if (selectionType == SelectionMode.GENE) {
			// make it an array for showVariants()
			var geneId = [ link.attr('id') ];
			showVariants(geneId);
			showDiseases(geneId, selectionType);
		}
	}
	
	
	/**
	 * 
	 */
	function setDiseaseTabNames(diseaseIds){
		if (!(diseaseIds instanceof Array)){
			diseaseIds = [diseaseIds];
		}
		
		var requestParams = '?';
		$.each(diseaseIds, function(index, diseaseId){
			requestParams += 'diseaseId=' + diseaseId;
			if (index !== diseaseIds.length - 1){
				requestParams += '&';
			}
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
	 * 
	 */
	function showDiseases(id, selectionType) {
		// empty disease tabs
		$('#diseasematcher-disease-tab-content').empty();
		$('#diseasematcher-disease-panel-tabs').empty();

		if (selectionType === SelectionMode.DISEASE) {
			// add 1 tab for current disease
			$('#diseasematcher-disease-panel-tabs').append('<li class="active"><a href="#' + id	+ '" data-toggle="tab">' + id + '</a></li>');
			$('#diseasematcher-disease-tab-content').append('<div class="tab-pane active" id="' + id + '">' + '</div>');

			
			setDiseaseTabNames(diseaseId);
			onSelectDiseaseTab(diseaseId);
			

		} else if (selectionType === SelectionMode.GENE) {
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
	 * 
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
				'field' : 'geneSymbol',
				'operator' : 'EQUALS',
				'value' : gene
			});
		});
		
		var query = {'q' : queryRules};

		// show associated variants in info panel
		if ($('#diseasematcher-variant-panel').has('table').length){
			$('#diseasematcher-variant-panel').table('setQuery', query);
		}else{
			$('#diseasematcher-variant-panel').table({
				'entityMetaData' : getEntity(),
				'attributes' : getAttributes(),
				'query' : query
			});
		}
		
		
	}

	
	/**
	 * 
	 */
	function escapeRegExp(string) {
	    return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
	}
	
	/**
	 * 
	 */
	function replaceAll(string, find, replace) {
		  return string.replace(new RegExp(escapeRegExp(find), 'g'), replace);
	}
	
	/**
	 * 
	 */
	function lineBreaksToBrTags(str) {
		return str.replace(/(?:\r\n|\r|\n)/g, '<br />');
	}

	/**
	 * Returns the selected entity from the data explorer
	 */
	function getEntity() {
		return molgenis.dataexplorer.getSelectedEntityMeta();
	}
	
	function getQuery() {
		return molgenis.dataexplorer.getEntityQuery();
	}

	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function getAttributes() {
		return molgenis.dataexplorer.getSelectedAttributes();
	}
	
	/**
	 * 
	 */
	function browserSupportsLocalStorage() {
		try {
			return 'localStorage' in window && window['localStorage'] !== null;
		} catch (e) {
			return false;
		}
	}

})($, window.top.molgenis = window.top.molgenis || {});