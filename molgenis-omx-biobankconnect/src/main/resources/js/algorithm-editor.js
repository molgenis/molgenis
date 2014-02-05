(function($, molgenis) {
	var pagination = new molgenis.Pagination();
	var standardModal = new molgenis.StandardModal();
	var restApi = new molgenis.RestClient();
	var searchApi = new molgenis.SearchClient();
	var selectedDataSet = null;
	var userName = null;
	var biobankDataSets = null;
	var sortRule = null;
	var storeMappingFeature = 'store_mapping_feature';
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var mappingScript = 'store_mapping_algorithm_script';
	
	molgenis.AlgorithmEditor = function AlgorithmEditor(){};
	
	molgenis.AlgorithmEditor.prototype.changeDataSet = function(userName, selectedDataSet, dataSetIds){
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
				$('#dataitem-number').empty().append(searchResponse.totalHitCount);
				pagination.reset();
				updateSelectedDataset(dataSetEntity);
				molgenis.AlgorithmEditor.prototype.createMatrixForDataItems();
			});
		}else{
			$('#dataitem-number').empty().append('Nothing selected');
		}
	};
	
	molgenis.AlgorithmEditor.prototype.createMatrixForDataItems = function() {
		var documentType = 'protocolTree-' + molgenis.hrefToId(selectedDataSet.href);
		var query = {
				rules : [[{
					operator : 'SEARCH',
					value : 'observablefeature'
				}]]
		};
		
		if(sortRule !== null) query.sort = sortRule;
		searchApi.search(pagination.createSearchRequest(documentType, query),function(searchResponse) {
			createAlgorithmMappingTable(searchResponse, function(tableBody, involedDataSets){
				$('#algorithm-table').empty().append(createHeaderAlgorithmMappingTable(involedDataSets)).append(tableBody);
				pagination.setTotalPage(Math.ceil(searchResponse.totalHitCount / pagination.getPager()));
				pagination.updateMatrixPagination($('.pagination ul'), molgenis.AlgorithmEditor.prototype.createMatrixForDataItems);
			});
		});
		
		function createAlgorithmMappingTable(searchResponse, callback){
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
								mappingScript : mapping[mappingScript],
								observationSet : mapping.observation_set,
								confirmed : mapping.confirmed,
								documentId : hit.id
							};
							if($.inArray(storeMappedFeatureId, allFeatureCollection) === -1) allFeatureCollection.push(storeMappedFeatureId);
						});
					}
					mappingPerStudy[molgenis.hrefToId(dataSet.href)] = tuple;
					count++;
					if(count === biobankDataSets.length) {
						preloadEntities(allFeatureCollection, cachedFeatures);
						renderAlgorithmMappingTable(mappingPerStudy, displayFeatures, cachedFeatures, callback);
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
					cachedFeatures[(molgenis.hrefToId(element.href))] = element;
				});
			}
		}
		
		function renderAlgorithmMappingTable(mappingPerStudy, displayFeatures, cachedFeatures, callback){
			var involvedDataSetNames = [];
			involvedDataSetNames.push(selectedDataSet.name);
			$.each(biobankDataSets, function(index, dataSet){
				involvedDataSetNames.push(dataSet.name);
			});
			var tableBody = $('<tbody />');
			$.each(displayFeatures, function(index, featureFromIndex){
				var featureId = featureFromIndex.columnValueMap.id;
				tableBody.append(createRowForAlgorithmMappingTable(mappingPerStudy, featureId, cachedFeatures));
			});
			callback(tableBody, involvedDataSetNames);
		}
		
		function createHeaderAlgorithmMappingTable(involedDataSets){
			var dataSetRow = $('<tr />');
			var columnWidth = 60 / (involedDataSets.length - 1);
			var firstColumn = null;
			for(var i = 0; i < involedDataSets.length; i++){
				if(i === 0){
					firstColumn = $('<th class="text-align-center">' + involedDataSets[i] + '</th>').css('width', '30%').appendTo(dataSetRow);
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
					molgenis.AlgorithmEditor.prototype.createMatrixForDataItems();
					return false;
				});
			}
			return $('<thead />').append(dataSetRow);
		}	
		
		function createRowForAlgorithmMappingTable(mappingPerStudy, featureId, cachedFeatures){
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
			$('<td />').addClass('show-popover').css('width', '20%').append(popover).appendTo(row);
			
			$.each(biobankDataSets, function(index, dataSet){
				row.append(initializeAlgorithmEditorEvent(dataSet, mappingPerStudy, feature, cachedFeatures));
			});
			return row;
		}
		
		function initializeAlgorithmEditorEvent(dataSet, mappingPerStudy, feature, cachedFeatures){
			var mappedDataSetId = molgenis.hrefToId(dataSet.href);
			var mapping = mappingPerStudy[mappedDataSetId][molgenis.hrefToId(feature.href)];
			if(!mapping) mapping = {};
			if(!mapping.mappingScript) mapping.mappingScript = '';
			var newCell = $('<td />').css('cursor','pointer').click(function(){
				standardModal.createModalCallback('Algorithm editor', function(modal){
					$(document).data('clickedCell', newCell).data('mapping', mapping);
					var mappedDataSet = restApi.get('/api/v1/dataset/' + mappedDataSetId);
					createAlgorithmEditorContent(feature, mappedDataSet, modal);
					modal.attr('data-backdrop', true).css({
						'width' : '90%',
						'left' : '5%',
						'top' : ($(document) - $(this).height())/2,
						'margin-left' : 0,
						'margin-top' : 0
					}).modal('show');
				});
			})
			return newCell.append(createCellInMappingPanel(mapping.mappingScript));
		}
		
		function createCellInMappingPanel(mappingScript) {
			var editIcon = $('<i />').addClass('show-popover ' + (mappingScript === '' ? 'icon-pencil' : 'icon-ok'));
			var iconHolderDiv = $('<div />').css({
				'float' : 'right',
				'margin-right' : '20%'
			}).append(editIcon);
			var algorithmStatusDiv = $('<div />').css({
				'float' : 'left',
				'margin-left' : '30%'
			});
			if(mappingScript === '') algorithmStatusDiv.append('<strong>Edit algorithm</strong>');
			else algorithmStatusDiv.append('<strong>Complete</strong>').css('color','#04B4AE');
			return $('<div />').addClass('row-fluid').append(algorithmStatusDiv).append(iconHolderDiv);
		}
		
		function createAlgorithmEditorContent(feature, mappedDataSet, modal){
			
			var searchRequest = {
				featureId : molgenis.hrefToId(feature.href),
				sourceDataSetId : molgenis.hrefToId(selectedDataSet.href),
				selectedDataSetIds : [molgenis.hrefToId(mappedDataSet.href)]
			};
			$(document).data('searchRequest', searchRequest);
			var tableDiv = $('<div class="span5"></div>');
			var metaInfoDiv = $('<div class="span7"></div>');
			var body = modal.find('.modal-body:eq(0)').css('max-height','100%');
			var featureInfoDiv = $('<div class="row-fluid"></div>').after('</br>').appendTo(body);
			$('<div class="row-fluid"></div>').append(metaInfoDiv).append(tableDiv).appendTo(body);
			var controlDiv = $('<div class="row-fluid"></div>').appendTo(body);
			
			$.ajax({
				type : 'POST',
				url : molgenis.getContextURL() + '/createmapping',
				async : false,
				data : JSON.stringify(searchRequest),
				contentType : 'application/json',
				success : function(data, textStatus, request) {	
					createFeatureInfoPanel(feature, featureInfoDiv);
					$('<div />').addClass('row-fuild').css('margin-bottom', '10px').append('<strong>' + mappedDataSet.name + '</strong>').appendTo(tableDiv);
					createTableForRetrievedMappings(data.searchHits, tableDiv);
					$('<div />').addClass('row-fuild').css('margin-bottom', '10px').append('<strong>' + selectedDataSet.name + '</strong>').appendTo(metaInfoDiv);
					var editor = createEditorInModel(metaInfoDiv, mappedDataSet);
					$('<div />').addClass('row-fuild').css('margin-bottom', '10px').before(controlDiv);
					addButtonsToControl(controlDiv, editor);
				},
				error : function(request, textStatus, error){
					console.log(error);
				}
			});
			
			function createFeatureInfoPanel(feature, parentDiv){
				var infoDiv = $('<div />').addClass('span3');
				$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.name + '</span>').appendTo(infoDiv);
				$('<div />').append('<span class="info"><strong>Data type : </strong></span>').append('<span>' + feature.dataType + '</span>').appendTo(infoDiv);
				$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + i18nDescription(feature).en + '</span>').appendTo(infoDiv);
				var middleDiv = $('<div />').addClass('span9');
				var categories = getCategoriesByFeatureId(molgenis.hrefToId(feature.href));
				if(categories.length > 0){
					var categoryDiv = $('<div />').addClass('span8').css('margin-left', '30px');
					$.each(categories, function(index, category){
						categoryDiv.append('<div>' + category.valueCode + ' = ' + category.name + '</div>');
					});
					$('<div />').addClass('row-fluid').append('<div class="span1"><strong>Categories: </strong></div>').append(categoryDiv).appendTo(middleDiv);
				}
				parentDiv.append(infoDiv).append(middleDiv);
			}
			
			function createTableForRetrievedMappings(searchHits, parentDiv){
				if(searchHits.length === 0) {
					$('<div />').append('No mappings were found!').appendTo(parentDiv);
					return;
				}
				var tableForSuggestedMappings = $('<table />').addClass('table table-bordered'); 
				var header = $('<thead><tr><th>Name</th><th>Description</th><th>Data type</th>/tr></thead>');
				tableForSuggestedMappings.append(header);
				$.each(searchHits, function(index, hit){
					var row = $('<tr />');
					var featureId = hit.columnValueMap.id;
					var featureEntity = restApi.get('/api/v1/observablefeature/' + featureId);
					row.append('<td>' + featureEntity.name + '</td>');
					row.append('<td>' + featureEntity.description + '</td>');
					row.append('<td>' + featureEntity.dataType + '</td>');
					tableForSuggestedMappings.append(row);
					row.css('cursor', 'pointer').click(function(){
						retrieveAllInfoForFeature(row, featureEntity);
					});
				});
				$('<div />').append(tableForSuggestedMappings).addClass('well').css({
					'overflow-y' : 'scroll',
					'height' : $(document).height()/4
				}).appendTo(parentDiv);
			}
			
			function retrieveAllInfoForFeature(clickedRow, featureEntity){
				var detailInfoTable = $('<table class="table table-bordered"></table>');
				detailInfoTable.append('<tr><th>Id</th><td>' + molgenis.hrefToId(featureEntity.href) + '</td></tr>');
				detailInfoTable.append('<tr><th>Name</th><td>' + featureEntity.name + '</td></tr>');
				detailInfoTable.append('<tr><th>Data type</th><td>' + featureEntity.dataType + '</td></tr>');
				detailInfoTable.append('<tr><th>Description</th><td>' + featureEntity.description + '</td></tr>');
				var categories = getCategoriesByFeatureId(molgenis.hrefToId(featureEntity.href));
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
			
			function createEditorInModel(parentDiv, mappedDataSet){
				var algorithmEditorDiv = $('<div id="algorithmEditorDiv"></div>');
				algorithmEditorDiv.addClass('well').css('height', $(document).height()/4).appendTo(parentDiv);
				var langTools = ace.require("ace/ext/language_tools");
				var editor = ace.edit('algorithmEditorDiv');
				editor.setOptions({
				    enableBasicAutocompletion: true
				});
				var script = null;
				if($(document).data('previousScript')){
					script = $(document).data('previousScript');
					$(document).removeData('previousScript');
				}
				if(script === null) {
					script = $(document).data('mapping').mappingScript	
				}
				editor.setValue(script);
				editor.setTheme("ace/theme/chrome");
				editor.getSession().setMode("ace/mode/javascript");
				var algorithmEditorCompleter = {
			        getCompletions: function(editor, session, pos, prefix, callback) {
			            if (prefix.length === 0) { callback(null, []); return }
			            searchApi.search(searchFeatureByName('protocolTree-' + molgenis.hrefToId(mappedDataSet.href), prefix), function(searchResponse){
			            	console.log(searchResponse);
		                    callback(null, searchResponse.searchHits.map(function(hit) {
		                    	var map = hit.columnValueMap;
		                        return {name: '$(\'' + map.name + '\')', value: '$(\'' + map.name + '\')', score: map.score, meta: mappedDataSet.name};
		                    }));
			            });
			        }
			    }
			    langTools.addCompleter(algorithmEditorCompleter);
				return editor;
			}
			
			function searchFeatureByName(documentType, prefix){
				var queryRules = [];
	            queryRules.push({
	            	field : 'name',
					operator : 'LIKE',
					value : prefix
				});
	            queryRules.push({
	            	operator : 'AND'
	            });
	            queryRules.push({
	            	field : 'type',
					operator : 'EQUALS',
					value : '"observablefeature"'
	            });
	            var autoCompletionSearchRequest = {
            		documentType : documentType,
            		query : {
    					pageSize: 10000,
    					rules: [queryRules]
    				}
	            };
	            return autoCompletionSearchRequest;
			}
			
			function addButtonsToControl(parentDiv, editor){
				var testStatisticsButton = $('<button class="btn btn-info">Test</button>');
				var suggestScriptButtion = $('<button class="btn btn-primary">Suggestion</button>');
				var saveScriptButton = $('<button class="btn btn-success">Save script</button>').css('float','right');
				$('<div class="span7"></div>').append(testStatisticsButton).append(' ').append(suggestScriptButtion).append(' ').append(saveScriptButton).appendTo(parentDiv);
				testStatisticsButton.click(function(){
					console.log('The testStatisticsButton button has been clicked!');
					var modalBody = parentDiv.parents('.modal-body:eq(0)');
					var ontologyMatcherRequest = $.extend($(document).data('searchRequest'), {
						'algorithmScript' : editor.getValue()
					});
					$.ajax({
						type : 'POST',
						url : molgenis.getContextURL() + '/testscript',
						async : false,
						data : JSON.stringify(ontologyMatcherRequest),
						contentType : 'application/json',
						success : function(data, textStatus, request){	
							if(data.results.length === 0) return;
							var graphDivId = 'featureId-' + ontologyMatcherRequest['featureId'];
							var statisticsDivHeight = modalBody.height()/4*3;
							modalBody.data('originalContent', modalBody.children());
							modalBody.children().remove();
							var backButton = $('<button />').addClass('btn btn-primary').append('Go back');
							var featureObject = restApi.get('/api/v1/observablefeature/' + ontologyMatcherRequest['featureId']);
							var dataSetObject = restApi.get('/api/v1/dataset/' + ontologyMatcherRequest['selectedDataSetIds'][0]);
							var algorithmDiv = $('<div />').addClass('offset3 span6 well text-align-center').append('Test for variable <strong>' + featureObject.name + '</strong> in dataset <strong>' + dataSetObject.name + '</strong>');
							var tableDiv = $('<div />').addClass('span6 well').css('min-height', statisticsDivHeight).append('<div class="legend-align-center">Summary statistics</div>').append(statisticsTable(data));
							var graphDiv = $('<div />').attr('id', graphDivId).addClass('span6 well').css('min-height', statisticsDivHeight).append('<div class="legend-align-center">Distribution plot</div>').bcgraph(data.results);
							$('<div />').addClass('row-fluid').append(algorithmDiv).appendTo(modalBody);
							$('<div />').addClass('row-fluid').append(tableDiv).append(graphDiv).appendTo(modalBody);
							modalBody.next('div:eq(0)').prepend(backButton);
							backButton.click(function(){
								$(document).data('previousScript',editor.getValue());
								$(document).data('clickedCell').click();
							});
						},
						error : function(request, textStatus, error){
							console.log(error);
						}
					});
				});
				suggestScriptButtion.click(function(){
					console.log('The suggestScript button has been clicked!');
					$.ajax({
						type : 'POST',
						url : molgenis.getContextURL() + '/suggestscript',
						async : false,
						data : JSON.stringify($(document).data('searchRequest')),
						contentType : 'application/json',
						success : function(data, textStatus, request) {	
							if(data.suggestedScript){
								editor.setValue(data.suggestedScript);
							}
						},
						error : function(request, textStatus, error){
							console.log(error);
						}
					});
				});
				saveScriptButton.click(function(){
					console.log('The saveScriptButton button has been clicked!' + editor.getValue());
					var modalBody = parentDiv.parents('.modal-body:eq(0)');
					var ontologyMatcherRequest = $.extend($(document).data('searchRequest'), {
						'algorithmScript' : editor.getValue()
					});
					$.ajax({
						type : 'POST',
						url : molgenis.getContextURL() + '/savescript',
						async : false,
						data : JSON.stringify(ontologyMatcherRequest),
						contentType : 'application/json',
						success : function(data, textStatus, request) {	
							console.log(data);
							var alerts = [];
							alerts.push(data);
							modalBody.find('.alert').remove();
							molgenis.createAlert(alerts, 'success', modalBody);
							$(document).data('clickedCell').empty().append(createCellInMappingPanel(editor.getValue()));
							$(document).data('mapping').mappingScript = editor.getValue();
						},
						error : function(request, textStatus, error){
							console.log(error);
						}
					});
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
		
		function statisticsTable(data){
			var array = data.results;
			var table = $('<table />').addClass('table table-bordered');
			table.append('<tr><th>Total cases</th><td>' + data.totalCounts + '</td></tr>');
			table.append('<tr><th>Valid cases</th><td>' + array.length + '</td></tr>');
			table.append('<tr><th>Mean</th><td>' + jStat.mean(array) + '</td></tr>');
			table.append('<tr><th>Median</th><td>' + jStat.median(array) + '</td></tr>');
			table.append('<tr><th>Standard Deviation</th><td>' + jStat.stdev(array) + '</td></tr>');
			return table;
		}
	};
	
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
	
	function updateSelectedDataset(dataSet){
		selectedDataSet = dataSet;
	}
	
	function getSelectedDataSet(){
		return selectedDataSet;
	}

	function createDataSetIdentifier(targetDataSet, sourceDataSet){
		return getUserName() + '-' + molgenis.hrefToId(targetDataSet.href) + '-' + molgenis.hrefToId(sourceDataSet.href);
	}
	
	function setUserName(name){
		userName = name;
	}
	
	function getUserName(){
		return userName;
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));