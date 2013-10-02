(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	
	ns.CatalogueChooser = function OntologyAnnotator(){
		
	};
	
	ns.CatalogueChooser.prototype.changeDataSet = function(selectedDataSet){
		if(selectedDataSet !== ''){
			var dataSetEntity = restApi.get('/api/v1/dataset/' + selectedDataSet);
			$('#selected-catalogue').empty().append(dataSetEntity.name);
			var request = {
				documentType : 'protocolTree-' + ns.hrefToId(dataSetEntity.href),
				queryRules : [{
					field : 'type',
					operator : 'EQUALS',
					value : 'observablefeature'
				}]
			};
			searchApi.search(request, function(searchResponse){
				$('#catalogue-name').empty().append(dataSetEntity.name);
				$('#dataitem-number').empty().append(searchResponse.totalHitCount);
				pagination.reset();
				ns.CatalogueChooser.prototype.updateSelectedDataset(selectedDataSet);
				ns.CatalogueChooser.prototype.createMatrixForDataItems();
				initSearchDataItems(dataSetEntity);
			});
		}else{
			$('#catalogue-name').empty().append('Nothing selected');
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function initSearchDataItems (dataSet) {
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					ns.CatalogueChooser.prototype.dataItemsTypeahead('observablefeature', ns.hrefToId(dataSet.href), query, process);
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
					ns.CatalogueChooser.prototype.createMatrixForDataItems();
			    }
			});
			$('#search-button').click(function(){
				ns.CatalogueChooser.prototype.createMatrixForDataItems();
			});
		}
	};
	
	ns.CatalogueChooser.prototype.createMatrixForDataItems = function(queryRule) {
		var documentType = 'protocolTree-' + getSelectedDataSet();
		var query = [{
			field : 'type',
			operator : 'SEARCH',
			value : 'observablefeature'
		}];
		
		var queryText = $('#search-dataitem').val();
		if(queryText !== ''){
			query.push({
				operator : 'AND'
			});
			query.push({
				operator : 'SEARCH',
				value : queryText
			});
			pagination.reset();
		}
		
		searchApi.search(pagination.createSearchRequest(documentType, query), function(searchResponse) {
			var searchHits = searchResponse.searchHits;
			var tableObject = $('#dataitem-table');
			var tableBody = $('<tbody />');
			
			$.each(searchHits, function(){
				$(createTableRow($(this)[0]["columnValueMap"])).appendTo(tableBody);
			});
			
			tableObject.empty().append(createTableHeader()).append(tableBody);
			pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()));
			pagination.updateMatrixPagination($('.pagination ul'), ns.CatalogueChooser.prototype.createMatrixForDataItems);
		});
		
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
		
		function createTableHeader(){
			var headerRow = $('<tr />');
			$('<th>Name</th>').css('width', '30%').appendTo(headerRow);
			$('<th>Description</th>').css('width', '70%').appendTo(headerRow);
			return $('<thead />').append(headerRow);
		}
		
		function getSelectedDataSet(){
			return selectedDataSet;
		}
	};
	
	ns.CatalogueChooser.prototype.updateIndex = function(ontologyTermIRI, boost){
		var updateRequest = {
			'ontologyTermIRI' : ontologyTermIRI,
			'updateScript' : 'boost=' + boost
		};
		$.ajax({
			type : 'POST',
			url : ns.getContextURL() + '/update',
			async : false,
			data : JSON.stringify(updateRequest),
			contentType : 'application/json',
		});
	}
	
	ns.CatalogueChooser.prototype.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	ns.CatalogueChooser.prototype.dataItemsTypeahead = function (type, dataSetId, query, response){
		var queryRules = [{
			field : 'type',
			operator : 'EQUALS',
			value : type,
		},{
			operator : 'AND'
		},{
			operator : 'SEARCH',
			value : query
		},{
			operator : 'LIMIT',
			value : 20
		}];
		var searchRequest = {
			documentType : 'protocolTree-' + dataSetId,
			queryRules : queryRules
		};
		searchApi.search(searchRequest, function(searchReponse){
			var result = [];
			var dataMap = {};
			$.each(searchReponse.searchHits, function(index, hit){
				var value = hit.columnValueMap.ontologyTerm;
				if($.inArray(value, result) === -1){
					var name = hit.columnValueMap.name;
					result.push(name);
					dataMap[name] = hit.columnValueMap;
				}
			});
			$(document).data('dataMap', dataMap);
			response(result);
		});
	};
}($, window.top));