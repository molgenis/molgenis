(function($, molgenis) {
	var restApi = new molgenis.RestClient();
	
	var currentQuery = null;
	var currentDiseases = null;
	
	var diseaseListMaxLength = 15;
	
	/**
	 * Listen for data explorer query changes.
	 */
	$(document).on('changeQuery', function(e, query){
		currentQuery = query;		
		updateDiseaseList();
	});
	
	/**
	 * Listen for entity change events. Also gets called when page is loaded.
	 */
	$(document).on('changeEntity', function(e, entityUri) {
		checkToolAvailable(entityUri);
	});
	
	
	/**
	 * Checks if the current state of Molgenis meets the requirements of this tool. Shows warnings and instructions when it does not.
	 */
	function checkToolAvailable(entityUri){
		var warnings = "";
		
		//check if a DiseaseMapping dataset is loaded, show a warning when it is not
		var check = restApi.get('/api/v1/DiseaseMapping', {'num': 1});
		if (check.total == 0){
			warnings += '<div class="alert alert-warning" id="diseasemapping-warning"><strong>DiseaseMapping not loaded!' +
					'</strong> For this tool to work, a valid DiseaseMapping dataset should be uploaded.</div>';
		}
		
		// if an entity is selected, check if it has a 'geneSymbol' column and show a warning if it does not 
		// TODO determine which columns to check for, and which annotators to propose
		var check = restApi.get(entityUri + '/meta');		
		if (check == null || !check.attributes.hasOwnProperty('geneSymbol')){			
				warnings += '<div class="alert alert-warning" id="gene-symbol-warning">' +
						'<strong>No gene symbols found!</strong> For this tool to work, make sure ' + 
						'your dataset has a \'geneSymbol\' column.</div>';						
				$('#grab-disease-button').attr('class', 'disabled');
		}
		
		//TODO disable buttons etc. when there are warnings
		$('#vardump').html(warnings);
	}

	/**
	 * Called when page has loaded.
	 */
	$(function() {
		//TODO get diseases when page loads (so 'grab diseases' button can be removed)	
		// register 'grab disease' button
		$('#grab-diseases-button').click(function(){
			updateDiseaseList();
		});
	});
	
	/**
	 * Gets the diseases based on the current query, populates the disease list and sets the pager.
	 */
	function updateDiseaseList(){
		currentDiseases = getDiseases();
		populateDiseaseList(currentDiseases.slice(0, diseaseListMaxLength));
		initPager();
	}
	
	/**
	 * Initializes a pager for the currently selected diseases.
	 */
	function initPager(){
		var container = $('#disease-pager');
		if(currentDiseases != null && currentDiseases.length > diseaseListMaxLength) {
			container.pager({
				'nrItems' : currentDiseases.length,
				'nrItemsPerPage' : diseaseListMaxLength,
				'onPageChange' : function(page) {
					populateDiseaseList(currentDiseases.slice(page.start, page.end));
				}
			});
			container.show();
		} else container.hide();
	}
	
	/**
	 * Populates disease list with new set of diseases.
	 */
	function populateDiseaseList(diseases){
		
		$('#disease-list').empty();
		
		$.each(diseases, function(index, disease){
			$('#disease-list').append('<li><a href="#" id="' + disease + '">' + disease + '</a></li>');
		});
		
		$('#disease-list a').click(function(e){
			e.preventDefault(); // stop jumping to top of page
			onSelectDisease($(this));
		});
		
		// pre-select top-most disease
		onSelectDisease($('#disease-list li a').first());
	}
	
	/**
	 * Called when a disease is selected. Updates the selection in the disease list and shows the associated
	 * phenotypes (from DiseaseMapping) and variants (from current dataset) for this disease.
	 * @param diseaseLink the disease link that was clicked
	 */
	function onSelectDisease(diseaseLink){
		// visually select the right disease in the list
		$('#disease-list li').attr('class', '');
		diseaseLink.parent().attr('class', 'active');
		
		//get TYPICAL phenotypes for this disease
		var diseaseId = diseaseLink.attr('id');		
		var phenotypes =  restApi.get('/api/v1/DiseaseMapping', {
			'q' : {
				'q' : [{
					field : 'diseaseId',
					operator : 'EQUALS',
					value : diseaseId
				}, {operator: 'AND'}, {
					field : 'isTypical',
					operator: 'EQUALS',
					value : true
				}],
				'num': 10000,
			},
			'attributes': ['HPODescription']
		});

		// show the phenotypes for this disease in the info panel
		var container = $('#diseasematcher-analysis-right');
		container.empty();
		$.each(phenotypes.items, function(index, pheno){
			container.append(pheno.HPODescription + '<br/>');
		});
		
		// get genes for this disease
		var diseaseInfo =  restApi.get('/api/v1/DiseaseMapping', {
			'q' : {
				'q' : [{
					field : 'diseaseId',
					operator : 'EQUALS',
					value : diseaseId
				}],	
				'num': 10000
			},
			'attributes': ['geneSymbol']
		});
		
		// get unique genes for this disease
		var uniqueGenes = [];
		$.each(diseaseInfo.items, function(index, disease){
			if($.inArray(disease.geneSymbol, uniqueGenes) === -1){
				uniqueGenes.push(disease.geneSymbol);
			}
		});	

		//build query to retrieve variants associated with this disease from current dataset
		var queryRules = [];
		$.each(uniqueGenes, function(index, uniqueGene){
			if(queryRules.length > 0){
				queryRules.push({
					'operator' : 'OR'
				});
			}
			queryRules.push({
				'field' : 'geneSymbol',
				'operator' : 'EQUALS',
				'value' : uniqueGene
			});
		});
		
		// show associated variants in info panel
		$('#vardump').table({
			'entityMetaData' : getEntity(),
			'attributes' : getAttributes(),
			'query' : {
				'q' : queryRules
			}
		});
	}
	
	/**
	 * Uses the genes in the currently selected entity to look for diseases. 
	 * @returns array of diseaseIds, empty array when none found 
	 */
	function getDiseases(){
		var entityName = getEntity().name;
		var entityData = restApi.get('/api/v1/' + entityName, {'attributes': ['geneSymbol'], 'q': currentQuery});	
		
		var uniqueGeneSymbols = [];
		if (entityData.total > 0){
			$.each(entityData.items, function(index, entity){
				if($.inArray(entity.geneSymbol, uniqueGeneSymbols) === -1){
					uniqueGeneSymbols.push(entity.geneSymbol);
				}
			});
		}else{
			return [];
		}
		
		//TODO change num: 10000 to a sensible solution (get unique)
		// fetch diseases based on list of genes
		var diseaseInfo =  restApi.get('/api/v1/DiseaseMapping', {
			'q' : {
				'q' : [{
					field : 'geneSymbol',
					operator : 'IN',
					value : uniqueGeneSymbols
				}],	
				'num': 10000
			},
			'attributes': ['diseaseId']
		});
		
		var uniqueDiseases = [];
		if (diseaseInfo.total > null){
			$.each(diseaseInfo.items, function(index, disease){
				if($.inArray(disease.diseaseId, uniqueDiseases) === -1){
					uniqueDiseases.push(disease.diseaseId);
				}
			});	
			
			return uniqueDiseases;
		}else{
			return [];
		}
	}
	
	/**
	 * Returns the selected entity from the data explorer 
	 */
	function getEntity() {
		return molgenis.dataexplorer.getSelectedEntityMeta();
	}
	
	/**
	 * @memberOf molgenis.dataexplorer.data
	 */
	function getAttributes() {
		return molgenis.dataexplorer.getSelectedAttributes();
	}
	
})($, window.top.molgenis = window.top.molgenis || {});