(function($, molgenis) {
	var restApi = new molgenis.RestClient();
	
	var currentQuery = null;
	var currentDiseases = null;
	
	var diseaseListMaxLength = 10;
	
	/**
	 * Listen for data explorer query changes.
	 */
	$(document).on('changeQuery', function(e, query){
		currentQuery = query;

		//update disease list
		updateDiseaseList();
	});
	
	
	$(function() {
		//TODO check if DiseaseMapping is loaded and give warning + instructions if not
		//TODO check if suitable annotation has been added (gene symbol column), and give warning + instructions if not
		
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
		container = $('#disease-pager');
		if(currentDiseases != null && currentDiseases.length > diseaseListMaxLength) {
			container.pager({
				'nrItems' : currentDiseases.length,
				'nrItemsPerPage' : 10,
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
			$('#disease-list').append('<li><a href="">' + disease + '</a></li>');
		});
		
		//set first disease as the active one
		$('#disease-list li').first().attr("class", "active");
	}
	
	/**
	 * Uses the genes in the currently selected entity to look for diseases. 
	 * @returns array of diseaseIds, empty array when none found 
	 */
	function getDiseases(){
		entityName = getEntity().name;
		console.log(entityName);
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
				'attributes': ['diseaseId'],
				'num': 10000
			}
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
	
})($, window.top.molgenis = window.top.molgenis || {});