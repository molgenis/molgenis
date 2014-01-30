(function($, molgenis, w) {
	"use strict";
	
	var ns = molgenis;
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	var userName = null;
	var biobankDataSets = null;
	var sortRule = null;
	var previousSearchText = null;
	var storeMappingFeature = 'store_mapping_feature';
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var storeMappingConfirmMapping = 'store_mapping_confirm_mapping';
	var scoreMappingScore = "store_mapping_score";
	
	ns.MappingManager = function MappingManager(){
		
	};
	
	ns.MappingManager.prototype.changeDataSet = function(userName, selectedDataSet, dataSetIds){
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
				documentType : 'protocolTree-' + ns.hrefToId(dataSetEntity.href),
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
				ns.MappingManager.prototype.updateSelectedDataset(selectedDataSet);
				ns.MappingManager.prototype.createMatrixForDataItems();
				initSearchDataItems(dataSetEntity);
			});
		}else{
			$('#dataitem-number').empty().append('Nothing selected');
		}
		
		function initSearchDataItems (dataSet) {
			$('#search-dataitem').typeahead({
				source: function(query, process) {
					ns.dataItemsTypeahead('observablefeature', ns.hrefToId(dataSet.href), query, process);
				},
				minLength : 3,
				items : 20
			});
			$('#search-button').click(function(){
				ns.MappingManager.prototype.createMatrixForDataItems();
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
					ns.MappingManager.prototype.createMatrixForDataItems();
			    }
			});
		}
	};
	
	ns.MappingManager.prototype.createMatrixForDataItems = function() {
		var dataSetMapping = getDataSetsForMapping();
		if(dataSetMapping.items.length > 0){
			var documentType = 'protocolTree-' + dataSetMapping.items[0].identifier.split('-')[1];
			
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
					pagination.updateMatrixPagination($('.pagination ul'), ns.MappingManager.prototype.createMatrixForDataItems);
				});
			});
		}else{
			$('#dataitem-table').empty();
			$('#table-papger').empty();
			var dataSetEntity = restApi.get('/api/v1/dataset/' + ns.MappingManager.prototype.getSelectedDataSet());
			showMessage('alert alert-info', 'There are not mappings for <strong>' + dataSetEntity.name + '</strong> catalogue');
		}
		
		function getDataSetsForMapping(){
			var identifiers = [];
			$.each(biobankDataSets, function(index, dataSet){
				var dataSetId = ns.hrefToId(dataSet.href);
				if(dataSetId !== ns.MappingManager.prototype.getSelectedDataSet())
				identifiers.push(getUserName() + '-' + ns.MappingManager.prototype.getSelectedDataSet() + '-' + dataSetId); 
			});
			var dataSetMapping = restApi.get('/api/v1/dataset/', null, {
				q : [{
					field : 'identifier',
					operator : 'IN',
					value : identifiers
				}],
			});
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
						operator : 'OR'
					});
				}
				queryRules.push({
					field : storeMappingFeature,
					operator : 'EQUALS',
					value : hitInfo.id.toString()
				});
				allFeatureCollection.push(hitInfo.id);
			});
			
			$.each(dataSets, function(index, dataSet){
				var tuple = {};
				var searchRequest = {
					documentType : dataSet.identifier,
					query : {
						pageSize: 10000,
						rules: [queryRules]
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
								score : mapping[scoreMappingScore],
								mappedFeatureId : storeMappedFeatureId,
								confirmed : mapping[storeMappingConfirmMapping],
								observationSet : mapping.observation_set,
								documentId : hit.id
							});
							
							if($.inArray(storeMappedFeatureId, allFeatureCollection) === -1) allFeatureCollection.push(storeMappedFeatureId);
						});
					}
					var dataSetIdArray = dataSet.identifier.split('-');
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
				var listOfFeatures = restApi.get('/api/v1/observablefeature', null, {
					q : [{
						field : 'id',
						operator : 'IN',
						value : allFeatureCollection.slice(lower, upper)
					}],
					num : 500
				});
				$.each(listOfFeatures.items, function(index, element){
					cachedFeatures[(ns.hrefToId(element.href))] = element;
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
					var dataSetIdArray = dataSet.identifier.split('-');
					involvedDataSetIds.push(dataSetIdArray[2]);
				}
			});
			involvedDataSetIds.splice(0, 0, ns.MappingManager.prototype.getSelectedDataSet());
			biobankDataSets = sortOrderOfDataSets(biobankDataSets, involvedDataSetIds);
			var removeDataSetIndex = [];
			$.each(biobankDataSets, function(index, dataSet){
				if($.inArray(ns.hrefToId(dataSet.href), involvedDataSetIds) !== -1){
					involvedDataSetNames.push(dataSet.name);
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
						if(ns.hrefToId(currentDataSet.href) === dataSetId){
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
			var isPopOver = description.length < 90;
			var popover = $('<span />').html(isPopOver ? description : description.substring(0, 90) + ' ...');
			if(!isPopOver){
				popover.addClass('show-popover');
				popover.popover({
					content : i18nDescription(feature).en,
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
				var mappedDataSetId = dataSet.identifier.split('-')[2];
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
									'documentType' : dataSet.identifier,
									'documentIds' : documentIds
								};
								$.ajax({
									type : 'POST',
									url : molgenis.getContextURL().replace('/biobankconnect', '') + '/mappingmanager/delete',
									async : false,
									data : JSON.stringify(deleteRequest),
									contentType : 'application/json',
								});
								modal.remove();
								ns.MappingManager.prototype.createMatrixForDataItems();
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
			var featureId = ns.hrefToId(feature.href);
			var title = 'Rematch research variable : ';
			standardModal.createModalCallback('Rematch research variable : ' + feature.name, function(modal){
				restApi.getAsync(feature.href, ["unit", "definitions"], null, function(restApiFeature){
					var body = modal.find('div.modal-body:eq(0)').addClass('overflow-y-visible');
					ns.getOntologyAnnotator().createFeatureTable(body, title, restApiFeature, createAnnotationModal);
					body.append(ns.getOntologyAnnotator().createSearchDiv(title, restApiFeature, createAnnotationModal));
					var footer = modal.find('div.modal-footer:eq(0)');
					var nextButton = $('<button type="btn" class="btn btn-primary">Next</button>').click(function(){
						createRematchingModal(restApiFeature);
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
			});
		}
		
		function createRematchingModal(feature){
			standardModal.createModalCallback('Rematch research variable : ' + feature.name, function(modal){
				var body = modal.find('div.modal-body:eq(0)');
				var divControlPanel = $('<div />').addClass('row-fluid').appendTo(body);
				var selectTag = $('<select />');
				$.each(biobankDataSets, function(index, dataSet){
					var dataSetId = ns.hrefToId(dataSet.href);
					if(dataSetId !== ns.MappingManager.prototype.getSelectedDataSet()){
						selectTag.append('<option value="' + dataSetId + '">' + dataSet.name + '</option>');
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
						'sourceDataSetId' : ns.MappingManager.prototype.getSelectedDataSet(),
						'featureId' : ns.hrefToId(feature.href),
						'selectedDataSetIds' : selectedOptions
					};
					$.ajax({
						type : 'POST',
						url : ns.getContextURL() + '/rematch',
						async : false,
						data : JSON.stringify(request),
						contentType : 'application/json',
						success : function(data, textStatus, request) {
							modal.remove();
							var storedRowInfo = $('body').data('clickedRow');
							var existingRow = storedRowInfo[ns.hrefToId(feature.href)];
							var biobankDataSetIds = [];
							$.each(biobankDataSets, function(index, dataSet){
								biobankDataSetIds.push(ns.hrefToId(dataSet.href));
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
					url : ns.getContextURL() + '/match/status',
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
					'documentType' : 'protocolTree-' + ns.MappingManager.prototype.getSelectedDataSet(),
					'query' : {
						'rules':[[{
							'field' : 'id',
							'operator' : 'EQUALS',
							'value' : ns.hrefToId(feature.href).toString()
						}]]
					}
				}
				searchApi.search(searchRequest, function(searchResponse){
					var storedRowInfo = $('body').data('clickedRow');
					var existingRow = storedRowInfo[ns.hrefToId(feature.href)];
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
					delete storedRowInfo[ns.hrefToId(feature.href)];
				});
			}
			
			function listOfOptions(dataSetsContainer, selectedOptions){
				dataSetsContainer.find('div:gt(0)').remove();
				$.each(selectedOptions, function(index, dataSetId){
					var dataSet = restApi.get('/api/v1/dataset/' + dataSetId);
					var newRow = $('<div />').addClass('row-fluid');
					$('<div />').addClass('offset2 span3').append(dataSet.name).appendTo(newRow);
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
				options.attr('selected', false).each(function(){
					if(targetDataSetId !== $(this).val()){
						index++;
					}else return false;
				});
				index = index === options.length - 1 ? 0 : index + 1;
				$(options[index]).attr('selected', true);
			}
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
			var mappingTable = $('<table />').addClass('table table-bordered table-striped'); 
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
						selectedFeatures.push(mappedFeature.name);
					}
					eachMapping.mappedFeature = mappedFeature;
					checkBox.data('eachMapping', eachMapping);
					row.append('<td>' + mappedFeature.name + '</td><td>' + i18nDescription(mappedFeature).en + '</td><td>' + score + '</td>');
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
				updateMappingInfo(feature, mappingTable, mappedDataSet, clickedCell);
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
			var confirmFeature = null;
			mappingTable.find('input').each(function(index, checkBox){
				var eachMapping = $(checkBox).data('eachMapping');
				var changedValue = null;
				if(checkBox.checked){
					displayedFeatures.push(eachMapping.mappedFeature.name);
				}
				if(checkBox.checked && !eachMapping.confirmed){
					changedValue = true;
				}
				if(!checkBox.checked && eachMapping.confirmed){
					changedValue = false;
				}
				if(changedValue !== null){
					eachMapping.confirmed = changedValue;
					if(confirmFeature === null){
						confirmFeature = restApi.get('/api/v1/observablefeature', null, {
							q : [{
								field : 'identifier',
								operator : 'EQUALS',
								value : storeMappingConfirmMapping
							}]
						});
					}
					
					var observedValue = restApi.get('/api/v1/observedvalue', ['value'], {
						q : [{
							field : 'observationSet',
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
						'documentType' : getUserName() + '-' + selectedDataSet + '-' + ns.hrefToId(mappedDataSet.href),
						'documentIds' : [eachMapping.documentId],
						'updateScript' : storeMappingConfirmMapping + '=' + changedValue,
					};
					$.ajax({
						type : 'POST',
						url : molgenis.getContextURL().replace('/biobankconnect', '') + '/mappingmanager/update',
						async : false,
						data : JSON.stringify(updateRequest),
						contentType : 'application/json'
					});
					
					showMessage('alert alert-info', 'the mapping(s) has been updated for <strong>' + feature.name + '</strong> in <strong>' + mappedDataSet.name + '</strong> Biobank!');
				}
				
				if(displayedFeatures.length > 0){
					clickedCell.siblings('span:eq(0)').empty().append(displayedFeatures.join(' , '));
					clickedCell.removeClass('icon-pencil').addClass('icon-ok');
				}else{
					var firstMapping = mappingTable.find('input:eq(0)').data('eachMapping');
					clickedCell.siblings('span:eq(0)').empty().append(firstMapping.mappedFeature.name);
					clickedCell.removeClass('icon-ok').addClass('icon-pencil');
				}
			});
		}
		
		function showMessage(alertClass, message){
			var messageDiv = $('#alert-message');
			if(messageDiv.length === 0) messageDiv = $('<div id="alert-message"></div>');
			var messageAlert = $('<div />').addClass(alertClass).append('<button type="button" class="close" data-dismiss="alert">&times;</button>');
			$('<span><strong>Message : </strong>' + message + '</span>').appendTo(messageAlert);
			messageDiv.empty().append(messageAlert);
			$('form:eq(-1)').prepend(messageDiv);
			w.setTimeout(function(){messageDiv.fadeOut(1000).remove();}, 10000);
		}
		
		function i18nDescription(feature){
			if(feature.description === undefined) feature.description = '';
			if(feature.description.indexOf('{') !== 0){
				feature.description = '{"en":"' + (feature.description === null ? '' : feature.description.replace(new RegExp('"','gm'), '')) +'"}';
			}
			return eval('(' + feature.description + ')');
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
	
	ns.MappingManager.prototype.downloadMappings = function(){
		var dataSet = restApi.get('/api/v1/dataset/' + selectedDataSet);
		var mappedDataSetIds = [];
		$.each(biobankDataSets, function(index, dataSet){
			if(ns.hrefToId(dataSet.href) !== selectedDataSet) mappedDataSetIds.push(ns.hrefToId(dataSet.href));
		});
		var deleteRequest = {
			'dataSetId' : selectedDataSet,
			'matchedDataSetIds' : mappedDataSetIds,
			'documentType' : dataSet.identifier
		};
		$.download(molgenis.getContextURL().replace('/biobankconnect', '') + '/mappingmanager/download',{request : JSON.stringify(deleteRequest)});
	};
	
	ns.MappingManager.prototype.createHelpModal = function(){
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
	
	ns.MappingManager.prototype.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	ns.MappingManager.prototype.getSelectedDataSet = function (){
		return selectedDataSet;
	};
	
	function setUserName(name){
		userName = name;
	}
	
	function getUserName(){
		return userName;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}, window.top));
