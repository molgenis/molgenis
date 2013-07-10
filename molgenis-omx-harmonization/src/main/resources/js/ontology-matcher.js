(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var pagination = new ns.Pagination();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	
	ns.changeDataSet = function(selectedDataSet){
		pagination.reset();
		ns.updateSelectedDataset(selectedDataSet);
		ns.createMatrixForDataItems();
	};
	
	ns.createMatrixForDataItems = function() {
		searchApi.search(pagination.createSearchRequest(getSelectedDataSet()), function(searchResponse) {
			var mappingPerStudy = [];
			var involedDataSets = [];
			
			var dataSetMapping = restApi.get('/api/v1/dataset/', null, {
				q : [{
					field : 'identifier',
					operator : 'LIKE',
					value : getSelectedDataSet()
				}],
			});
			$.each(dataSetMapping.items, function(index, dataSet){
				var dataSetsId = dataSet.identifier.split('-');
				var mappingDataSet = restApi.get('/api/v1/dataset/' + dataSetsId[1]);
				involedDataSets.push(mappingDataSet.name);
				mappingPerStudy.push(createMapping(dataSet));
			});
			
			//create table header
			createDynamicTableHeader(involedDataSets);
			var searchHits = searchResponse.searchHits;
			$.each(searchHits, function(){
				var feature = $(this)[0]["columnValueMap"];
				var row = $('<tr />');
				row.append('<td>' + feature.name + '</td>');
				$.each(mappingPerStudy, function(index, mapping){
					if(mapping[feature.id]){
						var value = '';
						var count = 0;
						$.each(mapping[feature.id], function(index, eachValue){
							if(count == 0){
								value = eachValue.name;
								count++;
							}
						});
						row.append('<td>' + value + '</td>');
						createModalForEdit(feature, mapping);
					}else{
						row.append('<td />');
					}
				});
				$(row).appendTo($('#dataitem-table'));
			});
			
			pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()) - 1);
			pagination.updateMatrixPagination($('.pagination ul'), ns.createMatrixForDataItems);
		});
		
		function createModalForEdit(feature, mapping){
			
		}
		
		function createMapping(dataSet){
			var tuple = {};
			var observationSets = restApi.get('/api/v1/observationset/', null, {
				q : [{
					field : 'partOfDataSet',
					operator : 'EQUALS',
					value : hrefToId(dataSet.href)
				}]
			});
			
			var storeMappingFeature = 'store_mapping_feature';
			var storeMappingMappedFeature = 'store_mapping_mapped_feature';
			var map = {};
			if(observationSets.items.length > 0){
				var observationIds = [];
				$.each(observationSets.items, function(index, observation){
					observationIds.push(hrefToId(observation.href));
				});
				var observedValues = restApi.get('/api/v1/observedvalue/', ['feature', 'value', 'observationSet'], {
					q : [{
						field : 'observationset',
						operator : 'IN',
						value : observationIds
					}],
				});
				
				$.each(observedValues.items, function(index, observedValue){
					if(!map[observedValue.observationSet.href])
						map[observedValue.observationSet.href] = {};
					else {
						console.log();
					}
					var data = map[observedValue.observationSet.href];
					if(observedValue.feature.identifier === storeMappingFeature){
						data[storeMappingFeature] = getDataValueId(observedValue.feature, hrefToId(observedValue.value.href));
					}else if(observedValue.feature.identifier === storeMappingMappedFeature){
						data[storeMappingMappedFeature] = getDataValueId(observedValue.feature, hrefToId(observedValue.value.href));
					}
					map[observedValue.observationSet.href] = data;
				});
				
				var map = {};
				$.each(observedValues.items, function(index, observedValue){
					if(!map[observedValue.observationSet.href])
						map[observedValue.observationSet.href] = {};
					else {
						console.log();
					}
					var data = map[observedValue.observationSet.href];
					if(observedValue.feature.identifier === storeMappingFeature){
						data[storeMappingFeature] = getDataValueId(observedValue.feature, hrefToId(observedValue.value.href));
					}else if(observedValue.feature.identifier === storeMappingMappedFeature){
						data[storeMappingMappedFeature] = getDataValueId(observedValue.feature, hrefToId(observedValue.value.href));
					}
					map[observedValue.observationSet.href] = data;
				});
				
				$.each(map, function(key, value){
					var dataItem = value[storeMappingFeature] === undefined ? null : value[storeMappingFeature];
					var mappedDataItems = value[storeMappingMappedFeature] === undefined ? null : value[storeMappingMappedFeature];
					if(dataItem !== null && mappedDataItems !== null){
						console.log(dataItem);
						console.log(mappedDataItems);
						tuple[hrefToId(dataItem.value.href)] = mappedDataItems.value.items;
					}
				});
			}
			
			return tuple;
		}
		
		function getDataValueId(feature, valueId){
			var prefix = '';
			switch(feature.dataType){
				case 'xref' : 
					prefix = '/api/v1/xrefvalue/';
					break;
				case 'mref' : 
					prefix = '/api/v1/mrefvalue/';
					break;
				case 'categorical' :
					prefix = '/api/v1/categoricalvalue/';
					break;
				case 'bool' : 
					prefix = '/api/v1/boolvalue/';
					break;
			}
			var valueEntity = restApi.get(prefix + valueId, ['value']);
			return valueEntity;
		}

		function createDynamicTableHeader(involedDataSets){
			var dataSet = restApi.get('/api/v1/dataset/' + getSelectedDataSet());
			var table = $('#dataitem-table');
			table.empty();
			table.append('<thead />');
			var row = $('<tr />');
			row.append('<th>' + dataSet.name + '</th>');
			
			$.each(involedDataSets, function(index, dataSetName){
				row.append('<th>' + dataSetName + '</th>');
			});
			table.append(row);
		}
	}
	
	ns.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	function hrefToId(href){
		return href.substring(href.lastIndexOf('/') + 1); 
	}
	
	function getSelectedDataSet(){
		return selectedDataSet;
	}
	
	$(function() {
		
	});
}($, window.top));