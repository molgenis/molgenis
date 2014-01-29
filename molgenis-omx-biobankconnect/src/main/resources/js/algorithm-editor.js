(function($, molgenis) {
	var ns = molgenis;
	var pagination = new ns.Pagination();
	var standardModal = new ns.StandardModal();
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var selectedDataSet = null;
	var userName = null;
	var biobankDataSets = null;
	var sortRule = null;
	var storeMappingFeature = 'store_mapping_feature';
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var mappingScript = 'mapping_script';
	
	ns.AlgorithmEditor = function AlgorithmEditor(){
		
	};
	
	ns.AlgorithmEditor.prototype.changeDataSet = function(userName, selectedDataSet, dataSetIds){
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
				$('#dataitem-number').empty().append(searchResponse.totalHitCount);
				pagination.reset();
				ns.AlgorithmEditor.prototype.updateSelectedDataset(dataSetEntity);
				ns.AlgorithmEditor.prototype.createMatrixForDataItems();
			});
		}else{
			$('#dataitem-number').empty().append('Nothing selected');
		}
	};
	
	ns.AlgorithmEditor.prototype.createMatrixForDataItems = function() {
		var documentType = 'protocolTree-' + ns.hrefToId(selectedDataSet.href);
		var query = {
				rules : [[{
					operator : 'SEARCH',
					value : 'observablefeature'
				}]]
		};
		searchApi.search(pagination.createSearchRequest(documentType, query),function(searchResponse) {
			createMappingFromIndex(searchResponse, function(tableBody, involedDataSets){
				$('#dataitem-table').empty().append(createDynamicTableHeader(involedDataSets)).append(tableBody);
				pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()));
				pagination.updateMatrixPagination($('.pagination ul'), ns.AlgorithmEditor.prototype.createMatrixForDataItems);
			});
		});
		
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
		
		function createMappingFromIndex(searchResponse, callback){
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
			
			$.each(biobankDataSets, function(index, dataSet){
				var tuple = {};
				var searchRequest = {
					documentType : createDataSetIdentifier(selectedDataSet, dataSet),
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
							tuple[featureId] = {
								mappedFeatureId : storeMappedFeatureId,
								mappingScript : mapping.mappingScript,
								observationSet : mapping.observation_set,
								confirmed : mapping.confirmed,
								documentId : hit.id
							};
							if($.inArray(storeMappedFeatureId, allFeatureCollection) === -1) allFeatureCollection.push(storeMappedFeatureId);
						});
					}
					mappingPerStudy[ns.hrefToId(dataSet.href)] = tuple;
					count++;
					if(count === biobankDataSets.length) {
						preloadEntities(allFeatureCollection, cachedFeatures);
						renderMappingTable(mappingPerStudy, displayFeatures, cachedFeatures, callback);
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
		
		function renderMappingTable(mappingPerStudy, displayFeatures, cachedFeatures, callback){
			//create table header
			var involvedDataSetNames = [];
			involvedDataSetNames.push(selectedDataSet.name);
			$.each(biobankDataSets, function(index, dataSet){
				involvedDataSetNames.push(dataSet.name);
			});
			var tableBody = $('<tbody />');
			$.each(displayFeatures, function(index, featureFromIndex){
				var featureId = featureFromIndex.columnValueMap.id;
				tableBody.append(createRowForMappingTable(mappingPerStudy, featureId, cachedFeatures));
			});
			callback(tableBody, involvedDataSetNames);
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
		
		function createRowForMappingTable(mappingPerStudy, featureId, cachedFeatures){
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
			$.each(biobankDataSets, function(index, dataSet){
				var mappedDataSetId = ns.hrefToId(dataSet.href);
				var mapping = mappingPerStudy[mappedDataSetId];
				var mappedFeatures = mapping[featureId];
				var mappingScript = mapping[mappingScript];
				var confirmed = mapping.confirmed;
				var description = '';
				if(mappingScript) {
					var mappedFeatureEntity = cachedFeatures[mappedFeatures.mappedFeatureId];
					description = mappingScript;
				}
				var editIcon = $('<i />').addClass('show-popover ' + (confirmed ? 'icon-ok' : 'icon-pencil'));
				editIcon.css({
					position : 'relative',
					'float' : 'right'
				}).click(function(){
					standardModal.createModalCallback('Algorithm editor', function(modal){
						var mappedDataSet = restApi.get('/api/v1/dataset/' + mappedDataSetId);
						createMappingTable(feature, mappingScript, mappedDataSet, modal, editIcon);
						var table = row.parents('table:eq(0)');
						modal.attr('data-backdrop', true).css({
							'width' : '90%',
							'left' : '5%',
							'height' : '90%',
							'top' : '5%',
							'margin-left' : 0,
							'margin-top' : 0
						}).modal('show');
					});
				});
				var displayTermSpan = $('<span />').addClass('show-popover').append(description);
				$('<td />').addClass('add-border').append(displayTermSpan).append(editIcon).appendTo(row);
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
		
		function createMappingTable(feature, mappingScript, mappedDataSet, modal, clickedCell){
			var tableDiv = $('<div class="span5"></div>');
			var metaInfoDiv = $('<div class="span7"></div>');
			var body = modal.find('.modal-body:eq(0)').css('max-height', '100%');
			var featureInfoDiv = $('<div class="row-fluid"></div></br>').appendTo(body);
			$('<div class="row-fluid"></div>').append(metaInfoDiv).append(tableDiv).appendTo(body);
			var controlDiv = $('<div class="row-fluid"></div>').appendTo(body);
			
			var searchRequest = {
				featureId : ns.hrefToId(feature.href),
				sourceDataSetId : ns.hrefToId(selectedDataSet.href),
				selectedDataSetIds : [ns.hrefToId(mappedDataSet.href)]
			};
			$.ajax({
				type : 'POST',
				url : molgenis.getContextURL() + '/createmapping',
				async : false,
				data : JSON.stringify(searchRequest),
				contentType : 'application/json',
				success : function(data, textStatus, request) {	
					createFeatureInfo(feature, featureInfoDiv);
					$('<div />').addClass('row-fuild').css('margin-bottom', '10px').append('<strong>' + mappedDataSet.name + '</strong>').appendTo(tableDiv);
					createTableForRetrievedMappings(data.searchHits, tableDiv);
					$('<div />').addClass('row-fuild').css('margin-bottom', '10px').append('<strong>' + selectedDataSet.name + '</strong>').appendTo(metaInfoDiv);
					createEditorInModel(metaInfoDiv);
					$('<div />').addClass('row-fuild').css('margin-bottom', '10px').appendTo(controlDiv);
					addButtonsToControl(controlDiv);
				},
				error : function(request, textStatus, error){
					console.log(error);
				}
			});
			
			function createFeatureInfo(feature, parentDiv){
				var infoDiv = $('<div />').addClass('span4');
				$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.name + '</span>').appendTo(infoDiv);
				$('<div />').append('<span class="info"><strong>Data type : </strong></span>').append('<span>' + feature.dataType + '</span>').appendTo(infoDiv);
				$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + i18nDescription(feature).en + '</span>').appendTo(infoDiv);
				var middleDiv = $('<div />').addClass('span4');
				var categories = getCategoriesByFeatureId(ns.hrefToId(feature.href));
				if(categories.length > 0){
					var categoryDiv = $('<div />').addClass('span7');
					$.each(categories, function(index, category){
						categoryDiv.append('<div>' + category.valueCode + ' = ' + category.name + '</div>');
					});
					$('<div />').addClass('row-fluid').append('<div class="span3"><strong>Categories: </strong></div>').append(categoryDiv).appendTo(middleDiv);
				}
				parentDiv.append(infoDiv).append(middleDiv);
			}
			
			function createTableForRetrievedMappings(searchHits, parentDiv){
				if(searchHits.length === 0) return;
				var tableForSuggestedMappings = $('<table />').addClass('table table-bordered table-striped'); 
				var header = $('<tr><th>Name</th><th>Description</th>/tr>');
				tableForSuggestedMappings.append(header);
				$.each(searchHits, function(index, hit){
					var row = $('<tr />');
					var columnValueMap = hit.columnValueMap;
					row.append('<td>' + columnValueMap.name + '</td>');
					row.append('<td>' + columnValueMap.description + '</td>');
					tableForSuggestedMappings.append(row);
					row.css('cursor', 'pointer').click(function(){
						retrieveAllInfoForFeature(row, hit.columnValueMap);
					});
				});
				$('<div />').append(tableForSuggestedMappings).css({
					'overflow-y' : 'scroll',
					'height' : '300px'
				}).appendTo(parentDiv);
			}
			
			function retrieveAllInfoForFeature(clickedRow, hit){
				var detailInfoTable = $('<table class="table table-bordered"></table>');
				detailInfoTable.append('<tr><th>Id</th><td>' + hit.id + '</td></tr>');
				detailInfoTable.append('<tr><th>Name</th><td>' + hit.name + '</td></tr>');
				detailInfoTable.append('<tr><th>Data type</th><td>' + hit.dataType + '</td></tr>');
				detailInfoTable.append('<tr><th>Description</th><td>' + hit.description + '</td></tr>');
				var categories = getCategoriesByFeatureId(hit.id);
				if(categories.length > 0){
					var categoryDiv = $('<div />');
					$.each(categories, function(index, category){
						categoryDiv.append('<div>' + category.valueCode + ' = ' + category.name + '</div>');
					});
					detailInfoTable.append('<tr><th>Categories</th><td>' + categoryDiv.html() + '</td></tr>');
				}
				var parentTable = clickedRow.parents('table:eq(0)');
				var backButton = $('<button class="btn btn-primary">Go back</button>');
				parentTable.hide().before(detailInfoTable).before(backButton);
				backButton.click(function(){
					detailInfoTable.remove();
					backButton.remove();
					parentTable.show();
				});
				detailInfoTable.click(function(){
					backButton.click();
				});
			}
			
			function getCategoriesByFeatureId(featureId){
				var categories = restApi.get('/api/v1/category/', null, {
					q : [{
						field : 'observableFeature',
						operator : 'EQUALS',
						value : featureId
					}],
				});
				return categories.items; 
			}
			
			function createEditorInModel(parentDiv){
				$('<div />').append('<div id="algorithmEditorDiv"></div>').appendTo(parentDiv);
				var editor = ace.edit('algorithmEditorDiv');
				editor.setTheme("ace/theme/chrome");
				editor.getSession().setMode("ace/mode/javascript");
			}
			
			function addButtonsToControl(parentDiv){
				var testButton = $('<button class="btn btn-primary">Test</button>');
				var suggestionButtion = $('<button class="btn">Suggestion</button>');
				$('<div class="row-fluid"></div>').append(testButton).append(' ').append(suggestionButtion).appendTo(parentDiv);
				testButton.click(function(){
					console.log('Test button has been clicked!');
				});
				suggestionButtion.click(function(){
					console.log('Suggestion button has been clicked!');
				});
			}
		}
		
		function i18nDescription(feature){
			if(feature.description === undefined) feature.description = '';
			if(feature.description.indexOf('{') !== 0){
				feature.description = '{"en":"' + (feature.description === null ? '' : feature.description.replace(new RegExp('"','gm'), '')) +'"}';
			}
			return eval('(' + feature.description + ')');
		}
	};
	
	ns.AlgorithmEditor.prototype.updateSelectedDataset = function(dataSet) {
		selectedDataSet = dataSet;
	};
	
	ns.AlgorithmEditor.prototype.getSelectedDataSet = function (){
		return selectedDataSet;
	};
	
	function createDataSetIdentifier(targetDataSet, sourceDataSet){
		return getUserName() + '-' + ns.hrefToId(targetDataSet.href) + '-' + ns.hrefToId(sourceDataSet.href);
	}
	
	function setUserName(name){
		userName = name;
	}
	
	function getUserName(){
		return userName;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));