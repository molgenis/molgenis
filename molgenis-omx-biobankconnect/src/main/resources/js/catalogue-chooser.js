(function($, molgenis) {
	"use strict";
	
	var selectedDataSet = null;
	var restApi = new molgenis.RestClient();
	
	molgenis.CatalogueChooser = function OntologyAnnotator(){};
	
	molgenis.CatalogueChooser.prototype.changeDataSet = function(selectedDataSetId){
		if(selectedDataSetId !== null && selectedDataSetId !== undefined && selectedDataSetId !== ''){
			selectedDataSet = restApi.get('/api/v1/dataset/' + selectedDataSetId, {'expand' : ['ProtocolUsed']});	
			$('#selected-catalogue').empty().append(selectedDataSet.Name);
			$('#catalogue-name').empty().append(selectedDataSet.Name);
			$('#dataitem-number').empty().append(molgenis.getTotalNumberOfItems(selectedDataSetId));
			$('#search-dataitem').val('');
			updateMatrix();
			initSearchDataItems();
		}else{
			$('#catalogue-name').empty().append('Nothing selected');
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function initSearchDataItems() {
			var options = {'updatePager' : true};
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					molgenis.dataItemsTypeahead(molgenis.hrefToId(selectedDataSet.href), query, process);
				},
				minLength : 3,
				items : 20
			}).on('keydown', function(e){
			    if (e.which == 13) {
			    	$('#search-button').click();
			    	return false;
			    }
			}).on('keyup', function(e){
				if($(this).val() === ''){
					updateMatrix(options);
			    }
			});
			$('#search-button').click(function(){
				updateMatrix(options);
			});
		}
	};
	
	function updateMatrix(options){
		var default_options = {
			'dataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'tableHeaders' : ['Name', 'Description'],
			'queryText' : $('#search-dataitem').val(),
			'sortRule' : null,
			'createTableRow' : null,
			'updatePager' : false,
			'container' : $('#container')
		}
		if(options !== undefined && options !== null){
			$.extend(default_options, options);
		}
		molgenis.createMatrixForDataItems(default_options);
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));
