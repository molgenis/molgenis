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
		//check if a DiseaseMapping dataset is loaded, show a warning when it is not
		restApi.getAsync('/api/v1/DiseaseMapping', {'num': 1}, function(data){
			if (data.total === 0){
				var warning = '<div class="alert alert-warning" id="diseasemapping-warning"><strong>DiseaseMapping not loaded!' +
						'</strong> For this tool to work, a valid DiseaseMapping dataset should be uploaded.</div>';
				if(!$('#vardump #diseasemapping-warning').length){
					$('#vardump').append(warning);
				}
			}else{
				$(('#vardump #diseasemapping-warning')).remove();
			}
		});

		// if an entity is selected, check if it has a 'geneSymbol' column and show a warning if it does not 
		// TODO determine which columns to check for, and which annotators to propose
		restApi.getAsync(entityUri + '/meta', {}, function(data){
			if (data === null || !data.attributes.hasOwnProperty('geneSymbol')){			
				var warning = '<div class="alert alert-warning" id="gene-symbol-warning">' +
						'<strong>No gene symbols found!</strong> For this tool to work, make sure ' + 
						'your dataset has a \'geneSymbol\' column.</div>';		
				if(!$('#vardump #gene-symbol-warning').length){
					$('#vardump').append(warning);
				}
			}else{
				$(('#vardump #gene-symbol-warning')).remove();
			}
		});	
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
		restApi.getAsync('/api/v1/DiseaseMapping', {
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
		}, function(phenotypes){
			
			// show the phenotypes for this disease in the info panel
			var container = $('#diseasematcher-analysis-right');
			container.empty();
			$.each(phenotypes.items, function(index, pheno){
				container.append(pheno.HPODescription + '<br/>');
			});
		});
				
		// get genes for this disease
		restApi.getAsync('/api/v1/DiseaseMapping', {
			'q' : {
				'q' : [{
					field : 'diseaseId',
					operator : 'EQUALS',
					value : diseaseId
				}],	
				'num': 10000
			},
			'attributes': ['geneSymbol']
		}, function(diseaseGenes){
			
			// get unique genes for this disease
			var uniqueGenes = [];
			$.each(diseaseGenes.items, function(index, disease){
				if($.inArray(diseaseGenes.geneSymbol, uniqueGenes) === -1){
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
		});
	}
	
	/**
	 * Uses the genes in the currently selected entity to look for diseases and updates the list by
	 * calling populateDiseaseList() and initPager(). 
	 */
	function updateDiseaseList(){
		var entityName = getEntity().name;
		restApi.getAsync('/api/v1/' + entityName, {'attributes': ['geneSymbol'], 'q': currentQuery}, function(entityData){
			var uniqueGeneSymbols = [];
			if (entityData.total > 0){
				$.each(entityData.items, function(index, entity){
					if($.inArray(entity.geneSymbol, uniqueGeneSymbols) === -1){
						uniqueGeneSymbols.push(entity.geneSymbol);
					}
				});
			}else{
				currentDiseases = [];
			}
			
			//TODO change num: 10000 to a sensible solution (get unique)
			// fetch diseases based on list of genes
			restApi.getAsync('/api/v1/DiseaseMapping', {
				'q' : {
					'q' : [{
						field : 'geneSymbol',
						operator : 'IN',
						value : uniqueGeneSymbols
					}],	
					'num': 10000
				},
				'attributes': ['diseaseId']
			}, function(diseaseInfo){	
				var uniqueDiseases = [];
				if (diseaseInfo.total > null){
					$.each(diseaseInfo.items, function(index, disease){
						if($.inArray(disease.diseaseId, uniqueDiseases) === -1){
							uniqueDiseases.push(disease.diseaseId);
						}
					});	
					
					currentDiseases = uniqueDiseases;
				}else{
					currentDiseases = [];
				}	

				// add the diseases (if any) to the disease list and init the pager
				populateDiseaseList(currentDiseases.slice(0, diseaseListMaxLength));
				initPager();
			});
		});	
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