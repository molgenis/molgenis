(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
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
			var mappingPerStudy = {};
			var involedDataSets = [];
			
			var dataSetMapping = restApi.get('/api/v1/dataset/', null, {
				q : [{
					field : 'identifier',
					operator : 'LIKE',
					value : getSelectedDataSet() + '-'
				}],
			});
			$.each(dataSetMapping.items, function(index, dataSet){
				var dataSetsId = dataSet.identifier.split('-');
				var mappingDataSet = restApi.get('/api/v1/dataset/' + dataSetsId[1]);
				involedDataSets.push(mappingDataSet.name);
				mappingPerStudy[mappingDataSet.name] = createMapping(dataSet);
			});
			
			//create table header
			createDynamicTableHeader(involedDataSets);
			var searchHits = searchResponse.searchHits;
			$.each(searchHits, function(){
				var feature = $(this)[0]["columnValueMap"];
				var row = $('<tr />');
				var popover = $('<span />').html(feature.name).addClass('show-popover');
				popover.popover({
					content : feature.description,
					trigger : 'hover',
					placement : 'right'
				});
				$('<td />').addClass('add-border').append(popover).appendTo(row);
				$.each(mappingPerStudy, function(dataSetName, mapping){
					if(mapping[feature.id]){
						var value = '';
						var count = 0;
						var confirmed = false;
						$.each(mapping[feature.id], function(index, eachValue){
							if(count === 0){
								value = eachValue.mappedFeature.name;
							}
							if(eachValue.confirmed === true){
								confirmed = true;
								return false;
							}
							count++;
						});
						var editIcon = $('<i />');
						if(confirmed === true){
							editIcon.addClass('icon-ok show-popover');
						}else{
							editIcon.addClass('icon-pencil show-popover');
						}
						editIcon.css({
							position : 'relative',
							float : 'right'
						}).click(function(){
							var components = [];
							components.push(createFeatureTable(feature));
							components.push(createMappingTable(feature, mapping, dataSetName));
							standardModal.createModal('Candidate mappings', components);
							standardModal.modal.css({
								'width' : 900,
								'margin-left' : -450
							});
						});
						$('<td />').addClass('add-border').append('<span>' + value + '</span>').append(editIcon).appendTo(row);
					}else{
						$('<td />').addClass('add-border').append('<i class="icon-ban-circle show-popover" title="Not available"></i>').appendTo(row);
					}
				});
				$(row).appendTo($('#dataitem-table'));
			});
			
			pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()) - 1);
			pagination.updateMatrixPagination($('.pagination ul'), ns.createMatrixForDataItems);
		});
		
		function createFeatureTable(feature){
			var div = $('<div />').css('margin-left', -20);
			var dataSet = restApi.get('/api/v1/dataset/' + getSelectedDataSet());
			$('<div />').append('<h4>' + dataSet.name + '</h4>').appendTo(div);
			$('<div />').append('<span class="info"><strong>Data item to be mapped : </strong></span>').append('<span>' + feature.name + '</span>').appendTo(div);
			$('<div />').append('<span class="info"><strong>Data item description : </strong></span>').append('<span>' + i18nDescription(feature).en + '</span>').appendTo(div);
			var button = $('<button class="btn">Confirm</button>');
			div.append('<br /><br />');
			button.appendTo(div);
			return $('<div />').addClass('span4').append(div);
		}
		
		function createMappingTable(feature, mapping, mappedBiobank){
			var tableDiv = $('<div />').addClass('span7').css('margin-right' , -100);
			$('<div />').append('<h4>' + mappedBiobank + '</h4>').appendTo(tableDiv);
			var mappingTable = $('<table />').addClass('table table-bordered'); 
			var header = $('<tr><th>Name</th><th>Description</th><th>Select</th></tr>');
			mappingTable.append(header);
			$.each(mapping[feature.id], function(index, eachMapping){
				var mappedFeature = eachMapping.mappedFeature;
				var row = $('<tr />');
				var checkBox = $('<label class="checkbox"><input type="checkbox"></label>');
				if(eachMapping.confirmed){
					checkBox.find('input').attr('checked', true);
				}
				checkBox.data('mappedFeature', mappedFeature);
				row.append('<td>' + mappedFeature.name + '</td><td>' + i18nDescription(mappedFeature).en + '</td>').append($('<td />').append(checkBox)).appendTo(mappingTable);
			});
			return tableDiv.append().append(mappingTable);
		}
		
		function i18nDescription(feature){
			if(feature.description === undefined) feature.description = '';
			if(feature.description.indexOf('{') !== 0){
				feature.description = '{"en":"' + (feature.description === null ? '' : feature.description) +'"}';
			}
			return eval('(' + feature.description + ')');
		}

		
		function createMapping(dataSet){
			var tuple = {};
			var observationSets = restApi.get('/api/v1/observationset/', null, {
				q : [{
					field : 'partOfDataSet',
					operator : 'EQUALS',
					value : hrefToId(dataSet.href)
				}],
				num : 500
			});
			
			var storeMappingFeature = 'store_mapping_feature';
			var storeMappingMappedFeature = 'store_mapping_mapped_feature';
			var storeMappingConfirmMapping = 'store_mapping_confirm_mapping';
			if(observationSets.items.length > 0){
				var map = {};
				var observationIds = [];
				$.each(observationSets.items, function(index, observation){
					observationIds.push(hrefToId(observation.href));
				});
				var observedValues = restApi.get('/api/v1/observedvalue/', ['feature', 'value', 'observationSet'], {
					num : 500,
					q : [{
						field : 'observationset',
						operator : 'IN',
						value : observationIds
					}],
				});
				preLoadValueEntities(observedValues.items, map);
				
				$.each(map, function(key, value){
					var dataItem = value[storeMappingFeature] === undefined ? null : value[storeMappingFeature];
					var mappedDataItems = value[storeMappingMappedFeature] === undefined ? null : value[storeMappingMappedFeature];
					var confirmMapping = value[storeMappingConfirmMapping] === undefined ? null : value[storeMappingConfirmMapping];
					if(dataItem !== null && mappedDataItems !== null){
						if(!tuple[hrefToId(dataItem.value.href)])
							tuple[hrefToId(dataItem.value.href)] = [];
						tuple[hrefToId(dataItem.value.href)].push({
							'mappedFeature' : mappedDataItems.value,
							
						});
					}
				});
			}
			
			return tuple;
		}
		
		function preLoadValueEntities(observedValues, map){
			
			var storeMappingFeature = 'store_mapping_feature';
			var storeMappingMappedFeature = 'store_mapping_mapped_feature';
			var storeMappingConfirmMapping = 'store_mapping_confirm_mapping';
			var valueType = {'xref':[], 'bool' : []};
			$.each(observedValues, function(index, observedValue){
				var featureIdentifier = observedValue.feature.identifier;
				if(featureIdentifier === storeMappingFeature || featureIdentifier === storeMappingMappedFeature)
					valueType.xref.push(hrefToId(observedValue.value.href));
				else if(featureIdentifier === storeMappingConfirmMapping)
					valueType.bool.push(hrefToId(observedValue.value.href));
			});
			restApi.get('/api/v1/xrefvalue/', ['value'], {
				q : [{
					field : 'id',
					operator : 'IN',
					value : valueType.xref
				}],
				num : 500
			});
			restApi.get('/api/v1/boolvalue/', ['value'], {
				q : [{
					field : 'id',
					operator : 'IN',
					value : valueType.bool
				}],
				num : 500
			});
			
			$.each(observedValues, function(index, observedValue){
				if(!map[observedValue.observationSet.href])
					map[observedValue.observationSet.href] = {};
				var data = map[observedValue.observationSet.href];
				if(observedValue.feature.identifier === storeMappingFeature){
					data[storeMappingFeature] = getDataValueId(observedValue.feature, hrefToId(observedValue.value.href));
				}else if(observedValue.feature.identifier === storeMappingMappedFeature){
					data[storeMappingMappedFeature] = getDataValueId(observedValue.feature, hrefToId(observedValue.value.href));
				}else if(observedValue.feature.identifier === storeMappingConfirmMapping){
					data[storeMappingConfirmMapping] = getDataValueId(observedValue.feature, hrefToId(observedValue.value.href));
				}
				map[observedValue.observationSet.href] = data;
			});
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
			var valueEntity = restApi.get(prefix + valueId);
			return valueEntity;
		}

		function createDynamicTableHeader(involedDataSets){
			var dataSet = restApi.get('/api/v1/dataset/' + getSelectedDataSet());
			var table = $('#dataitem-table');
			table.empty();
			var row = $('<tr />');
			row.append('<th>' + dataSet.name + '</th>');
			
			$.each(involedDataSets, function(index, dataSetName){
				row.append('<th>' + dataSetName + '</th>');
			});
			$('<thead />').append(row).appendTo(table);
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