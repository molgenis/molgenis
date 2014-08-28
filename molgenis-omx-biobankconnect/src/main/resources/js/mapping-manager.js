(function($, molgenis, w) {
	"use strict";
	
	var ontologyAnnotator = new molgenis.OntologyAnnotator();
	var standardModal = new molgenis.StandardModal();
	var restApi = new molgenis.RestClient();
	var selectedDataSet = null;
	var userName = null;
	var biobankDataSets = null;
	var storeMappingMappedFeature = 'store_mapping_mapped_feature';
	var observationSet = 'observationsetid';
	var mappingScript = 'store_mapping_algorithm_script';
	
	molgenis.MappingManager = function MappingManager(){};
	
	molgenis.MappingManager.prototype.changeDataSet = function(userName, selectedDataSetId, dataSetIds){
		
		//Initialize selectedDataset with default value null
		selectedDataSet = null;
		//Clear the previous messages if there are any 
		if($('#mapping-unavailable')) {
			$('#mapping-unavailable').remove();
		}
		//Check the nullibity of target selecteDataSetId
		if(!selectedDataSetId || selectedDataSetId === ''){
			var mappingUnavailable = $('<p />').attr('id', 'mapping-unavailable').addClass('text-align-center').append('The target catalogue cannot be empty!').css('margin-top', '50px');
			$('#container').empty().after(mappingUnavailable);
			$('#dataitem-number').empty().append('Nothing selected');
			$('#div-search').hide();
		}else{
			selectedDataSet = restApi.get('/api/v1/dataset/' + selectedDataSetId, {'expand' : ['protocolUsed']});
			//Check the nullibity of selecte biobank dataSetIds
			if(!dataSetIds || dataSetIds.length === 0){
				var mappingUnavailable = $('<p />').attr('id', 'mapping-unavailable').addClass('text-align-center').append('There are no matches produced for catalog <strong>' + selectedDataSet.Name + '</strong> . Please click on BiobankConnectWizard to produce new match sets!').css('margin-top', '50px');
				$('#container').empty().after(mappingUnavailable);
				$('#dataitem-number').empty().append('Nothing selected');
				$('#div-search').hide();
			}else{
				$('#div-search').show();
				setUserName(userName); 
				biobankDataSets = restApi.get('/api/v1/dataset/', {
					'q' : {
						'q' : [{
							'field' : 'id',
							'operator' : 'IN',
							'value' : remove(selectedDataSetId, dataSetIds)
						}]
					},
					'expand' : ['protocolUsed']
				}).items;
				$('#dataitem-number').empty().append(molgenis.getTotalNumberOfItems(selectedDataSetId));
				updateMatrix();
				initSearchDataItems();
			}
		} 
		
		function initSearchDataItems() {
			var options = {'updatePager' : true};
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
					updateMatrix(options);
			    }
			});
			$('#search-button').click(function(){
				updateMatrix(options);
			});
		}
	};
	
	function updateMatrix(options){
		var involvedDataSetNames = [];
		involvedDataSetNames.push(selectedDataSet.Name);
		$.each(biobankDataSets, function(index, dataSet){
			involvedDataSetNames.push(dataSet.Name);
		});
		var default_options = {
			'dataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'tableHeaders' : involvedDataSetNames,
			'queryText' : $('#search-dataitem').val(),
			'sortRule' : null,
			'createTableRow' : createTableRow,
			'updatePager' : false,
			'container' : $('#container')
		}
		if(options !== undefined && options !== null){
			$.extend(default_options, options);
		}
		molgenis.createMatrixForDataItems(default_options);
	}
	
	function createTableRow(featureFromIndex, table){
		var row = $('<tr />');
		var feature = restApi.get('/api/v1/observablefeature/' + featureFromIndex.id, {'expand' : ['unit']});
		var description = '<strong>' + feature.Name + '</strong> : ' + molgenis.i18nDescription(feature).en;
		var popover = makePopoverComponenet(description, Math.ceil((table.find('th:eq(0)').width() - 135)/4), molgenis.i18nDescription(feature).en);
		$('<td />').addClass('add-border show-popover').append(popover).appendTo(row).click(function(){
			var row = $(this).parents('tr:eq(0)');
			if(!$('body').data('clickedRow')) $('body').data('clickedRow', {});
			var storedRowInfo = $('body').data('clickedRow');
			storedRowInfo[molgenis.hrefToId(feature.href)] = row;
			createAnnotationModal(feature);
		});
		
		//Calculate the number of letters to show in the cell and full content is displayed in popover
		var numberOfDigit = Math.ceil(((table.width() - table.find('th:eq(0)').width()) / biobankDataSets.length - 100)/8);
		$.each(biobankDataSets, function(index, mappedDataSet){
			var removeIcon = $('<i />').addClass('icon-trash show-popover float-right');
			var editIcon = $('<i />').addClass('icon-pencil show-popover float-right');
			var cellDiv = $('<div />').addClass('row text-align-center').append(removeIcon).append(editIcon);
			$('<td />').append(cellDiv).appendTo(row);
			getMapping(createDataSetIdentifier(selectedDataSet, mappedDataSet), molgenis.hrefToId(feature.href), function(mappedFeatureNames){
				var topFeatureDivContainer = $('<div />').addClass('show-popover').appendTo(cellDiv).click(function(){
					editIcon.click();
				});
				if(mappedFeatureNames.length > 0){
					updateInfoInTableCell(editIcon, mappedFeatureNames, numberOfDigit);
				}
				
				//Initialize the edit click events
				editIcon.click(function(){
					//Remember which cell is clicked here
					$(document).data('clickedCell', editIcon);
					createMappings(selectedDataSet, mappedDataSet, feature, function(candidateMappedFeatures){
						getMapping(createDataSetIdentifier(selectedDataSet, mappedDataSet), molgenis.hrefToId(feature.href), function(mappedFeatureNames, script){
							standardModal.createModalCallback('Candidate mappings', function(modal){
								//Customize modal
								modal.attr('data-backdrop', true).css({
									'width' : '90%',
									'left' : '5%',
									'top' : '10%',
									'margin-left' : 0,
									'margin-top' : 0
								}).modal('show');
								
								//Modal body information.
								var body = modal.find('.modal-body:eq(0)').css('max-height','100%').append(createFeatureInfoPanel(feature));
								var leftControlDiv = $('<div />').addClass('col-md-6').css('margin-bottom','10px').append('<div><h4>' + selectedDataSet.Name +  '</h4></div>');
								var rightControlDiv = $('<div />').addClass('col-md-6').css('margin-bottom','10px').append('<div><h4>' + mappedDataSet.Name +  '</h4></div>');
								var modalBodyDiv = $('<div />').addClass('row').append(leftControlDiv).append(rightControlDiv).appendTo(body);
								var controlDiv = $('<div class="row"></div>').appendTo(body);
								var mappingTable = createMappingTable(candidateMappedFeatures, mappedFeatureNames);
								
								$('<div />').addClass('row well').append(mappingTable).css({
									'overflow-y' : 'scroll',
									'height' : $(document).height()/4,
									'padding-right' : '5px',
									'padding-left' : '5px'
								}).appendTo(rightControlDiv);
								
								var searchRequest = {
									'featureId' : molgenis.hrefToId(feature.href),
									'sourceDataSetId' : molgenis.hrefToId(selectedDataSet.href),
									'selectedDataSetIds' : [molgenis.hrefToId(mappedDataSet.href)]
								};
								var editor = createEditorInModel(leftControlDiv, mappedDataSet, script);
								addButtonsToControl(controlDiv, searchRequest, editor);
								
								//Modal footer information.
								var confirmButton = $('<button class="btn btn-primary">Confirm</button>');
								modal.find('.modal-footer:eq(0)').prepend(confirmButton);
								
								//Confirm button event handler
								confirmButton.click(function(){
									var mappedFeatureMap = updateMappingInfo(feature, mappingTable, mappedDataSet, mappedFeatureNames);
									mappedFeatureNames = mappedFeatureMap.mappedFeatureNames;
									updateInfoInTableCell(editIcon, mappedFeatureNames, numberOfDigit);
									standardModal.closeModal();
								});
								
								$.each($(mappingTable).find('tr:gt(0)'), function(index, row){
									$(row).children('td:first').click(function(){
										var variable = '$(\'' + $(this).text() + '\')';
										var value = editor.getValue();
										editor.setValue((value + '\n' + variable));
									})
								});
							});
						});
					});
				});
				
				//Initialize the remove click event
				removeIcon.click(function(){
					standardModal.createModalCallback('Confirmation', function(modal){
						var confirmButton = $('<button type="btn" class="btn btn-primary">Confirm</button>');
						var confirmationMessage = $('<p />').css('font-size', '16px').addClass('text-align-center');
						confirmationMessage.append('Are you sure that you want to remove candidate mappings for ');
						confirmationMessage.append('<span style="text-decoration:underline;"><strong>' + feature.Name + '</strong></span> in dataset ');
						confirmationMessage.append('<span style="text-decoration:underline;"><strong>' + mappedDataSet.Name + '</strong></span> ?');
						modal.find('div.modal-body:eq(0)').append(confirmationMessage);
						modal.find('div.modal-footer:eq(0)').prepend(confirmButton);
						modal.css({
							'width' : '50%',
							'left' : '25%',
							'top' : '30%',
							'margin-left' : 0,
							'margin-top' : 0
						}).modal('show');
						//Initialize the event for confirm button
						confirmButton.click(function(){
							removeSelectedMappings(molgenis.hrefToId(feature.href), [molgenis.hrefToId(mappedDataSet.href)], function(data){
								molgenis.getFeatureFromIndex(feature, function(featureFromIndex){
									updateExistingRow(featureFromIndex.columnValueMap, row, [molgenis.hrefToId(mappedDataSet.href)]);
									modal.modal('hide');									
								});
							});
						});
					});
				});
			});
		});
		return row;
		
		//Create the general info about parameter of interest
		function createFeatureInfoPanel(feature){
			var divRow = $('<div />').addClass('row');
			var infoDiv = $('<div />').addClass('col-md-3');
			$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.Name + '</span>').appendTo(infoDiv);
			if(feature.unit){
				$('<div />').append('<span class="info"><strong>Unit : </strong></span>').append('<span>' + feature.unit.Name + '</span>').appendTo(infoDiv);
			}
			$('<div />').append('<span class="info"><strong>Data type : </strong></span>').append('<span>' + feature.dataType + '</span>').appendTo(infoDiv);
			$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + molgenis.i18nDescription(feature).en + '</span>').appendTo(infoDiv);
			var middleDiv = $('<div />').addClass('col-md-9');
			var categories = getCategoriesByFeatureIdentifier(feature.Identifier);
			if(categories.length > 0){
				var categoryDiv = $('<div />').addClass('col-md-8').css('margin-left', '30px');
				$.each(categories, function(index, category){
					categoryDiv.append('<div>' + category.valueCode + ' = ' + category.Name + '</div>');
				});
				$('<div />').addClass('row').append('<div class="col-md-1"><strong>Categories: </strong></div>').append(categoryDiv).appendTo(middleDiv);
			}
			divRow.append(infoDiv).append(middleDiv);
			return divRow;
		}
		
		//Helper method for createTableRow function...
		function makePopoverComponenet(text, length, content){
			var popover = $('<span />').html(text.length < length ? text : text.substring(0, length) + '..');
			if(!(text.length < length)){
				popover.addClass('show-popover').popover({
					'content' : content,
					'trigger' : 'hover',
					'placement' : 'bottom',
					'html' : true
				});
			}
			return popover;
		}
		
		//Helper method for updating the information in the table cells
		function updateInfoInTableCell(editIcon, mappedFeatureNames, numberOfDigit){
			if(mappedFeatureNames.length === 0){
				editIcon.removeClass('icon-ok').addClass('icon-pencil').parents('td:eq(0)').css('background', '');
			}else{
				editIcon.removeClass('icon-pencil').addClass('icon-ok').parents('td:eq(0)').css('background-color', 'rgb(223, 235, 245)');
			}
			editIcon.siblings('div:eq(0)').empty().append(makePopoverComponenet(mappedFeatureNames.join(','), numberOfDigit, mappedFeatureNames.join('<br />')));
		}
		
		//Create ace editor for defining the algorithms
		function createEditorInModel(parentDiv, mappedDataSet, script){
			var algorithmEditorDiv = $('<div id="algorithmEditorDiv"></div>').addClass('well').css('height', $(document).height()/4).appendTo(parentDiv);
			var langTools = ace.require("ace/ext/language_tools");
			var editor = ace.edit('algorithmEditorDiv');
			editor.setOptions({
			    enableBasicAutocompletion: true
			});
			if($(document).data('previousScript')){
				script = $(document).data('previousScript');
				$(document).removeData('previousScript');
			}
			editor.setValue(script);
			editor.setTheme("ace/theme/chrome");
			editor.getSession().setMode("ace/mode/javascript");
			var algorithmEditorCompleter = {
		        getCompletions: function(editor, session, pos, prefix, callback) {
		            if (prefix.length === 0) { callback(null, []); return }
		            molgenis.dataItemsTypeahead(molgenis.hrefToId(mappedDataSet.href), prefix, function(results){
	            	callback(null, $.each(results, (function(index, featureName) {
		            		var map = $(document).data('dataMap')[featureName];
	                        return {name: '$(' + map.name + ')', value: '$(' + map.name + ')', score: map.score, meta: mappedDataSet.Name};
	                    })));
		            }, true);
		        }
		    }
		    langTools.addCompleter(algorithmEditorCompleter);
			return editor;
		}
		
		function addButtonsToControl(parentDiv, searchRequest, editor){
			var testStatisticsButton = $('<button class="btn btn-info">Test</button>');
			var suggestScriptButtion = $('<button class="btn btn-primary">Suggestion</button>');
			var saveScriptButton = $('<button class="btn btn-success">Save script</button>').css('float','right');
			$('<div class="col-md-7"></div>').append(testStatisticsButton).append(' ').append(suggestScriptButtion).append(' ').append(saveScriptButton).appendTo(parentDiv);
			
			testStatisticsButton.click(function(){
				var modalBody = parentDiv.parents('.modal-body:first');
				var modalFooter = parentDiv.parents('modal:first').children('.modal-footer');
				var ontologyMatcherRequest = $.extend(searchRequest, {
					'algorithmScript' : editor.getValue()
				});
				$.ajax({
					type : 'POST',
					url : molgenis.adaptContextUrl(molgenis.getContextUrl()) + '/testscript',
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
						var algorithmDiv = $('<div />').addClass('col-md-offset-3 col-md-6 well text-align-center').append('Test for variable <strong>' + featureObject.Name + '</strong> in dataset <strong>' + dataSetObject.Name + '</strong>');
						var tableDiv = $('<div />').addClass('col-md-6 well').css('min-height', statisticsDivHeight).append('<div class="legend-align-center">Summary statistics</div>').append(statisticsTable(data));
						var graphDiv = $('<div />').attr('id', graphDivId).addClass('col-md-6 well').css('min-height', statisticsDivHeight).append('<div class="legend-align-center">Distribution plot</div>').bcgraph(data.results);
						$('<div />').addClass('row').append(algorithmDiv).appendTo(modalBody);
						$('<div />').addClass('row').append(tableDiv).append(graphDiv).appendTo(modalBody);
						modalBody.next('div:first').empty().prepend(backButton);
						backButton.click(function(){
							$(document).data('previousScript',editor.getValue());
							$(document).data('clickedCell').click();
						});
					}
				});
			});
			
			suggestScriptButtion.click(function(){
				$.ajax({
					type : 'POST',
					url : molgenis.adaptContextUrl(molgenis.getContextUrl()) + '/suggestscript',
					async : false,
					data : JSON.stringify(searchRequest),
					contentType : 'application/json',
					success : function(data, textStatus, request) {	
						if(data.suggestedScript){
							editor.setValue(data.suggestedScript);
						}
					}
				});
			});
			
			saveScriptButton.click(function(){
				var modalBody = parentDiv.parents('.modal-body:eq(0)');
				var ontologyMatcherRequest = $.extend(searchRequest, {
					'algorithmScript' : editor.getValue()
				});
				$.ajax({
					type : 'POST',
					url : molgenis.adaptContextUrl(molgenis.getContextUrl()) + '/savescript',
					async : false,
					data : JSON.stringify(ontologyMatcherRequest),
					contentType : 'application/json',
					success : function(data, textStatus, request) {	
						modalBody.find('.alert').remove();
						molgenis.createAlert([data], 'success', modalBody);
						var mappedFeatureNames = extractFeatures(editor.getValue());
						$.each(parentDiv.parents('.modal-body:first').find('table input'), function(index, checkbox){
							$(checkbox).attr('checked',$.inArray($(checkbox).data('eachMapping').name, mappedFeatureNames) !== -1);
						});
						updateInfoInTableCell($(document).data('clickedCell'), mappedFeatureNames, numberOfDigit);
					}
				});
			});
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
		
		function extractFeatures(algorithm){
			var results = [];
			if(algorithm && algorithm.length > 0){
				$.each(algorithm.match(/\$\(\s*.[^\\$\\(\\)]+\s*\)/g), function(inex, group){
					results.push(group.replace(/\$\(\s*'\s*/, '').replace(/\s*'\s*\)/, ''));
				});
			}
			return results;
		}
	}
	
	function updateExistingRow(featureFromIndex, existingRow, rematchedSelectedDataSetIds){
		var biobankDataSetIds = [];
		$.each(biobankDataSets, function(index, dataSet){
			biobankDataSetIds.push(molgenis.hrefToId(dataSet.href));
		});
		$.each(rematchedSelectedDataSetIds, function(index, dataSetId){
			var index = $.inArray(dataSetId, biobankDataSetIds);
			if(index !== -1){
				var spinner = $('<img src="/img/waiting-spinner.gif">').css('height', '20px');
				existingRow.find('td:eq(' + (index + 1) + ')').empty().append(spinner).css('text-align', 'center');
			}
		});
		if(rematchedSelectedDataSetIds.length > 0){
			var columnIndex = $.inArray(rematchedSelectedDataSetIds[0], biobankDataSetIds)
			setTimeout(function(){
				var table = existingRow.parents('table:eq(0)');
				var rowIndex = $.inArray(existingRow[0], table.find('tr'));
				var updatedRow = createTableRow(featureFromIndex, table);
				var columnToUpdate = updatedRow.find('td:eq(' + (columnIndex + 1) + ')');
				table.find('tr:eq(' + --rowIndex + ')').after(updatedRow);
				existingRow.remove();
			}, 1500);
		}
	}
	
	function removeSelectedMappings(featureId, mappedDataSetIds, callback){
		var request = {
			'sourceDataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'selectedDataSetIds' : mappedDataSetIds,
			'featureId' : featureId,
			'mappedFeatureIds' : []
		};
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/savescript',
			data : JSON.stringify(request),
			contentType : 'application/json',
			success : function(data){
				if(callback !== undefined && callback !== null && typeof(callback) === 'function'){
					callback(data);
				}
			}
		});
	}
	
	function updateMappingInfo(feature, mappingTable, mappedDataSet, mappedFeatureNames){
		var selectedFeatureMap = {};
		$.each(mappingTable.find('input'), function(index, checkBox){
			var eachMapping = $(checkBox).data('eachMapping');
			if(checkBox.checked)
			{
				selectedFeatureMap[eachMapping.mappedFeature.Name] = molgenis.hrefToId(eachMapping.mappedFeature.href);
			}
		});
		var results = {
			'mappedFeatureNames' : Object.keys(selectedFeatureMap)
		}
		if(!compareTwoArrays(selectedFeatureMap, mappedFeatureNames)){
			var mappedFeatureIds = Object.keys(selectedFeatureMap).map(function(key){
				return selectedFeatureMap[key];
			});
			var request = {
				'sourceDataSetId' : molgenis.hrefToId(selectedDataSet.href),
				'selectedDataSetIds' : [molgenis.hrefToId(mappedDataSet.href)],
				'featureId' : molgenis.hrefToId(feature.href),
				'mappedFeatureIds' : mappedFeatureIds
			};
			$.ajax({
				type : 'POST',
				url : molgenis.adaptContextUrl() + '/savescript',
				data : JSON.stringify(request),
				async : false,
				contentType : 'application/json',
				success : function(data){				
					$.extend(results, data);
				}
			});
		}
		return results;
	}
	
	function getMapping(dataSetIdentifier, featureId, callback){
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/getmapping',
			data : JSON.stringify({'dataSetIdentifier' : dataSetIdentifier , 'featureIds' : [featureId]}),
			contentType : 'application/json',
			success : function(data, textStatus, request){				
				var results = [];
				var script = null;
				if(data.searchHits.length > 0){
					var hit = data.searchHits[0].columnValueMap;
					var mappedFeatureString = hit[storeMappingMappedFeature];
					var mappedFeatureIds = (mappedFeatureString.length > 2) ? mappedFeatureString.substring(1, mappedFeatureString.length - 1).split(/\s*,\s*/) : [];
					script = hit[mappingScript];
					if(mappedFeatureIds.length > 0){
						var mappedFeatureItems = restApi.get('/api/v1/observablefeature/', {
							'q' : {
								'q' : [{
									'field' : 'id',
									'operator' : 'IN',
									'value' : mappedFeatureIds
								}]
							}
						});
						$.each(mappedFeatureItems.items, function(index, feature){
							if($.inArray(feature.Name, results) === -1) results.push(feature.Name);
						});
					}
				}
				callback(results, script);
			}
		});
	}
	
	function createMappings(selectedDataSet, mappedDataSet, feature, callback){
		var searchRequest = {
			featureId : molgenis.hrefToId(feature.href),
			sourceDataSetId : molgenis.hrefToId(selectedDataSet.href),
			selectedDataSetIds : [molgenis.hrefToId(mappedDataSet.href)]
		};
		$.ajax({
			type : 'POST',
			url : molgenis.adaptContextUrl() + '/createmapping',
			data : JSON.stringify(searchRequest),
			contentType : 'application/json',
			success : function(data, textStatus, request) {	
				var uniqueFeatureMap = {};
				var mappedFeatues = [];
				var existingIds = [];
				$.each(data.searchHits, function(index, hit){
					
					if($.inArray(hit.columnValueMap.id, existingIds) === -1){
						existingIds.push(hit.columnValueMap.id);
						mappedFeatues.push(hit.columnValueMap);
					}
					
					if(!uniqueFeatureMap[hit.columnValueMap.id]){
						hit.columnValueMap.category = [];
						uniqueFeatureMap[hit.columnValueMap.id] = hit.columnValueMap;
					}
					
					if(hit.columnValueMap.type === 'category'){
						uniqueFeatureMap[hit.columnValueMap.id].category.push(hit.columnValueMap.name);
					}
					
				});
				callback(mappedFeatues);
			},
			error : function(request, textStatus, error){
				callback([]);
			}
		});
	}
	
	function createMappingTable(candidateMappedFeatures, mappedFeatureNames){
		var mappingTable = $('<table />').addClass('table table-bordered').append('<tr><th>Name</th><th>Description</th><th>Score</th><th>Select</th></tr>').css({
			'width' : '96%',
			'margin-left' : '2%'
		});
		var scores = [];
		$.each(candidateMappedFeatures, function(index, eachMapping){
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
		} else {
			$.each(scores, function(index, score){
				highScoresIndex.push(index);
			});
		}
		var soretedMappedFeatures = sortByScoreAndLength(candidateMappedFeatures);
		$.each(soretedMappedFeatures, function(index, mappedFeatureFromIndex){
			var mappedFeatureId = mappedFeatureFromIndex.id;
			var score = mappedFeatureFromIndex.score;
			var mappedFeature = restApi.get('/api/v1/observablefeature/' + mappedFeatureId);
			if(mappedFeature){
				var row = $('<tr />');
				var checkBox = $('<input type="checkbox">');
				mappedFeatureFromIndex.mappedFeature = mappedFeature;
				checkBox.data('eachMapping', mappedFeatureFromIndex);
				checkBox.attr('checked', $.inArray(mappedFeature.Name, mappedFeatureNames) !== -1)
				row.append('<td>' + mappedFeature.Name + '</td><td>' + molgenis.i18nDescription(mappedFeature).en + '</td><td>' + score + '</td>');
				row.append($('<td />').append($('<label class="checkbox"></label>').append(checkBox))).appendTo(mappingTable);
				//If the query is matched with categories instead of feature description
				if(mappedFeatureFromIndex.type === 'category' || mappedFeature.dataType === 'categorical'){
					row.children('td:lt(2)').css('cursor', 'pointer').click(function(){
						retrieveAllInfoForFeature(row, mappedFeature, mappedFeatureFromIndex.category);
					});
				}
			}
			if($.inArray(index, highScoresIndex) !== -1){
				row.addClass('info');
			}else{
				row.addClass('warning');
			}
		});
		return mappingTable;
		
		function retrieveAllInfoForFeature(clickedRow, featureEntity, categoryIdentifiers){
			var detailInfoTable = $('<table class="table table-bordered"></table>');
			detailInfoTable.append('<tr><th>Id</th><td>' + molgenis.hrefToId(featureEntity.href) + '</td></tr>');
			detailInfoTable.append('<tr><th>Name</th><td>' + featureEntity.Name + '</td></tr>');
			if(featureEntity.unit !== undefined && featureEntity.unit !== null){
				detailInfoTable.append('<tr><th>Unit</th><td>' + featureEntity.unit.Name + '</td></tr>');
			}
			detailInfoTable.append('<tr><th>Data type</th><td>' + featureEntity.dataType + '</td></tr>');
			detailInfoTable.append('<tr><th>Description</th><td>' + molgenis.i18nDescription(featureEntity).en + '</td></tr>');
			var categories = getCategoriesByFeatureIdentifier(featureEntity.Identifier);
			if(categories.length > 0){
				var categoryDiv = $('<div />');
				$.each(categories, function(index, category){
					if($.inArray(category.Identifier, categoryIdentifiers) !== -1){
						categoryDiv.append('<div style="color:#3F8B1C"><strong>' + category.valueCode + ' = ' + category.Name + '</strong><div class="float-right">(mapped code)</div></div>');
					}else{						
						categoryDiv.append('<div>' + category.valueCode + ' = ' + category.Name + '</div>');
					}
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
	}
	
	function sortByScoreAndLength(mappedFeatures){
		var total = mappedFeatures.length;
		var subSetMappings = mappedFeatures.slice(0, total < 10 ? total : 10);
		var map = {};
		$.each(subSetMappings, function(index, mappedFeatureFromIndex){
			var score = mappedFeatureFromIndex.score;
			mappedFeatureFromIndex.comparedScore = score / mappedFeatureFromIndex.description.length;
			if(!map[score]){
				map[score] = [];
			}
			map[score].push(mappedFeatureFromIndex);
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
		});
	}
	
	function createRematchingModal(feature){
		standardModal.createModalCallback('Rematch research variable : ' + feature.Name, function(modal){
			var divControlPanel = $('<div />').addClass('row');
			var selectTag = $('<select />');
			var rematchedSelectedDataSetIds = [];
			$.each(biobankDataSets, function(index, dataSet){
				selectTag.append('<option value="' + molgenis.hrefToId(dataSet.href) + '">' + dataSet.Name + '</option>');
				rematchedSelectedDataSetIds.push(molgenis.hrefToId(dataSet.href));
			});
			var selectButton = $('<button type="btn" class="btn btn-info">Select</button>');
			var selectAllButton = $('<button type="btn" class="btn btn-primary">Select all</button>');
			var removeAllButton = $('<button type="btn" class="btn btn">Remove all</button>');
			var matchButton = $('<button type="btn" class="btn btn-primary">Rematch</button>');
			
			$('<div />').addClass('col-md-offset-1 col-md-3').append(selectTag).appendTo(divControlPanel);
			$('<div />').addClass('col-md-offset-1 col-md-5 btn-group').append(selectButton).append(selectAllButton).append(removeAllButton).appendTo(divControlPanel);
			
			var infoContainer = $('<div />').addClass('row');
			var dataSetsContainer = $('<div />').addClass('col-md-offset-1 col-md-10 well').appendTo(infoContainer);
			$('<div />').addClass('col-md-12').append('<legend class="legend-small">Selected catalogues : </legend>').appendTo(dataSetsContainer);
			
			modal.find('div.modal-body:eq(0)').append(divControlPanel).append(infoContainer);
			modal.find('div.modal-footer:eq(0)').prepend(matchButton);
			modal.css({
				'width' : '50%',
				'left' : '25%',
				'top' : '30%',
				'margin-left' : 0,
				'margin-top' : 0
			}).modal('show');
			
			//Initialize all the button events
			selectButton.click(function(){
				var selectedDataSetId = selectTag.val();
				if(selectedDataSetId !== null && selectedDataSetId !== undefined){
					if($.inArray(selectedDataSetId, rematchedSelectedDataSetIds) === -1){
						rematchedSelectedDataSetIds.push(selectedDataSetId);
						updateSelectedDataSets(selectedDataSetId, $(selectTag));
						listAllDataSets(dataSetsContainer, rematchedSelectedDataSetIds)
					}
				}
			})
			
			selectAllButton.click(function(){
				$(selectTag).find('option').each(function(){
					if($.inArray($(this).val(), rematchedSelectedDataSetIds) === -1){
						rematchedSelectedDataSetIds.push($(this).val());
					}
				});
				listAllDataSets(dataSetsContainer, rematchedSelectedDataSetIds)
			});
			
			removeAllButton.click(function(){
				rematchedSelectedDataSetIds = [];
				listAllDataSets(dataSetsContainer, rematchedSelectedDataSetIds)
			});
			
			matchButton.click(function(){
				removeSelectedMappings(molgenis.hrefToId(feature.href), rematchedSelectedDataSetIds, function(data){
					molgenis.getFeatureFromIndex(feature, function(featureFromIndex){
						modal.modal('hide');
						var storedRowInfo = $('body').data('clickedRow');
						var existingRow = storedRowInfo[molgenis.hrefToId(feature.href)];
						updateExistingRow(featureFromIndex.columnValueMap, existingRow, rematchedSelectedDataSetIds);
					});
				});
			});
			
			selectAllButton.click();
		});
		
		function listAllDataSets(dataSetsContainer, rematchedSelectedDataSetIds){
			dataSetsContainer.find('div:gt(0)').remove();
			$.each(rematchedSelectedDataSetIds, function(index, dataSetId){
				var dataSet = restApi.get('/api/v1/dataset/' + dataSetId);
				var newRow = $('<div />').addClass('row');
				$('<div />').addClass('col-md-offset-2 col-md-3').append(dataSet.Name).appendTo(newRow);
				var removeButton = $('<button type="btn" class="btn btn-link">Remove</button>');
				$('<div />').addClass('col-md-offset-3 col-md-2').append(removeButton).appendTo(newRow);
				dataSetsContainer.append(newRow);
				removeButton.click(function(){
					rematchedSelectedDataSetIds.splice($.inArray(dataSetId, rematchedSelectedDataSetIds), 1);
					newRow.remove();
				});
			});
		}
		
		function updateSelectedDataSets(targetDataSetId, rematchedSelectedDataSetIds){
			var index = 0;
			var options = $(rematchedSelectedDataSetIds).find('option');
			options.attr('selected', false).each(function(){
				if(targetDataSetId !== $(this).val()){
					index++;
				}else return false;
			});
			index = index === options.length - 1 ? 0 : index + 1;
			$(options[index]).attr('selected', true);
		}
	}
	
	function getCategoriesByFeatureIdentifier(featureIdentifier){
		var categories = restApi.get('/api/v1/category/', {
			'q' : { 
				'q' : [{
					'field' : 'observableFeature',
					'operator' : 'EQUALS',
					'value' : featureIdentifier
				}]
			}
		});
		return categories.items; 
	}
	
	function compareTwoArrays(selectedFeatureMap, mappedFeatureNames){
		var selectedFeatureNames = Object.keys(selectedFeatureMap);
		if(selectedFeatureNames.length !== mappedFeatureNames.length) {
			return false;
		}
		for(var i = 0; i < selectedFeatureNames.length; i++){
			if($.inArray(selectedFeatureNames[i], mappedFeatureNames) === -1){
				return false;
			}
		}
		return true;
	}
	
	molgenis.MappingManager.prototype.downloadMappings = function(){
		var mappedDataSetIds = [];
		$.each(biobankDataSets, function(index, dataSet){
			mappedDataSetIds.push(molgenis.hrefToId(dataSet.href));
		});
		var deleteRequest = {
			'dataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'matchedDataSetIds' : mappedDataSetIds,
			'documentType' : selectedDataSet.Identifier
		};
		console.log(deleteRequest);
		$.download(molgenis.getContextUrl() + '/download',{request : JSON.stringify(deleteRequest)});
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
				'width' : '50%',
				'left' : '25%',
				'top' : '30%',
				'margin-left' : 0,
				'margin-top' : 0
			}).modal('show');
		});
	};
	
	molgenis.MappingManager.prototype.getAllMappedDataSetIds = function (userName, selectedDataSetId){
		var mappings = restApi.get('/api/v1/dataset/', {
			'q' : {
				'q' : [{
					'field' : 'Identifier',
					'operator' : 'LIKE',
					'value' : userName + '-' + selectedDataSetId
				}]
			},
			'expand' : ['ProtocolUsed']
		}).items;
		var mappedDataSetIds = [];
		$.each(mappings, function(index, dataSet){
			if(dataSet.ProtocolUsed.Identifier === 'store_mapping'){
				var identifier = dataSet.Identifier;
				var dataSetIdArray = identifier.split('-');
				mappedDataSetIds.push(dataSetIdArray[dataSetIdArray.length - 1]);
			}
		});
		return mappedDataSetIds;
	};
	
	function remove(object, array){
		var newArray = [];
		$.each(array, function(index, element){
			if(element !== object){
				newArray.push(element);
			}
		});
		return newArray;;
	}
	
	function setUserName(name){
		userName = name;
	}
	
	function getUserName(){
		return userName;
	}
	
	function createDataSetIdentifier(targetDataSet, sourceDataSet){
		var dataSetIdentifier = '';
		if(targetDataSet.href && sourceDataSet.href){
			dataSetIdentifier = getUserName() + '-' + molgenis.hrefToId(targetDataSet.href) + '-' + molgenis.hrefToId(sourceDataSet.href);
		}else{
			dataSetIdentifier = getUserName() + '-' + targetDataSet + '-' + sourceDataSet;
		}
		return dataSetIdentifier;
	}
}($, window.top.molgenis = window.top.molgenis || {}, window.top));