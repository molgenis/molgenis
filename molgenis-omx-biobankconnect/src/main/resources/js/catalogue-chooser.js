(function($, molgenis) {
	"use strict";
	
	var selectedDataSet = null;
	var restApi = new molgenis.RestClient();
	
	molgenis.CatalogueChooser = function OntologyAnnotator(){};
	
	molgenis.CatalogueChooser.prototype.changeDataSet = function(selectedDataSetId){
		if(selectedDataSetId !== null && selectedDataSetId !== undefined && selectedDataSetId !== ''){
			selectedDataSet = restApi.get('/api/v1/dataset/' + selectedDataSetId, {'expand' : ['ProtocolUsed']});
			var attributes = restApi.get('/api/v1/' + selectedDataSet.Identifier + '/meta', {'expand' : ['attributes']});	
			$('#selected-catalogue').empty().append(selectedDataSet.Name);
			$('#catalogue-name').empty().append(selectedDataSet.Name);
			$('#dataitem-number').empty().append(Object.keys(attributes.attributes).length);
			updateMatrix(molgenis.hrefToId(selectedDataSet.href));
			initSearchDataItems();
		}else{
			$('#catalogue-name').empty().append('Nothing selected');
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function initSearchDataItems() {
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
					updateMatrix(molgenis.hrefToId(selectedDataSet.href));
			    }
			});
			$('#search-button').click(function(){
				updateMatrix(molgenis.hrefToId(selectedDataSet.href));
			});
		}
	};
	
	function updateMatrix(dataSetId){
		molgenis.createMatrixForDataItems({
			'dataSetId' : dataSetId,
			'tableHeaders' : ['Name', 'Description'],
			'queryText' : $('#search-dataitem').val(),
			'sortRule' : null,
			'createTableRow' : createTableRow,
			'container' : $('#container')
		});
	}
	
	function createTableRow(feature){
		var row = $('<tr />');
		var description = feature.description;
		var isPopOver = description.length < 120;
		var descriptionSpan = $('<span />').html(isPopOver ? description : description.substring(0, 120) + '...');
		if(!isPopOver){
			descriptionSpan.addClass('show-popover');
			descriptionSpan.popover({
				content : description,
				trigger : 'hover',
				placement : 'bottom'
			});
		}
		var featureNameSpan = $('<span>' + feature.name + '</span>');
		$('<td />').append(featureNameSpan).appendTo(row);
		$('<td />').append(descriptionSpan).appendTo(row);
		return row;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));
