(function($, molgenis, w) {
	"use strict";
	
	var ontologyAnnotator = new molgenis.OntologyAnnotator();
	var pagination = new molgenis.Pagination();
	var standardModal = new molgenis.StandardModal();
	var restApi = new molgenis.RestClient();
	var searchApi = new molgenis.SearchClient();
	var selectedDataSet = null;
	var userName = null;
	var biobankDataSets = null;
	var sortRule = null;
	var previousSearchText = null;
	var storeMappingFeature = 'store_mapping_feature';
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var storeMappingConfirmMapping = 'store_mapping_confirm_mapping';
	var scoreMappingScore = "store_mapping_score";
	
	molgenis.MappingManager = function MappingManager(){
		
	};
	
	molgenis.MappingManager.prototype.changeDataSet = function(userName, selectedDataSet, dataSetIds){
		if(selectedDataSet !== '' && dataSetIds.length > 0){
			setUserName(userName); 
			var dataSetEntity = restApi.get('/api/v1/dataset/' + selectedDataSet);
			biobankDataSets = restApi.get('/api/v1/dataset/', null, {
				q : [{
					field : 'id',
					operator : 'IN',
					value : dataSetIds
				}],
			}).items;
			var request = {
				documentType : 'protocolTree-' + molgenis.hrefToId(dataSetEntity.href),
				query:{
					rules :[[{
						field : 'type',
						operator : 'EQUALS',
						value : 'observablefeature'
					}]]
				}
			};
			searchApi.search(request, function(searchResponse){
				sortRule = null;
				$('#dataitem-number').empty().append(searchResponse.totalHitCount);
				pagination.reset();
				molgenis.MappingManager.prototype.updateSelectedDataset(selectedDataSet);
				molgenis.MappingManager.prototype.createMatrixForDataItems();
				initSearchDataItems(dataSetEntity);
			});
		}else{
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function initSearchDataItems (dataSet) {
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					molgenis.dataItemsTypeahead('observablefeature', molgenis.hrefToId(dataSet.href), query, process);
				},
				minLength : 3,
				items : 20
			});
			$('#search-button').click(function(){
				molgenis.MappingManager.prototype.createMatrixForDataItems();
				previousSearchText = $('#search-dataitem').val();
			});
			$('#search-dataitem').on('keydown', function(e){
			    if (e.which == 13) {
			    	previousSearchText = $(this).val();
			    	$('#search-button').click();
			    	return false;
			    }
			});
			$('#search-dataitem').on('keyup', function(e){
				if($(this).val() === '' && previousSearchText !== null){
					previousSearchText = null;
					molgenis.MappingManager.prototype.createMatrixForDataItems();
			    }
			});
		}
	};
	
	molgenis.MappingManager.prototype.createMatrixForDataItems = function() {
		var dataSetMapping = getDataSetsForMapping();
		if(dataSetMapping.items.length > 0){
			var documentType = 'protocolTree-' + dataSetMapping.items[0].Identifier.split('-')[1];
			var q = {
					rules : [[{
						operator : 'SEARCH',
						value : 'observablefeature'
					}]]
			}
			var queryText = $('#search-dataitem').val();
			if(queryText !== ''){
				q.rules[0].push({
					operator : 'AND'
				});
				q.rules[0].push({
					operator : 'SEARCH',
					value : queryText
				});
				pagination.reset();
			}
			if(sortRule !== null)
			{
				q.sort = sortRule;
			}
			
			searchApi.search(pagination.createSearchRequest(documentType, q),function(searchResponse) {
				createMappingFromIndex(dataSetMapping.items, searchResponse, function(tableBody, involedDataSets){
					$('#dataitem-table').empty().append(createDynamicTableHeader(involedDataSets)).append(tableBody);
					pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()));
					pagination.updateMatrixPagination($('.pagination ul'), molgenis.MappingManager.prototype.createMatrixForDataItems);
				});
			});
		}else{
			$('#dataitem-table').empty();
			$('#table-papger').empty();
			var dataSetEntity = restApi.get('/api/v1/dataset/' + molgenis.MappingManager.prototype.getSelectedDataSet());
			molgenis.showMessage('alert alert-info', 'There are not mappings for <strong>' + dataSetEntity.Name + '</strong> catalogue', $('form:eq(-1)'));
		}
		
		function getDataSetsForMapping(){
			var identifiers = [];
			$.each(biobankDataSets, function(index, dataSet){
				var dataSetId = molgenis.hrefToId(dataSet.href);
				if(dataSetId !== molgenis.MappingManager.prototype.getSelectedDataSet())
				identifiers.push(getUserName() + '-' + molgenis.MappingManager.prototype.getSelectedDataSet() + '-' + dataSetId); 
			});
			var request = {
				'q' : [{
					'field' : 'Identifier',
					'operator' : 'IN',
					'value' : identifiers
				}]
			};
			var dataSetMapping = restApi.get('/api/v1/dataset/',{ 'q' : request });
			return dataSetMapping;
		}
		
		function createMappingFromIndex(dataSets, searchResponse, callback){
			var allFeatureCollection = [];
			var count = 0;
			var mappingPerStudy = {};
			var cachedFeatures = {};
			
			var displayFeatures = searchResponse.searchHits;
			var queryRules = [];
			$.each(displayFeatures, function(index, hit){
				var hitInfo = hit.columnValueMap;
				if(queryRules.length > 0){
					queryRules.push({
						'operator' : 'OR'
					});
				}
				queryRules.push({
					'field' : storeMappingFeature,
					'operator' : 'EQUALS',
					'value' : hitInfo.id.toString()
				});
				allFeatureCollection.push(hitInfo.id);
			});
			
			$.each(dataSets, function(index, dataSet){
				var tuple = {};
				var searchRequest = {
					'documentType' : dataSet.Identifier,
					'query' : {
						'pageSize': 10000,
						'rules' : [queryRules]
					}
				};	
				searchApi.search(searchRequest, function(searchResponse) {
					var searchHits = searchResponse.searchHits;	
					if(searchHits.length > 0){
						$.each(searchHits, function(index, hit){
							var mapping = hit.columnValueMap;
							var featureId = mapping[storeMappingFeature];
							var storeMappedFeatureId = mapping[storeMappingMappedFeature];
							
							if(!tuple[featureId]) tuple[featureId] = [];
							tuple[featureId].push({
								'score' : mapping[scoreMappingScore],
								'mappedFeatureId' : storeMappedFeatureId,
								'confirmed' : mapping[storeMappingConfirmMapping],
								'observationSet' : mapping.observation_set,
								'documentId' : hit.id
							});
							
							if($.inArray(storeMappedFeatureId, allFeatureCollection) === -1) allFeatureCollection.push(storeMappedFeatureId);
						});
					}
					var dataSetIdArray = dataSet.Identifier.split('-');
					mappingPerStudy[dataSetIdArray[2]] = sortMappings(tuple);
					count++;
					
					if(count === dataSets.length) {
						preloadEntities(allFeatureCollection, cachedFeatures);
						renderMappingTable(mappingPerStudy, dataSets, displayFeatures, cachedFeatures, callback);
					}
				});
			});
		}
		
		function preloadEntities(allFeatureCollection, cachedFeatures){
			var iterations = Math.ceil(allFeatureCollection.length/500) + 1;
			for(var i = 1; i < iterations; i++){
				var lower = (i - 1) * 500;
				var upper = (i * 500) < allFeatureCollection.length ? (i * 500) : allFeatureCollection.length; 
				var query = {
					'q' : [{
						'field' : 'id',
						'operator' : 'IN',
						'value' : allFeatureCollection.slice(lower, upper)
					}],
					'num' : 500
				};
				var listOfFeatures = restApi.get('/api/v1/observablefeature', {q : query});
				$.each(listOfFeatures.items, function(index, element){
					cachedFeatures[(molgenis.hrefToId(element.href))] = element;
				});
			}
		}
		
		function sortMappings(tuple){
			$.each(tuple, function(index, mappings){
				if(mappings.length > 1){
					mappings.sort(function(a,b){
						return molgenis.naturalSort(b.score, a.score);
					});
				}
			});
			return tuple;
		}
		
		function renderMappingTable(mappingPerStudy, mappingDataSets, displayFeatures, cachedFeatures, callback){
			//create table header
			var involvedDataSetIds = [];
			var involvedDataSetNames = [];
			$.each(mappingDataSets, function(index, dataSet){
				if(dataSet !== undefined && dataSet !== null){
					var dataSetIdArray = dataSet.Identifier.split('-');
					involvedDataSetIds.push(dataSetIdArray[2]);
				}
			});
			involvedDataSetIds.splice(0, 0, molgenis.MappingManager.prototype.getSelectedDataSet());
			biobankDataSets = sortOrderOfDataSets(biobankDataSets, involvedDataSetIds);
			var removeDataSetIndex = [];
			$.each(biobankDataSets, function(index, dataSet){
				if($.inArray(molgenis.hrefToId(dataSet.href), involvedDataSetIds) !== -1){
					involvedDataSetNames.push(dataSet.Name);
				}else{
					removeDataSetIndex.push(index);
				}
			});
			$.each(removeDataSetIndex, function(index, number){
				biobankDataSets.splice(number, 1);
			});
			var tableBody = $('<tbody />');
			$.each(displayFeatures, function(index, featureFromIndex){
				var featureId = featureFromIndex.columnValueMap.id;
				tableBody.append(createRowForMappingTable(mappingPerStudy, mappingDataSets, featureId, cachedFeatures));
			});
			callback(tableBody, involvedDataSetNames);
			
			function sortOrderOfDataSets(biobankDataSets, involvedDataSetIds){
				var sortedDataSets = [];
				$.each(involvedDataSetIds, function(index, dataSetId){
					for(var i = 0; i < biobankDataSets.length; i++){
						var currentDataSet = biobankDataSets[i];
						if(molgenis.hrefToId(currentDataSet.href) === dataSetId){
							sortedDataSets.splice(index, 0, currentDataSet);
							break;
						}
					}
				});
				return sortedDataSets;
			}
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
						if (sortRule.orders[0].direction == 'ASC') {
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
					if (sortRule && sortRule.orders[0].direction == 'ASC') {
						sortRule = {
								orders: [{
									property: 'name',
									direction: 'DESC'
								}]
						};
					} else {
						sortRule = {
								orders: [{
									property: 'name',
									direction: 'ASC'
								}]
						};
					}
					molgenis.MappingManager.prototype.createMatrixForDataItems();
					return false;
				});
			}
			return $('<thead />').append(headerRow).append(dataSetRow);
		}	
		
		function createRowForMappingTable(mappingPerStudy, dataSets, featureId, cachedFeatures){
			var feature = cachedFeatures[featureId];
			var row = $('<tr />');
			var description = '<strong>' + feature.Name + '</strong> : ' + molgenis.i18nDescription(feature).en;
			var isPopOver = description.length < 90;
			var popover = $('<span />').html(isPopOver ? description : description.substring(0, 90) + ' ...');
			if(!isPopOver){
				popover.addClass('show-popover');
				popover.popover({
					content : molgenis.i18nDescription(feature).en,
					trigger : 'hover',
					placement : 'bottom'
				});
			}
			$('<td />').addClass('add-border show-popover').append(popover).appendTo(row).click(function(){
				var row = $(this).parents('tr:eq(0)');
				if(!$('body').data('clickedRow')) $('body').data('clickedRow', {});
				var storedRowInfo = $('body').data('clickedRow');
				storedRowInfo[featureId] = row;
				$('body').data('table', row.parents('table:eq(0)'));
				createAnnotationModal(feature);
			});
			
			$.each(dataSets, function(index, dataSet){
				var mappedDataSetId = dataSet.Identifier.split('-')[2];
				var mapping = mappingPerStudy[mappedDataSetId];
				var mappedFeatures = mapping[featureId];
				if(mappedFeatures){
					var displayTerm = '';
					var description = '';
					var count = 0;
					var confirmed = false;
					var selectedMappings = [];
					$.each(mappedFeatures, function(index, eachValue){
						if(eachValue.mappedFeatureId !== undefined){
							var mappedFeatureEntity = cachedFeatures[eachValue.mappedFeatureId];
							if(count === 0){
								displayTerm = mappedFeatureEntity.Name;
								description = mappedFeatureEntity.description;
							}
							if(eachValue.confirmed === true){
								selectedMappings.push(mappedFeatureEntity.Name);
								confirmed = true;
							}
							count++;
						}
					});
					displayTerm = selectedMappings.length > 0 ? selectedMappings.join(' , ') : displayTerm;
					var displayTermSpan = $('<span />').addClass('show-popover').append(displayTerm).click(function(){
						editIcon.click();
					});
					
					var removeIcon = $('<i />').addClass('icon-trash show-popover').css({
						position : 'relative',
						'float':'right'
					}).click(function(){
						standardModal.createModalCallback('Confirmation', function(modal){
							var confirmButton = $('<button type="btn" class="btn btn-primary">Confirm</button>').click(function(){
								var documentIds = [];
								$.each(mappedFeatures, function(index, element){
									documentIds.push(element.documentId);
								});
								removeAnnotation(mappedFeatures);
								var deleteRequest = {
									'documentType' : dataSet.Identifier,
									'documentIds' : documentIds
								};
								$.ajax({
									type : 'POST',
									url : molgenis.getContextUrl().replace('/biobankconnect', '') + '/mappingmanager/delete',
									async : false,
									data : JSON.stringify(deleteRequest),
									contentType : 'application/json',
								});
								modal.remove();
								molgenis.MappingManager.prototype.createMatrixForDataItems();
							});
							var table = row.parents('table:eq(0)');
							modal.find('div.modal-body:eq(0)').append('<p style="font-size:16px"><strong>Are you sure that you want to remove candidate mappings?</strong></p>');
							modal.find('div.modal-footer:eq(0)').prepend(confirmButton);
							modal.css({
								'width' : 600,
								'left' : table.width()/2 - 300,
								'margin-left' : 0,
								'margin-top' : 0,
								'top' : 200
							}).modal('show');
						});
					});
					
					var editIcon = $('<i />').addClass('show-popover ' + (confirmed ? 'icon-ok' : 'icon-pencil'));
					editIcon.css({
						position : 'relative',
						'float' : 'right'
					}).click(function(){
						standardModal.createModalCallback('Candidate mappings', function(modal){
							createMappingTable(feature, mappedFeatures, restApi.get('/api/v1/dataset/' + mappedDataSetId), modal, editIcon);
							var table = row.parents('table:eq(0)');
							modal.find('div.modal-body:eq(0)').css('max-height' , 300);
							modal.css({
								'width' : 950,
								'left' : table.width() / 2 - 400,
								'margin-left' : 0,
								'margin-top' : 0,
								'top' : 200
							}).modal('show');
						});
					});
					
					$('<td />').addClass('add-border').append(displayTermSpan).append(removeIcon).append(editIcon).appendTo(row);
				}else{
					$('<td />').addClass('add-border').append('<i class="icon-ban-circle show-popover" title="Not available"></i>').appendTo(row);
				}
			});
			return row;
		}
		
		function createAnnotationModal(feature){
			var title = 'Rematch research variable : ';
			restApi.getAsync(feature.href, {'expand': ["unit", "definitions"]}, function(updatedFeature){
				var modal = ontologyAnnotator.createFeatureTable(title, updatedFeature, createAnnotationModal);
				var body = modal.find('div.modal-body:eq(0)').addClass('overflow-y-visible');
				var footer = modal.find('div.modal-footer:eq(0)');
				var nextButton = $('<button type="btn" class="btn btn-primary">Next</button>').click(function(){
					createRematchingModal(updatedFeature);
				});
				footer.prepend(nextButton);
				var table = $('body table:eq(0)');
				modal.css({
					'width' : 800,
					'left' : table.width()/2 - 300,
					'margin-left' : 0,
					'margin-top' : 0,
					'top' : 200
				}).modal('show');
			});
		}
		
		function createRematchingModal(feature){
			standardModal.createModalCallback('Rematch research variable : ' + feature.Name, function(modal){
				var body = modal.find('div.modal-body:eq(0)');
				var divControlPanel = $('<div />').addClass('row-fluid').appendTo(body);
				var selectTag = $('<select />');
				$.each(biobankDataSets, function(index, dataSet){
					var dataSetId = molgenis.hrefToId(dataSet.href);
					if(dataSetId !== molgenis.MappingManager.prototype.getSelectedDataSet()){
						selectTag.append('<option value="' + dataSetId + '">' + dataSet.Name + '</option>');
					}
				});
				var selectButton = $('<button type="btn" class="btn btn-info">Select</button>');
				var selectAllButton = $('<button type="btn" class="btn btn-primary">Select all</button>');
				var removeAllButton = $('<button type="btn" class="btn btn">Remove all</button>');
				
				$('<div />').addClass('offset1 span3').append(selectTag).appendTo(divControlPanel);
				$('<div />').addClass('offset1 span5 btn-group').append(selectButton).append(selectAllButton).append(removeAllButton).appendTo(divControlPanel);
				
				var infoContainer = $('<div />').addClass('row-fluid').appendTo(body);
				var dataSetsContainer = $('<div />').addClass('offset1 span10 well').appendTo(infoContainer);
				$('<div />').addClass('span12').append('<legend class="legend-small">Selected catalogues : </legend>').appendTo(dataSetsContainer);
				
				var selectedOptions = [];
				$(selectTag).find('option').each(function(){
					selectedOptions.push($(this).val());
				});
				listOfOptions(dataSetsContainer, selectedOptions)
				
				var footer = modal.find('div.modal-footer:eq(0)');
				var matchButton = $('<button type="btn" class="btn btn-primary">Rematch</button>')
				footer.prepend(matchButton);
				modal.modal('show');
				
				selectButton.click(function(){
					var selectedDataSetId = selectTag.val();
					if(selectedDataSetId !== null && selectedDataSetId !== undefined){
						if($.inArray(selectedDataSetId, selectedOptions) === -1){
							selectedOptions.push(selectedDataSetId);
							switchOptions(selectedDataSetId, $(selectTag));
							listOfOptions(dataSetsContainer, selectedOptions)
						}
					}
				})
				
				selectAllButton.click(function(){
					$(selectTag).find('option').each(function(){
						if($.inArray($(this).val(), selectedOptions) === -1){
							selectedOptions.push($(this).val());
						}
					});
					listOfOptions(dataSetsContainer, selectedOptions)
				});
				
				removeAllButton.click(function(){
					selectedOptions = [];
					listOfOptions(dataSetsContainer, selectedOptions)
				});
				
				matchButton.click(function(){
					var request = {
						'sourceDataSetId' : molgenis.MappingManager.prototype.getSelectedDataSet(),
						'featureId' : molgenis.hrefToId(feature.href),
						'selectedDataSetIds' : selectedOptions
					};
					$.ajax({
						type : 'POST',
						url : molgenis.getContextUrl() + '/rematch',
						async : false,
						data : JSON.stringify(request),
						contentType : 'application/json',
						success : function(data, textStatus, request) {
							modal.modal('hide');
							var storedRowInfo = $('body').data('clickedRow');
							var existingRow = storedRowInfo[molgenis.hrefToId(feature.href)];
							var biobankDataSetIds = [];
							$.each(biobankDataSets, function(index, dataSet){
								biobankDataSetIds.push(molgenis.hrefToId(dataSet.href));
							});
							$.each(selectedOptions, function(index, dataSetId){
								var index = $.inArray(parseInt(dataSetId), biobankDataSetIds);
								if(index < 0) index = $.inArray(dataSetId, biobankDataSetIds);
								if(index !== -1){
									var spinner = $('<img src="/img/waiting-spinner.gif">').css('height', '15px');
									existingRow.find('td:eq(' + index + ')').empty().append(spinner).css('text-align', 'center');
								}
							});
							checkProgressForRematch(feature);
						},
						error : function(request, textStatus, error){
							console.log(error);
						} 
					});
				});
			});
			
			function checkProgressForRematch(feature){
				$.ajax({
					type : 'GET',
					url : molgenis.getContextUrl() + '/match/status',
					async : false,
					contentType : 'application/json',
					success : function(data, textStatus, request) {
						if(data.isRunning){
							setTimeout(function(){
								checkProgressForRematch(feature);
							}, 1000);
						}else{
							setTimeout(function(){
								replaceMappingInTable(feature);
							}, 1500);
						}
					},
					error : function(request, textStatus, error){
						console.log(error);
					} 
				});
			}
			
			function replaceMappingInTable(feature){
				var searchRequest = {
					'documentType' : 'protocolTree-' + molgenis.MappingManager.prototype.getSelectedDataSet(),
					'query' : {
						'rules':[[{
							'field' : 'id',
							'operator' : 'EQUALS',
							'value' : molgenis.hrefToId(feature.href).toString()
						}]]
					}
				}
				searchApi.search(searchRequest, function(searchResponse){
					var storedRowInfo = $('body').data('clickedRow');
					var existingRow = storedRowInfo[molgenis.hrefToId(feature.href)];
					var table = $('body').data('table');
					var index = $.inArray(existingRow[0], table.find('tr'));
					var dataSetMapping = getDataSetsForMapping();
					if(dataSetMapping.items.length > 0){
						createMappingFromIndex(dataSetMapping.items, searchResponse, function(tableBody, involedDataSets){
							var newMappings = tableBody.find('tr');
							if(newMappings.length > 0){
								if(index > 0){
									var prevRow = table.find('tr:eq(' + --index + ')');
									existingRow.remove();
									prevRow.after(newMappings[0]);
								}
							}else{
								existingRow.find('td').each(function(){
									$(this).empty().append('<i class="icon-ban-circle show-popover" title="Not available"></i>');
								});
							}
						});
					}
					delete storedRowInfo[molgenis.hrefToId(feature.href)];
				});
			}
			
			function listOfOptions(dataSetsContainer, selectedOptions){
				dataSetsContainer.find('div:gt(0)').remove();
				$.each(selectedOptions, function(index, dataSetId){
					var dataSet = restApi.get('/api/v1/dataset/' + dataSetId);
					var newRow = $('<div />').addClass('row-fluid');
					$('<div />').addClass('offset2 span3').append(dataSet.Name).appendTo(newRow);
					var removeButton = $('<button type="btn" class="btn btn-link">Remove</button>');
					$('<div />').addClass('offset3 span2').append(removeButton).appendTo(newRow);
					dataSetsContainer.append(newRow);
					removeButton.click(function(){
						selectedOptions.splice($.inArray(dataSetId, selectedOptions), 1);
						newRow.remove();
					});
				});
			}
			
			function switchOptions(targetDataSetId, selectedOptions){
				var index = 0;
				var options = $(selectedOptions).find('option');
				optiomolgenis.attr('selected', false).each(function(){
					if(targetDataSetId !== $(this).val()){
						index++;
					}else return false;
				});
				index = index === optiomolgenis.length - 1 ? 0 : index + 1;
				$(options[index]).attr('selected', true);
			}
		}
		
		function removeAnnotation(mappings){
			var observationSetIds = [];
			$.each(mappings, function(index, eachMapping){
				observationSetIds.push(eachMapping.observationSet);
			});
			var observationSets = restApi.get('/api/v1/observationset', {
				'q' : {
					'q' : [{
						'field' : 'id',
						'operator' : 'IN',
						'value' : observationSetIds
					}]
				}
			});
			var observationSetIdentifiers = [];
			$.each(observationSets.items, function(index, observationSet){
				observationSetIdentifiers.push(observationSet.Identifier);
			});
			molgenis.showMessage('alert alert-info', observationSetIds.length + ' candidate mappings are being deleted!', $('form:eq(-1)'));
			var observedValues = restApi.get('/api/v1/observedvalue', {
				'q' : {
					'q' : [{
						'field' : 'observationSet',
						'operator' : 'IN',
						'value' : observationSetIdentifiers
					}],
					'num' : 500
				}
			});
			
			var observedValueIds = [];
			$.each(observedValues.items, function(index, ov){
				observedValueIds.push(molgenis.hrefToId(ov.href));
			});
			deleteEntity('/api/v1/observedvalue/', observedValueIds, function(){deleteEntity('/api/v1/observationset/', observationSetIds, null);});
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
		
		function createMappingTable(feature, mappedFeatures, mappedDataSet, modal, clickedCell){
			var selectedFeatures = [];
			var tableDiv = $('<div />').addClass('span7').css('margin-right', -100);
			$('<div />').append('<h4>' + mappedDataSet.name + '</h4>').appendTo(tableDiv);
			var mappingTable = $('<table />').addClass('table table-bordered'); 
			var header = $('<tr><th>Name</th><th>Description</th><th>Score</th><th>Select</th></tr>');
			mappingTable.append(header);
			
			var scores = [];
			$.each(mappedFeatures, function(index, eachMapping){
				scores.push(eachMapping.score);
			});
			var highScoresIndex = [];
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
			
			var soretedMappedFeatures = sortByScoreAndLength(mappedFeatures);
			$.each(soretedMappedFeatures, function(index, eachMapping){
				var mappedFeatureId = eachMapping.mappedFeatureId;
				var score = eachMapping.score;
				var mappedFeature = restApi.get('/api/v1/observablefeature/' + mappedFeatureId);
				if(mappedFeature){
					var row = $('<tr />');
					var checkBox = $('<input type="checkbox">');
					if(eachMapping.confirmed){
						checkBox.attr('checked', true);
						selectedFeatures.push(mappedFeature.Name);
					}
					eachMapping.mappedFeature = mappedFeature;
					checkBox.data('eachMapping', eachMapping);
					row.append('<td>' + mappedFeature.Name + '</td><td>' + molgenis.i18nDescription(mappedFeature).en + '</td><td>' + score + '</td>');
					row.append($('<td />').append($('<label class="checkbox"></label>').append(checkBox))).appendTo(mappingTable);
				}
				if($.inArray(index, highScoresIndex) !== -1){
					row.addClass('info');
				}else{
					row.addClass('warning');
				}
			});
			
			tableDiv.append(mappingTable);
			var body = modal.find('.modal-body:eq(0)').css({
				'min-height' : 200
			}).addClass('overflow-y-auto');
			$('<div />').append('<div class="span4"></div>').append(tableDiv).appendTo(body);
			
			var infoDiv = $('<div />').addClass('span4').css({
				'position' : 'absolute',
				'margin-left' : 0,
				'margin-top' : 25,
				'z-index' : 10000
			});
			var dataSet = restApi.get('/api/v1/dataset/' + molgenis.MappingManager.prototype.getSelectedDataSet());
			$('<div />').append('<h4>' + dataSet.Name + '</h4>').appendTo(infoDiv);
			$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.Name + '</span>').appendTo(infoDiv);
			$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + molgenis.i18nDescription(feature).en + '</span>').appendTo(infoDiv);
			var selectedMappings = $('<div />').append('<span class="info"><strong>Selected mappings : </strong></span>').append('<span>' + selectedFeatures.join(' , ') + '</span>').appendTo(infoDiv);
			infoDiv.append('<br /><br />');
			var header = modal.find('.modal-header:eq(0)');
			header.append(infoDiv);
			
			var confirmButton = $('<button class="btn btn-primary">Confirm</button>');
			confirmButton.click(function(){
				updateMappingInfo(feature, mappingTable, mappedDataSet, clickedCell);
				standardModal.closeModal();
			});
			
			var footer = modal.find('.modal-footer:eq(0)');
			footer.prepend(confirmButton);
			
			tableDiv.find('input[type="checkbox"]').click(function(){
				var dataItems = [];
				tableDiv.find('input:checked').each(function(index, checkbox){
					dataItems.push($(checkbox).data('eachMapping').mappedFeature.Name);
				});
				selectedMappings.find('span:eq(1)').html(dataItems.join(' , '));
			});
		}
		
		function sortByScoreAndLength(mappedFeatures){
			var total = mappedFeatures.length;
			var subSetMappings = mappedFeatures.slice(0, total < 10 ? total : 10);
			var map = {};
			$.each(subSetMappings, function(index, eachMapping){
				var mappedFeatureId = eachMapping.mappedFeatureId;
				var score = eachMapping.score;
				var mappedFeature = restApi.get('/api/v1/observablefeature/' + mappedFeatureId);
				eachMapping.comparedScore = score / mappedFeature.description.length;
				if(!map[score]){
					map[score] = [];
				}
				map[score].push(eachMapping);
			});
			var topTenOrder = [];
			$.each(map, function(score, mappings){
				if(mappings.length > 1 && score > 0){
					mappings.sort(function(a,b){
						return molgenis.naturalSort(b.comparedScore, a.comparedScore);
					});
				}
				topTenOrder = topTenOrder.concat(mappings);
			});
			if(total > 10) topTenOrder = topTenOrder.concat(mappedFeatures.slice(10, total));
			return topTenOrder;
		}
		
		function updateMappingInfo(feature, mappingTable, mappedDataSet, clickedCell){
			var displayedFeatures = [];
			mappingTable.find('input').each(function(index, checkBox){
				var eachMapping = $(checkBox).data('eachMapping');
				var changedValue = null;
				if(checkBox.checked){
					displayedFeatures.push(eachMapping.mappedFeature.Name);
				}
				if(checkBox.checked && !eachMapping.confirmed){
					changedValue = true;
				}
				if(!checkBox.checked && eachMapping.confirmed){
					changedValue = false;
				}
				if(changedValue !== null){
					eachMapping.confirmed = changedValue;
					var observationSet = restApi.get('/api/v1/observationset/' + eachMapping.observationSet);
					var observedValue = restApi.get('/api/v1/observedvalue', {
						'expand': ['Value'],
						'q' : {
							'q' : [{
								'field' : 'observationSet',
								'operator' : 'EQUALS',
								'value' : observationSet.Identifier
							},{
								'field' : 'feature',
								'operator' : 'EQUALS',
								'value' : storeMappingConfirmMapping
							}]
						}
					});
					var xrefValue = restApi.get('/api/v1/boolvalue/' + molgenis.hrefToId(observedValue.items[0].Value.href));
					xrefValue.value = changedValue;
					updateEntity(xrefValue.href, xrefValue);
					var updateRequest = {
						'documentType' : getUserName() + '-' + selectedDataSet + '-' + molgenis.hrefToId(mappedDataSet.href),
						'documentIds' : [eachMapping.documentId],
						'updateScript' : storeMappingConfirmMapping + '=' + changedValue,
					};
					$.ajax({
						type : 'POST',
						url : molgenis.getContextUrl().replace('/biobankconnect', '') + '/mappingmanager/update',
						async : false,
						data : JSON.stringify(updateRequest),
						contentType : 'application/json'
					});
					
					molgenis.showMessage('alert alert-info', 'the mapping(s) has been updated for <strong>' + feature.Name + '</strong> in <strong>' + mappedDataSet.Name + '</strong> Biobank!', $('form:eq(-1)'));
				}
				
				if(displayedFeatures.length > 0){
					clickedCell.siblings('span:eq(0)').empty().append(displayedFeatures.join(' , '));
					clickedCell.removeClass('icon-pencil').addClass('icon-ok');
				}else{
					var firstMapping = mappingTable.find('input:eq(0)').data('eachMapping');
					clickedCell.siblings('span:eq(0)').empty().append(firstMapping.mappedFeature.Name);
					clickedCell.removeClass('icon-ok').addClass('icon-pencil');
				}
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
				async : false,
				success : function(data, textStatus, request) {
					console.log(data);
				},
				error : function(request, textStatus, error){
					console.log(error);
				} 
			});
		}
	};
	
	molgenis.MappingManager.prototype.downloadMappings = function(){
		var dataSet = restApi.get('/api/v1/dataset/' + selectedDataSet);
		var mappedDataSetIds = [];
		$.each(biobankDataSets, function(index, dataSet){
			if(molgenis.hrefToId(dataSet.href) !== selectedDataSet) mappedDataSetIds.push(molgenis.hrefToId(dataSet.href));
		});
		var deleteRequest = {
			'dataSetId' : selectedDataSet,
			'matchedDataSetIds' : mappedDataSetIds,
			'documentType' : dataSet.Identifier
		};
		$.download(molgenis.getContextUrl().replace('/biobankconnect', '') + '/mappingmanager/download',{request : JSON.stringify(deleteRequest)});
	};
	
	molgenis.MappingManager.prototype.createHelpModal = function(){
		var container = $('<div />');
		$('<div />').append('<i class="icon-ok"></i><span class="float-right text-success">Mappings have been selected</span>').appendTo(container);
		$('<div />').append('<i class="icon-pencil"></i><span class="float-right text-info">Select the mappings</span>').appendTo(container);
		$('<div />').append('<i class="icon-trash"></i><span class="float-right text-warning">Delete all mappings</span>').appendTo(container);
		$('<div />').append('<i class="icon-ban-circle"></i><span class="float-right text-error">No candidate available</span>').appendTo(container);
		standardModal.createModalCallback('Icon meanings', function(modal){
			modal.find('.modal-body:eq(0)').append(container);
			modal.css({
				'width' : 600,
				'margin-top' : 0,
				'top' : 200
			}).modal('show');
		});
	};
	
	molgenis.MappingManager.prototype.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	molgenis.MappingManager.prototype.getSelectedDataSet = function (){
		return selectedDataSet;
	};
	
	function setUserName(name){
		userName = name;
	}
	
	function getUserName(){
		return userName;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}, window.top));