(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	var storeMappingFeature = 'store_mapping_feature';
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var storeMappingConfirmMapping = 'store_mapping_confirm_mapping';
	
	ns.changeDataSet = function(selectedDataSet){
		pagination.reset();
		ns.updateSelectedDataset(selectedDataSet);
		ns.createMatrixForDataItems();
	};
	
	ns.createMatrixForDataItems = function() {
		var dataSetMapping = restApi.get('/api/v1/dataset/', null, {
			q : [{
				field : 'identifier',
				operator : 'LIKE',
				value : getSelectedDataSet() + '-'
			}],
		});
		if(dataSetMapping.items.length > 0){
			var documentType = 'protocolTree-' + dataSetMapping.items[0].identifier.split('-')[0];
			var query = {
				operator : 'SEARCH',
				value : 'observablefeature'
			};
			searchApi.search(pagination.createSearchRequest(documentType, query),function(searchResponse) {
				createMappingFromIndex(dataSetMapping.items, searchResponse);
			});
		}
		
		function createMappingFromIndex(dataSets, searchResponse){
			var totalHitCount = searchResponse.totalHitCount;
			var displayFeatures = searchResponse.searchHits;
			var queryRules = [];
			$.each(displayFeatures, function(index, hit){
				var hitInfo = hit.columnValueMap;
				queryRules.push({
					field : storeMappingFeature,
					operator : 'EQUALS',
					value : hitInfo.name
				});
				queryRules.push({
					operator : 'OR'
				})
			});
			queryRules.pop();
			queryRules.push({
				'operator' : 'LIMIT',
				'value' : 100000 
			});
			var mappingPerStudy = {};
			var involedDataSets = [];
			$.each(dataSets, function(index, dataSet){
				var tuple = {};
				var featureCollections = [];
				var allFeatureCollection = [];
				var searchRequest = {
					documentType : dataSet.identifier,
					queryRules :queryRules
				};	
				var count = 0;
				searchApi.search(searchRequest, function(searchResponse) {
					var searchHits = searchResponse.searchHits;	
					var confirmedMappingIds = [];
					var observationIds = [];
					$.each(searchHits, function(index, hit){
						var mapping = hit.columnValueMap;
						var observationSet = mapping['observation_set'];
						observationIds.push(observationSet);
					});
					
					var valuesObject = restApi.get('/api/v1/observedvalue', ['observationSet',  'value'], {
						q : [{
							field : 'feature_identifier',
							operator : 'EQUALS',
							value : storeMappingConfirmMapping
						},{
							field : 'observationSet',
							operator : 'IN',
							value : observationIds
						}],
						num : 500
					});
					var mappingConfirmMap = {};
					var boolValueIds = [];
					var boolValueObjects = {};
					if(valuesObject !== undefined && valuesObject.items.length > 0){
						$.each(valuesObject.items, function(index, ov){
							boolValueIds.push(hrefToId(ov.value.href));
						});
						restApi.get('/api/v1/boolvalue', null, {
							q : [{
								field : 'id',
								operator : 'IN',
								value : boolValueIds
							}],
							num : 500
						});
						
						$.each(valuesObject.items, function(index, ov){
							mappingConfirmMap[hrefToId(ov.observationSet.href)] = restApi.get('/api/v1/boolvalue/' + hrefToId(ov.value.href));
						});
					}
					var count = 0;
					$.each(searchHits, function(index, hit){
						var mapping = hit['columnValueMap'];
						var documentId = hit['id'];
						var feature = mapping[storeMappingFeature];
						var storeMappedFeature = mapping[storeMappingMappedFeature];
						var observationSet = mapping['observation_set'];
						var confirmed = false;
						if(mappingConfirmMap[observationSet])
							confirmed = mappingConfirmMap[observationSet].value;
						if(!tuple[feature])
							tuple[feature] = [];
						tuple[feature].push({
							'mappedFeature' : storeMappedFeature,
							'confirmed' : confirmed,
							'observationSet' : observationSet,
							'documentId' : documentId
						});
						//Put pre-load feature in the array and to be loaded via restApi
						if($.inArray(feature, featureCollections) === -1) featureCollections.push(feature);
						allFeatureCollection.push(feature);
						allFeatureCollection.push(storeMappedFeature);
					});
					$.each(displayFeatures, function(index, hit){
						var hitInfo = hit.columnValueMap;
						if($.inArray(hitInfo.name, featureCollections) === -1){
							tuple[hitInfo.name] = null;
							allFeatureCollection.push(hitInfo.name);
						}
					});
					var listOfFeatures = restApi.get('/api/v1/observablefeature', null, {
						q : [{
							field : 'name',
							operator : 'IN',
							value : allFeatureCollection
						}],
						num : 500
					});
					var cachedFeatures = {};
					$.each(listOfFeatures.items, function(index, element){
						cachedFeatures[element.name] = element;
					});
					var dataSetIdArray = dataSet.identifier.split('-');
					mappingPerStudy[dataSetIdArray[1]] = tuple;
					count++;
					if(count === dataSets.length) createTable(mappingPerStudy, dataSets, totalHitCount, cachedFeatures);
				});
			});
		}
		
		function createTable(mappingPerStudy, dataSets, totalHitCount, cachedFeatures){
			//create table header
			var involedDataSets = [];
			$.each(dataSets, function(index, dataSet){
				var dataSetIdArray = dataSet.identifier.split('-');
				var mappedDataSet = restApi.get('/api/v1/dataset/' + dataSetIdArray[1]);
				involedDataSets.push(mappedDataSet.name);
			});
			createDynamicTableHeader(involedDataSets);
			$.each(dataSets, function(index, dataSet){
				var dataSetIdArray = dataSet.identifier.split('-');
				var mappedDataSet = restApi.get('/api/v1/dataset/' + dataSetIdArray[1]);
				$.each(mappingPerStudy[dataSetIdArray[1]], function(featureName, mappedFeatures){
					var feature = cachedFeatures[featureName];
					var row = $('<tr />');
					var popover = $('<span />').html(feature.name + ' : ' + feature.description).addClass('show-popover');
					popover.popover({
						content : feature.description,
						trigger : 'hover',
						placement : 'right'
					});
					$('<td />').addClass('add-border').append(popover).appendTo(row);
					if(mappedFeatures){
						var displayTerm = '';
						var description = '';
						var count = 0;
						var confirmed = false;
						var selectedMappings = [];
						$.each(mappedFeatures, function(index, eachValue){
							eachValue.mappedFeature = cachedFeatures[eachValue.mappedFeature];
							if(count === 0){
								displayTerm = eachValue.mappedFeature.name;
								description = eachValue.mappedFeature.description;
							}
							if(eachValue.confirmed === true){
								selectedMappings.push(eachValue.mappedFeature.name);
								confirmed = true;
							}
							count++;
						});
						displayTerm = selectedMappings.length > 0 ? selectedMappings.join(' , ') : displayTerm;
						var removeIcon = $('<i />').addClass('icon-trash show-popover').css({
							position : 'relative',
							float : 'right'
						}).click(function(){
							var documentIds = [];
							$.each(mappedFeatures, function(index, element){
								documentIds.push(element.documentId);
							});
							removeAnnotation(mappedFeatures);
							var deleteRequest = {
								'documentType' : dataSet.identifier,
								'documentIds' : documentIds
							};
							$.ajax({
								type : 'POST',
								url : '/plugin/ontologymatcher/delete',
								async : false,
								data : JSON.stringify(deleteRequest),
								contentType : 'application/json',
							});
							ns.createMatrixForDataItems();
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
							standardModal.createModalCallback('Candidate mappings', function(modal){
								createMappingTable(feature, mappedFeatures, mappedDataSet, modal);
							});
							standardModal.modal.css({
								'width' : 950,
								'margin-left' : -475
							});
						});
						$('<td />').addClass('add-border').append('<span>' + displayTerm + '</span>').append(removeIcon).append(editIcon).appendTo(row);
					}else{
						$('<td />').addClass('add-border').append('<i class="icon-ban-circle show-popover" title="Not available"></i>').appendTo(row);
					}
					$(row).appendTo($('#dataitem-table'));
				});
			});
			pagination.setTotalPage(Math.ceil(totalHitCount / pagination.getPager()) - 1);
			pagination.updateMatrixPagination($('.pagination ul'), ns.createMatrixForDataItems);
		}
		
		function removeAnnotation(mappings){
			var observationSetIds = [];
			$.each(mappings, function(index, eachMapping){
				observationSetIds.push(eachMapping.observationSet);
			});
			showMessage('alert alert-info', observationSetIds.length + ' candidate mappings are being deleted!');
			var observedValues = restApi.get('/api/v1/observedvalue', null, {
				q : [{
					field : 'observationSet',
					operator : 'IN',
					value : observationSetIds
				}],
				num : 500
			});
			
			var observedValueIds = [];
			$.each(observedValues.items, function(index, ov){
				observedValueIds.push(hrefToId(ov.href));
			});
			deleteEntity('/api/v1/observedvalue/', observedValueIds, function(){deleteEntity('/api/v1/observationset/', observationSetIds, null)});
		}
		
		function deleteEntity(entityType, ids, callback){
			var workers = [];
			for(var i = 0 ; i < ids.length ; i++) {
				workers[i] = false;
			}
			for(var i = 0 ; i < ids.length ; i++) {
				$.ajax({
					type : 'DELETE',
					async : false,
					url : entityType + ids[i],
					success : function(data, textStatus, request) {
						workers[i] = true;
						if($.inArray(false, workers) === -1){
							if(callback !== null)
								callback();
						}
					},
					error : function(request, textStatus, error){
						console.log(error);
					} 
				});
			}
		}
		
		function createMappingTable(feature, mappedFeatures, mappedDataSet, modal){
			var selectedFeatures = [];
			var tableDiv = $('<div />').addClass('span7').css('margin-right', -100);
			$('<div />').append('<h4>' + mappedDataSet.name + '</h4>').appendTo(tableDiv);
			var mappingTable = $('<table />').addClass('table table-bordered table-striped'); 
			var header = $('<tr><th>Name</th><th>Description</th><th>Select</th></tr>');
			mappingTable.append(header);
			$.each(mappedFeatures, function(index, eachMapping){
				var mappedFeature = eachMapping.mappedFeature;
				var row = $('<tr />');
				var checkBox = $('<input type="checkbox">');
				if(eachMapping.confirmed){
					checkBox.attr('checked', true);
					selectedFeatures.push(mappedFeature.name);
				}
				checkBox.data('eachMapping', eachMapping);
				row.append('<td>' + mappedFeature.name + '</td><td>' + i18nDescription(mappedFeature).en + '</td>');
				row.append($('<td />').append($('<label class="checkbox"></label>').append(checkBox))).appendTo(mappingTable);
			});
			tableDiv.append(mappingTable);
			var body = modal.find('.modal-body:eq(0)');
			$('<div />').append('<div class="span4"></div>').append(tableDiv).appendTo(body);
			
			var infoDiv = $('<div />').addClass('span4').css({
				'position' : 'absolute',
				'margin-left' : 0,
				'margin-top' : 25,
				'z-index' : 10000
			});
			var dataSet = restApi.get('/api/v1/dataset/' + getSelectedDataSet());
			$('<div />').append('<h4>' + dataSet.name + '</h4>').appendTo(infoDiv);
			$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.name + '</span>').appendTo(infoDiv);
			$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + i18nDescription(feature).en + '</span>').appendTo(infoDiv);
			var selectedMappings = $('<div />').append('<span class="info"><strong>Selected mappings : </strong></span>').append('<span>' + selectedFeatures.join(' , ') + '</span>').appendTo(infoDiv);
			var confirmButton = $('<button class="btn">Confirm</button>');
			infoDiv.append('<br /><br />');
			confirmButton.appendTo(infoDiv).click(function(){
				updateMappingInfo(feature, mappingTable, mappedDataSet, ns.createMatrixForDataItems);
				standardModal.closeModal();
			});
			var footer = modal.find('.modal-header:eq(0)');
			footer.append(infoDiv);
			
			tableDiv.find('input[type="checkbox"]').click(function(){
				var dataItems = [];
				tableDiv.find('input:checked').each(function(index, checkbox){
					dataItems.push($(checkbox).data('eachMapping').mappedFeature.name);
				});
				selectedMappings.find('span:eq(1)').html(dataItems.join(' , '));
			});
		}
		
		function updateMappingInfo(feature, mappingTable, mappedDataSet, callback){
			var ifShowMessage = false;
			mappingTable.find('input').each(function(index, checkBox){
				var eachMapping = $(checkBox).data('eachMapping');
				var changedValue = null;
				if(checkBox.checked && !eachMapping.confirmed){
					changedValue = true;
					ifShowMessage = true;
				}
				if(!checkBox.checked && eachMapping.confirmed){
					changedValue = false;
					ifShowMessage = true;
				}
				if(changedValue !== null){
					var confirmFeature = restApi.get('/api/v1/observablefeature', null, {
						q : [{
							field : 'identifier',
							operator : 'EQUALS',
							value : storeMappingConfirmMapping
						}]
					});
					var observedValue = restApi.get('/api/v1/observedvalue', ['value'], {
						q : [{
							field : 'observationset',
							operator : 'EQUALS',
							value : eachMapping.observationSet
						},{
							field : 'feature',
							operator : 'EQUALS',
							value : hrefToId(confirmFeature.items[0].href)
						}]
					});
					var xrefValue = restApi.get('/api/v1/boolvalue/' + hrefToId(observedValue.items[0].value.href));
					xrefValue.value = changedValue;
					updateEntity(xrefValue.href, xrefValue);
					
					var updateRequest = {
						'documentType' : selectedDataSet + '-' + hrefToId(mappedDataSet.href),
						'documentIds' : [eachMapping.documentId],
						'updateScript' : storeMappingConfirmMapping + '=true',
					};
					$.ajax({
						type : 'POST',
						url : '/plugin/ontologymatcher/update',
						async : false,
						data : JSON.stringify(updateRequest),
						contentType : 'application/json',
					});
				}
			});
			if(ifShowMessage){
				showMessage('alert alert-info', 'the mapping(s) has been updated for <strong>' + feature.name + '</strong> in <strong>' + mappedDataSet.name + '</strong> Biobank!');
				callback();
			}
		}
		
		function showMessage(alertClass, message){
			var messageAlert = $('<div />').addClass(alertClass).append('<button type="button" class="close" data-dismiss="alert">&times;</button>');
			$('<span><strong>Message : </strong>' + message + '</span>').appendTo(messageAlert);
			messageAlert.appendTo('#alertMessage');
			w.setTimeout(function(){messageAlert.fadeOut(1000).remove()}, 10000);
		}
		
		function i18nDescription(feature){
			if(feature.description === undefined) feature.description = '';
			if(feature.description.indexOf('{') !== 0){
				feature.description = '{"en":"' + (feature.description === null ? '' : feature.description) +'"}';
			}
			return eval('(' + feature.description + ')');
		}

		function createMapping(dataSet){
			var batchSize = 500;
			var tuple = {};
			var observationSets = restApi.get('/api/v1/observationset/', null, {
				q : [{
					field : 'partOfDataSet',
					operator : 'EQUALS',
					value : hrefToId(dataSet.href)
				}],
				num : batchSize
			});
			var observationIds = [];
			if(observationSets.items.length > 0){
				var map = {};
				
				$.each(observationSets.items, function(index, observation){
					observationIds.push(hrefToId(observation.href));
				});
			}
			var iteration = Math.ceil(observationSets.total / batchSize);
			for(var i = 1; i < iteration; i++){
				var query = {
						q : [{
							field : 'partOfDataSet',
							operator : 'EQUALS',
							value : hrefToId(dataSet.href)
						}],
						num : batchSize,
						start : i * batchSize
				};
				observationSets = restApi.get('/api/v1/observationset/', null, query);
				$.each(observationSets.items, function(index, observation){
					observationIds.push(hrefToId(observation.href));
				});
			}
			if(observationIds.length > 0){	
				var observedValues = restApi.get('/api/v1/observedvalue/', ['feature', 'value', 'observationSet'], {
					start : 0,
					num : batchSize,
					q : [{
						field : 'observationset',
						operator : 'IN',
						value : observationIds
					}],
				});
				preLoadValueEntities(observedValues.items, map);
				iteration = Math.ceil(observedValues.total / batchSize);
				for(var i = 1; i < iteration; i++){
					var query = {
							q : [ {
								"field" : "observationset",
								"operator" : "IN",
								"value" : observationIds
							} ],
							num : batchSize,
							start : i * batchSize
					};
					observedValues = restApi.get('/api/v1/observedvalue/', ['feature', 'value', 'observationSet'], query);
					preLoadValueEntities(observedValues.items, map);
				}
				
				$.each(map, function(key, value){
					var dataItem = value[feature] === undefined ? null : value[feature];
					var mappedDataItems = value[storeMappingMappedFeature] === undefined ? null : value[storeMappingMappedFeature];
					var confirmMapping = value[storeMappingConfirmMapping] === undefined ? null : value[storeMappingConfirmMapping];
					if(dataItem !== null && mappedDataItems !== null){
						if(!tuple[hrefToId(dataItem.value.href)])
							tuple[hrefToId(dataItem.value.href)] = [];
						tuple[hrefToId(dataItem.value.href)].push({
							'mappedFeature' : mappedDataItems.value,
							'confirmed' : confirmMapping.value,
							'observationSetIdentifier' : key
						});
					}
				});
			}
			
			return tuple;
		}
		
		function preLoadValueEntities(observedValues, map){
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
		
		function updateEntity(href, data){
			$.ajax({
				type : 'PUT',
				dataType : 'json',
				url : href,
				cache: true,
				data : JSON.stringify(data),
				contentType : 'application/json',
				async : true,
				success : function(data, textStatus, request) {
					console.log(data);
				},
				error : function(request, textStatus, error){
					console.log(error);
				} 
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
	};
	
	ns.downloadMappings = function(){
		var dataSet = restApi.get('/api/v1/dataset/' + selectedDataSet);
		var deleteRequest = {
			'dataSetId' : selectedDataSet,
			'documentType' : dataSet.identifier
		};
		$.download('/plugin/ontologymatcher/download',{request : JSON.stringify(deleteRequest)});
	};
	
	ns.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	function hrefToId(href){
		return href.substring(href.lastIndexOf('/') + 1); 
	}
	
	function getSelectedDataSet(){
		return selectedDataSet;
	}
	
}($, window.top));