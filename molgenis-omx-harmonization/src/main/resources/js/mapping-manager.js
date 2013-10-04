(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	var sortRule = null;
	var storeMappingFeature = 'store_mapping_feature';
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var storeMappingConfirmMapping = 'store_mapping_confirm_mapping';
	var scoreMappingScore = "store_mapping_score";
	var scoreMappingAbsoluteScore = "store_mapping_absolute_score";
	
	ns.MappingManager = function MappingManager(){
		
	};
	
	ns.MappingManager.prototype.changeDataSet = function(selectedDataSet){
		ns.MappingManager.prototype.initAccordion();
		if(selectedDataSet !== ''){
			var dataSetEntity = restApi.get('/api/v1/dataset/' + selectedDataSet);
			var request = {
				documentType : 'protocolTree-' + ns.hrefToId(dataSetEntity.href),
				queryRules : [{
					field : 'type',
					operator : 'EQUALS',
					value : 'observablefeature'
				}]
			};
			searchApi.search(request, function(searchResponse){
				sortRule = null;
				$('#dataitem-number').empty().append(searchResponse.totalHitCount);
				pagination.reset();
				ns.MappingManager.prototype.updateSelectedDataset(selectedDataSet);
				ns.MappingManager.prototype.createMatrixForDataItems();
			});
		}else{
			$('#dataitem-number').empty().append('Nothing selected');
		}
	};
	
	ns.MappingManager.prototype.createMatrixForDataItems = function() {
		var dataSetMapping = restApi.get('/api/v1/dataset/', null, {
			q : [{
				field : 'identifier',
				operator : 'LIKE',
				value : ns.MappingManager.prototype.getSelectedDataSet() + '-'
			}],
		});
		if(dataSetMapping.items.length > 0){
			var documentType = 'protocolTree-' + dataSetMapping.items[0].identifier.split('-')[0];
			var query = [{
				operator : 'SEARCH',
				value : 'observablefeature'
			}];
			if(sortRule !== null) query.push(sortRule);
			searchApi.search(pagination.createSearchRequest(documentType, query),function(searchResponse) {
				createMappingFromIndex(dataSetMapping.items, searchResponse);
			});
		}else{
			$('#dataitem-table').empty();
			$('#table-papger').empty();
			var dataSetEntity = restApi.get('/api/v1/dataset/' + ns.MappingManager.prototype.getSelectedDataSet());
			showMessage('alert alert-info', 'There are not mappings for <strong>' + dataSetEntity.name + '</strong> catalogue');
		}
		
		function createMappingFromIndex(dataSets, searchResponse){
			
			var totalHitCount = searchResponse.totalHitCount;
			var displayFeatures = searchResponse.searchHits;
			var queryRules = [];
			var allFeatureCollection = [];
			var count = 0;
			var mappingPerStudy = {};
			var cachedFeatures = {};
			var involedDataSets = [];
			
			$.each(displayFeatures, function(index, hit){
				var hitInfo = hit.columnValueMap;
				queryRules.push({
					field : storeMappingFeature,
					operator : 'EQUALS',
					value : '' + hitInfo.id
				});
				queryRules.push({
					operator : 'OR'
				})
				allFeatureCollection.push(hitInfo.id);
			});
			queryRules.pop();
			queryRules.push({
				operator : 'LIMIT',
				value : 100000 
			});
			
			$.each(dataSets, function(index, dataSet){
				var tuple = {};
				var searchRequest = {
					documentType : dataSet.identifier,
					queryRules :queryRules
				};	
				searchApi.search(searchRequest, function(searchResponse) {
					var searchHits = searchResponse.searchHits;	
					if(searchHits.length > 0){
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
							num : 500,
						});
						
						var mappingConfirmMap = {};
						var boolValueIds = [];
						if(valuesObject !== undefined && valuesObject.items.length > 0){
							$.each(valuesObject.items, function(index, ov){
								boolValueIds.push(ns.hrefToId(ov.value.href));
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
								mappingConfirmMap[ns.hrefToId(ov.observationSet.href)] = restApi.get('/api/v1/boolvalue/' + ns.hrefToId(ov.value.href));
							});
						}
						
						$.each(searchHits, function(index, hit){
							var mapping = hit['columnValueMap'];
							var documentId = hit['id'];
							var featureId = mapping[storeMappingFeature];
							var storeMappedFeatureId = mapping[storeMappingMappedFeature];
							var score = mapping[scoreMappingScore];
							var absoluteScore = mapping[scoreMappingAbsoluteScore] === undefined ? null : mapping[scoreMappingAbsoluteScore];
							var observationSet = mapping['observation_set'];
							var confirmed = false;
							
							if(mappingConfirmMap[observationSet])
								confirmed = mappingConfirmMap[observationSet].value;
							if(!tuple[featureId])
								tuple[featureId] = [];
							tuple[featureId].push({
								'score' : score,
								'absoluteScore' : absoluteScore,
								'mappedFeature' : storeMappedFeatureId,
								'confirmed' : confirmed,
								'observationSet' : observationSet,
								'documentId' : documentId
							});
							
							if($.inArray(storeMappedFeatureId, allFeatureCollection) === -1) allFeatureCollection.push(storeMappedFeatureId);
						});
					}
					var listOfFeatures = restApi.get('/api/v1/observablefeature', null, {
						q : [{
							field : 'id',
							operator : 'IN',
							value : allFeatureCollection
						}],
						num : 500
					});
					$.each(listOfFeatures.items, function(index, element){
						cachedFeatures[(ns.hrefToId(element.href))] = element;
					});
					var dataSetIdArray = dataSet.identifier.split('-');
					mappingPerStudy[dataSetIdArray[1]] = sortMappings(tuple);
					count++;
					if(count === dataSets.length) renderMappingTable(mappingPerStudy, dataSets, displayFeatures, totalHitCount, cachedFeatures);
				});
			});
		}
		
		function sortMappings(tuple){
			$.each(tuple, function(index, mappings){
				if(mappings.length > 1){
					mappings.sort(function(a,b){
						return naturalSort(b.score, a.score);
					});
				}
			});
			return tuple;
		}
		
		function renderMappingTable(mappingPerStudy, dataSets, displayFeatures, totalHitCount, cachedFeatures){
			//create table header
			var involedDataSets = [];
			var selectedDataSet = restApi.get('/api/v1/dataset/' + ns.MappingManager.prototype.getSelectedDataSet());
			involedDataSets.push(selectedDataSet.name);
			$.each(dataSets, function(index, dataSet){
				var dataSetIdArray = dataSet.identifier.split('-');
				var mappedDataSet = restApi.get('/api/v1/dataset/' + dataSetIdArray[1]);
				involedDataSets.push(mappedDataSet.name);
			});
			
			var tableBody = $('<tbody />');

			$.each(displayFeatures, function(index, featureFromIndex){
				var featureId = featureFromIndex.columnValueMap.id;
				tableBody.append(createRowForMappingTable(mappingPerStudy, dataSets, featureId, cachedFeatures));
			});
			$('#dataitem-table').empty().append(createDynamicTableHeader(involedDataSets)).append(tableBody);
			
			pagination.setTotalPage(Math.ceil(totalHitCount / pagination.getPager()));
			pagination.updateMatrixPagination($('.pagination ul'), ns.MappingManager.prototype.createMatrixForDataItems);
		}
		
		function createDynamicTableHeader(involedDataSets){
			var headerRow = $('<tr />');
			var dataSetRow = $('<tr />');
			var columnWidth = 60 / (involedDataSets.length - 1);
			var firstColumn = null;
			for(var i = 0; i < involedDataSets.length; i++){
				if(i === 0){
					firstColumn = $('<th class="text-align-center">' + involedDataSets[i] + '</th>').css('width', '40%').appendTo(dataSetRow);
					if (sortRule) {
						if (sortRule.operator == 'SORTASC') {
							$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-s down float-right"></span>').appendTo(firstColumn);
						} else {
							$('<span data-value="Name" class="ui-icon ui-icon-triangle-1-n up float-right"></span>').appendTo(firstColumn);
						}
					} else {
						$('<span data-value="Name" class="ui-icon ui-icon-triangle-2-n-s updown float-right"></span>').appendTo(firstColumn);
					}
				}else{
					$('<th class="text-align-center">' + involedDataSets[i] + '</th>').css('width', columnWidth + '%').appendTo(dataSetRow);
				}
			}
			$('<th class="text-align-center">Desired cataogue</th>').width('width', '40%').appendTo(headerRow);
			$('<th class="text-align-center" colspan="' + involedDataSets.length + '">Match cataogues</th>').width('width', '60%').appendTo(headerRow);
			
			if(firstColumn !== null){
				$(firstColumn).find('.ui-icon').click(function() {
					if (sortRule && sortRule.operator == 'SORTASC') {
						sortRule = {
							value : 'name',
							operator : 'SORTDESC'
						};
					} else {
						sortRule = {
							value : 'name',
							operator : 'SORTASC'
						};
					}
					ns.MappingManager.prototype.createMatrixForDataItems();
					return false;
				});
			}
			
			return $('<thead />').append(headerRow).append(dataSetRow);
		}	
		
		function createRowForMappingTable(mappingPerStudy, dataSets, featureId, cachedFeatures){
			
			var feature = cachedFeatures[featureId];
			var row = $('<tr />');
			var description = '<strong>' + feature.name + '</strong> : ' + i18nDescription(feature).en;
			var isPopOver = description.length < 100;
			var popover = $('<span />').html(isPopOver ? description : description.substring(0, 90) + ' ...');
			if(!isPopOver){
				popover.addClass('show-popover');
				popover.popover({
					content : i18nDescription(feature).en,
					trigger : 'hover',
					placement : 'bottom'
				});
			}
			$('<td />').addClass('add-border').append(popover).appendTo(row);
			
			$.each(dataSets, function(index, dataSet){
				var mappedDataSetId = dataSet.identifier.split('-')[1];
				var mapping = mappingPerStudy[mappedDataSetId];
				var mappedFeatures = mapping[featureId];
				if(mappedFeatures){
					var displayTerm = '';
					var description = '';
					var count = 0;
					var confirmed = false;
					var selectedMappings = [];
					$.each(mappedFeatures, function(index, eachValue){
						if(eachValue.mappedFeature !== undefined){
							var mappedFeatureEntity = cachedFeatures[eachValue.mappedFeature];
							if(count === 0){
								displayTerm = mappedFeatureEntity.name;
								description = mappedFeatureEntity.description;
							}
							if(eachValue.confirmed === true){
								selectedMappings.push(mappedFeatureEntity.name);
								confirmed = true;
							}
							count++;
						}
					});
					displayTerm = selectedMappings.length > 0 ? selectedMappings.join(' , ') : displayTerm;
					var removeIcon = $('<i />').addClass('icon-trash show-popover').css({
						position : 'relative',
						float : 'right'
					}).click(function(){
						standardModal.createModalCallback('Confirmation', function(modal){
							var confirmButton = $('<button type="btn" class="btn btn-primary">Confirm</button>').click(function(){
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
									url : ns.getContextURL() + '/mappingmanager/delete',
									async : false,
									data : JSON.stringify(deleteRequest),
									contentType : 'application/json',
								});
								modal.remove();
								ns.MappingManager.prototype.createMatrixForDataItems();
							});
							modal.css({
								'margin-top' : 200
							});
							modal.find('div.modal-body:eq(0)').append('<p style="font-size:16px"><strong>Are you sure that you want to remove candidate mappings?</strong></p>');
							modal.find('div.modal-footer:eq(0)').prepend(confirmButton);
						});
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
							createMappingTable(feature, mappedFeatures, restApi.get('/api/v1/dataset/' + mappedDataSetId), modal);
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
			});
			return row;
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
				observedValueIds.push(ns.hrefToId(ov.href));
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
			var header = $('<tr><th>Name</th><th>Description</th><th>Score</th><th>Select</th></tr>');
			mappingTable.append(header);
			
			var absoluteScores = [];
			var scores = [];
			$.each(mappedFeatures, function(index, eachMapping){
				scores.push(eachMapping.score);
				absoluteScores.push(eachMapping.absoluteScore);
			});
			var highScoresIndex = [];
//			$.each(jstat.pnorm(scores), function(index, pValue){
//				if(pValue >= 0.90){
//					highScoresIndex.push(index);
//				}
//			});
			if(scores.length > 2){
				var classifications = ss.jenks(scores, 2);
				var naturalBreak = classifications[1];
				$.each(scores, function(index, score){
					if(score > naturalBreak){
						highScoresIndex.push(index);
					}
				});
			}
			else{
				$.each(scores, function(index, score){
					highScoresIndex.push(index);
				});
			}
			
			$.each(mappedFeatures, function(index, eachMapping){
				var mappedFeatureId = eachMapping.mappedFeature;
				var score = eachMapping.score;
				var mappedFeature = restApi.get('/api/v1/observablefeature/' + mappedFeatureId);
				if(mappedFeature){
					var row = $('<tr />');
					var checkBox = $('<input type="checkbox">');
					if(eachMapping.confirmed){
						checkBox.attr('checked', true);
						selectedFeatures.push(mappedFeature.name);
					}
					eachMapping.mappedFeature = mappedFeature;
					checkBox.data('eachMapping', eachMapping);
					row.append('<td>' + mappedFeature.name + '</td><td>' + i18nDescription(mappedFeature).en + '</td><td>' + score + '</td>');
					row.append($('<td />').append($('<label class="checkbox"></label>').append(checkBox))).appendTo(mappingTable);
				}
				if($.inArray(index, highScoresIndex) !== -1){
					if(absoluteScores[index] >= 85) row.addClass('success');
					else row.addClass('info');
				}else{
					row.addClass('warning');
				}
			});
			
			tableDiv.append(mappingTable);
			var body = modal.find('.modal-body:eq(0)').css({
				'min-height' : 200,
				'overflow' : 'auto'
			});
			$('<div />').append('<div class="span4"></div>').append(tableDiv).appendTo(body);
			
			var infoDiv = $('<div />').addClass('span4').css({
				'position' : 'absolute',
				'margin-left' : 0,
				'margin-top' : 25,
				'z-index' : 10000
			});
			var dataSet = restApi.get('/api/v1/dataset/' + ns.MappingManager.prototype.getSelectedDataSet());
			$('<div />').append('<h4>' + dataSet.name + '</h4>').appendTo(infoDiv);
			$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.name + '</span>').appendTo(infoDiv);
			$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + i18nDescription(feature).en + '</span>').appendTo(infoDiv);
			var selectedMappings = $('<div />').append('<span class="info"><strong>Selected mappings : </strong></span>').append('<span>' + selectedFeatures.join(' , ') + '</span>').appendTo(infoDiv);
			infoDiv.append('<br /><br />');
			var header = modal.find('.modal-header:eq(0)');
			header.append(infoDiv);
			
			var confirmButton = $('<button class="btn btn-primary">Confirm</button>');
			confirmButton.click(function(){
				updateMappingInfo(feature, mappingTable, mappedDataSet, ns.MappingManager.prototype.createMatrixForDataItems);
				standardModal.closeModal();
			});
			
			var footer = modal.find('.modal-footer:eq(0)');
			footer.prepend(confirmButton);
			
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
							value : ns.hrefToId(confirmFeature.items[0].href)
						}]
					});
					var xrefValue = restApi.get('/api/v1/boolvalue/' + ns.hrefToId(observedValue.items[0].value.href));
					xrefValue.value = changedValue;
					updateEntity(xrefValue.href, xrefValue);
					
					var updateRequest = {
						'documentType' : selectedDataSet + '-' + ns.hrefToId(mappedDataSet.href),
						'documentIds' : [eachMapping.documentId],
						'updateScript' : storeMappingConfirmMapping + '=true',
					};
					$.ajax({
						type : 'POST',
						url : ns.getContextURL() + '/mappingmanager/update',
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
			$('#alert-message').empty();
			var messageAlert = $('<div />').addClass(alertClass).append('<button type="button" class="close" data-dismiss="alert">&times;</button>');
			$('<span><strong>Message : </strong>' + message + '</span>').appendTo(messageAlert);
			messageAlert.appendTo('#alert-message');
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
					value : ns.hrefToId(dataSet.href)
				}],
				num : batchSize
			});
			var observationIds = [];
			if(observationSets.items.length > 0){
				var map = {};
				
				$.each(observationSets.items, function(index, observation){
					observationIds.push(ns.hrefToId(observation.href));
				});
			}
			var iteration = Math.ceil(observationSets.total / batchSize);
			for(var i = 1; i < iteration; i++){
				var query = {
						q : [{
							field : 'partOfDataSet',
							operator : 'EQUALS',
							value : ns.hrefToId(dataSet.href)
						}],
						num : batchSize,
						start : i * batchSize
				};
				observationSets = restApi.get('/api/v1/observationset/', null, query);
				$.each(observationSets.items, function(index, observation){
					observationIds.push(ns.hrefToId(observation.href));
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
						if(!tuple[ns.hrefToId(dataItem.value.href)])
							tuple[ns.hrefToId(dataItem.value.href)] = [];
						tuple[ns.hrefToId(dataItem.value.href)].push({
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
					valueType.xref.push(ns.hrefToId(observedValue.value.href));
				else if(featureIdentifier === storeMappingConfirmMapping)
					valueType.bool.push(ns.hrefToId(observedValue.value.href));
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
					data[storeMappingFeature] = getDataValueId(observedValue.feature, ns.hrefToId(observedValue.value.href));
				}else if(observedValue.feature.identifier === storeMappingMappedFeature){
					data[storeMappingMappedFeature] = getDataValueId(observedValue.feature, ns.hrefToId(observedValue.value.href));
				}else if(observedValue.feature.identifier === storeMappingConfirmMapping){
					data[storeMappingConfirmMapping] = getDataValueId(observedValue.feature, ns.hrefToId(observedValue.value.href));
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
	};
	
	ns.MappingManager.prototype.initAccordion = function(){
		$('#accordion-action-content').show();
		$('#accordion-icon-meaning-content').hide();
		$('.accordion ul li').removeClass('active');
		$('.accordion ul li:eq(0)').addClass('active');
		
		$('#accordion-action-click').click(function(){
			$('.accordion ul li').removeClass('active');
			$(this).addClass('active');
			$('#accordion-icon-meaning-content').hide();
			$('#accordion-action-content').show();
		});
		$('#accordion-icon-meaning-click').click(function(){
			$('.accordion ul li').removeClass('active');
			$(this).addClass('active');
			$('#accordion-icon-meaning-content').show();
			$('#accordion-action-content').hide();
		});
	};
	
	ns.MappingManager.prototype.downloadMappings = function(){
		var dataSet = restApi.get('/api/v1/dataset/' + selectedDataSet);
		var deleteRequest = {
			'dataSetId' : selectedDataSet,
			'documentType' : dataSet.identifier
		};
		$.download(ns.getContextURL() + '/mappingmanager/download',{request : JSON.stringify(deleteRequest)});
	};
	
	ns.MappingManager.prototype.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	ns.MappingManager.prototype.getSelectedDataSet = function (){
		return selectedDataSet;
	}
	
}($, window.top));