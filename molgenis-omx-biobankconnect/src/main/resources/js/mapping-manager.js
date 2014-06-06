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
	var mappingScript = 'store_mapping_algorithm_script';
	var observationSet = 'observationsetid';
	
	molgenis.MappingManager = function MappingManager(){
		
	};
	
	molgenis.MappingManager.prototype.changeDataSet = function(userName, selectedDataSetId, dataSetIds){
		if(selectedDataSetId !== '' && dataSetIds.length > 0){
			setUserName(userName); 
			selectedDataSet = restApi.get('/api/v1/dataset/' + selectedDataSetId, {'expand' : ['protocolUsed']});
			biobankDataSets = restApi.get('/api/v1/dataset/', {
				'q' : {
					'q' : [{
						'field' : 'id',
						'operator' : 'IN',
						'value' : dataSetIds
					}]
				},
				'expand' : ['protocolUsed']
			}).items;
			
			var involvedDataSetNames = [];
			involvedDataSetNames.push(selectedDataSet.Name);
			$.each(biobankDataSets, function(index, dataSet){
				involvedDataSetNames.push(dataSet.Name);
			});
			var attributes = restApi.get('/api/v1/' + selectedDataSet.Identifier + '/meta', {'expand' : ['attributes']});
			$('#dataitem-number').empty().append(Object.keys(attributes.attributes).length);
			updateMatrix({'tableHeaders' : involvedDataSetNames});
			initSearchDataItems();
		}else{
			$('#dataitem-number').empty().append('Nothing selected');
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
		var default_options = {
			'dataSetId' : molgenis.hrefToId(selectedDataSet.href),
			'tableHeaders' : ['Name', 'Description'],
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
	
	function createTableRow(featureFromIndex){
		var row = $('<tr />');
		var feature = restApi.get('/api/v1/observablefeature/' + featureFromIndex.id);
		var description = '<strong>' + feature.Name + '</strong> : ' + molgenis.i18nDescription(feature).en;
		var popover = $('<span />').html(description.length < 90 ? description : description.substring(0, 90) + ' ...');
		if(!(description.length < 90)){
			popover.addClass('show-popover').popover({
				'content' : molgenis.i18nDescription(feature).en,
				'trigger' : 'hover',
				'placement' : 'bottom'
			});
		}
		$('<td />').addClass('add-border show-popover').append(popover).appendTo(row).click(function(){
			var row = $(this).parents('tr:eq(0)');
			if(!$('body').data('clickedRow')) $('body').data('clickedRow', {});
			var storedRowInfo = $('body').data('clickedRow');
			storedRowInfo[molgenis.hrefToId(feature.href)] = row;
			createAnnotationModal(feature);
		});
		
		$.each(biobankDataSets, function(index, mappedDataSet){
			var removeIcon = $('<i />').addClass('icon-trash show-popover float-right');
			var editIcon = $('<i />').addClass('show-popover icon-pencil float-right');
			var spinner = $('<img src="/img/waiting-spinner.gif">').css('height', '15px');
			var cellDiv = $('<div />').addClass('row-fluid text-align-center').append(spinner).append(removeIcon).append(editIcon);
			$('<td />').append(cellDiv).appendTo(row);
			createMappings(selectedDataSet, mappedDataSet, feature, function(candidateMappedFeatures){
				var mappedFeatureNames = getMapping(createDataSetIdentifier(selectedDataSet, mappedDataSet), molgenis.hrefToId(feature.href));
				var topFeatureDivContainer = $('<div />').addClass('show-popover span10').appendTo(cellDiv).click(function(){
					editIcon.click();
				});
				if(mappedFeatureNames.length > 0){
					topFeatureDivContainer.append('<span style="float:left;color:#04B4AE;"><strong>Complete</strong></span>').append(mappedFeatureNames.join(' , '));
				}else{
					topFeatureDivContainer.append('<span style="float:left;">Choose matches</span>');
				}
				//Initialize the edit click events
				editIcon.click(function(){
					standardModal.createModalCallback('Candidate mappings', function(modal){
						//Customize modal
						modal.modal('show').css({
							'width' : '90%',
							'margin-left' : '-45%',
							'top' : '15%'
						});
						
						//Modal header information.
						var infoDiv = $('<div />').addClass('span4').css({
							'position' : 'absolute',
							'margin-left' : 0,
							'margin-top' : 25,
							'z-index' : 10000
						});
						$('<div />').append('<h4>' + selectedDataSet.Name + '</h4>').appendTo(infoDiv);
						$('<div />').append('<span class="info"><strong>Data item : </strong></span>').append('<span>' + feature.Name + '</span>').appendTo(infoDiv);
						$('<div />').append('<span class="info"><strong>Description : </strong></span>').append('<span>' + molgenis.i18nDescription(feature).en + '</span>').appendTo(infoDiv);
						var selectedMappings = $('<div />').append('<span class="info"><strong>Selected mappings : </strong></span>').append('<span>' + mappedFeatureNames.join(' , ') + '</span>').appendTo(infoDiv);
						modal.find('.modal-header:eq(0)').append(infoDiv);
						
						//Modal body information.
						var mappingTable = createMappingTable(feature, candidateMappedFeatures, mappedFeatureNames);
						var tableDiv = $('<div />').addClass('span7').css('margin-left', '-10px').append('<div><h4>' + mappedDataSet.Name +  '</h4></div>').append(mappingTable);
						var body = modal.find('.modal-body:eq(0)').addClass('overflow-y-auto').css({
							'min-height' : 250,
							'max-height' : 350
						});
						$('<div />').addClass('row-fluid').append('<div class="span5"></div>').append(tableDiv).appendTo(body);

						//Modal footer information.
						var confirmButton = $('<button class="btn btn-primary">Confirm</button>');
						modal.find('.modal-footer:eq(0)').prepend(confirmButton);
						
						//Confirm button event handler
						confirmButton.click(function(){
							var mappedFeatureMap = updateMappingInfo(feature, mappingTable, mappedDataSet, mappedFeatureNames);
							mappedFeatureNames = mappedFeatureMap.mappedFeatureNames;
							$(editIcon).siblings('div:eq(0)').empty().append('<span style="float:left;color:#04B4AE;"><strong>Complete</strong></span>').append(mappedFeatureNames.join(' , '));
							standardModal.closeModal();
						});
						//table check box handler
						tableDiv.find('input[type="checkbox"]').click(function(){
							var dataItems = [];
							tableDiv.find('input:checked').each(function(index, checkbox){
								dataItems.push($(checkbox).data('eachMapping').mappedFeature.Name);
							});
							selectedMappings.find('span:eq(1)').html(dataItems.join(' , '));
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
						modal.modal('show').css({
							'width' : '50%',
							'margin-left' : '-25%',
							'top' : '30%'
						});
						//Initialize the event for confirm button
						confirmButton.click(function(){
							removeSelectedMappings(molgenis.hrefToId(feature.href), [molgenis.hrefToId(mappedDataSet.href)]);
							$(editIcon).siblings('div:eq(0)').empty().append('<span style="float:left;">Choose matches</span>');
							modal.modal('hide');
						});
					});
				});
				
				spinner.remove();
			});
		});
		return row;
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
			url : molgenis.getContextUrl() + '/savescript',
			data : JSON.stringify(request),
			async : false,
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
			var changedValue = null;
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
				url : molgenis.getContextUrl() + '/savescript',
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
	
	function getMapping(dataSetIdentifier, featureId){
		var results = [];
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/getmapping',
			data : JSON.stringify({'dataSetIdentifier' : dataSetIdentifier , 'featureIds' : [featureId]}),
			async : false,
			contentType : 'application/json',
			success : function(data, textStatus, request){				
				if(data.searchHits.length > 0){
					var hit = data.searchHits[0].columnValueMap;
					var mappedFeatureString = hit[storeMappingMappedFeature];
					var mappedFeatureIds = (mappedFeatureString.length > 2) ? mappedFeatureString.substring(1, mappedFeatureString.length - 1).split(/\s*,\s*/) : [];
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
			}
		});
		return results;
	}
	
	function createMappings(selectedDataSet, mappedDataSet, feature, callback){
		var searchRequest = {
			featureId : molgenis.hrefToId(feature.href),
			sourceDataSetId : molgenis.hrefToId(selectedDataSet.href),
			selectedDataSetIds : [molgenis.hrefToId(mappedDataSet.href)]
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/createmapping',
			data : JSON.stringify(searchRequest),
			contentType : 'application/json',
			success : function(data, textStatus, request) {	
				var mappedFeatues = [];
				var existingIds = [];
				$.each(data.searchHits, function(index, hit){
					if($.inArray(hit.columnValueMap.id, existingIds) === -1){
						existingIds.push(hit.columnValueMap.id);
						mappedFeatues.push(hit.columnValueMap);
					}
				});
				callback(mappedFeatues);
			},
			error : function(request, textStatus, error){
				console.log(error);
				callback([]);
			}
		});
	}
	
	function createMappingTable(feature, candidateMappedFeatures, mappedFeatureNames){
		var mappingTable = $('<table />').addClass('table table-bordered').append('<tr><th>Name</th><th>Description</th><th>Score</th><th>Select</th></tr>');
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
			}
			if($.inArray(index, highScoresIndex) !== -1){
				row.addClass('info');
			}else{
				row.addClass('warning');
			}
		});
		return mappingTable;
	}
	
	function sortByScoreAndLength(mappedFeatures){
		var total = mappedFeatures.length;
		var subSetMappings = mappedFeatures.slice(0, total < 10 ? total : 10);
		var map = {};
		$.each(subSetMappings, function(index, mappedFeatureFromIndex){
			var mappedFeature = restApi.get('/api/v1/observablefeature/' + mappedFeatureFromIndex.id);
			var score = mappedFeatureFromIndex.score;
			mappedFeatureFromIndex.comparedScore = score / mappedFeature.description.length;
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
			var divControlPanel = $('<div />').addClass('row-fluid');
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
			
			$('<div />').addClass('offset1 span3').append(selectTag).appendTo(divControlPanel);
			$('<div />').addClass('offset1 span5 btn-group').append(selectButton).append(selectAllButton).append(removeAllButton).appendTo(divControlPanel);
			
			var infoContainer = $('<div />').addClass('row-fluid');
			var dataSetsContainer = $('<div />').addClass('offset1 span10 well').appendTo(infoContainer);
			$('<div />').addClass('span12').append('<legend class="legend-small">Selected catalogues : </legend>').appendTo(dataSetsContainer);
			
			modal.find('div.modal-body:eq(0)').append(divControlPanel).append(infoContainer);
			modal.find('div.modal-footer:eq(0)').prepend(matchButton);
			modal.modal('show');
			
			selectButton.click(function(){
				var selectedDataSetId = selectTag.val();
				if(selectedDataSetId !== null && selectedDataSetId !== undefined){
					if($.inArray(selectedDataSetId, rematchedSelectedDataSetIds) === -1){
						rematchedSelectedDataSetIds.push(selectedDataSetId);
						switchOptions(selectedDataSetId, $(selectTag));
						listOfOptions(dataSetsContainer, rematchedSelectedDataSetIds)
					}
				}
			})
			
			selectAllButton.click(function(){
				$(selectTag).find('option').each(function(){
					if($.inArray($(this).val(), rematchedSelectedDataSetIds) === -1){
						rematchedSelectedDataSetIds.push($(this).val());
					}
				});
				listOfOptions(dataSetsContainer, rematchedSelectedDataSetIds)
			});
			
			removeAllButton.click(function(){
				rematchedSelectedDataSetIds = [];
				listOfOptions(dataSetsContainer, rematchedSelectedDataSetIds)
			});
			
			matchButton.click(function(){
				molgenis.getFeatureFromIndex(feature, function(featureFromIndex){
					removeSelectedMappings(molgenis.hrefToId(feature.href), rematchedSelectedDataSetIds);
					modal.modal('hide');
					var storedRowInfo = $('body').data('clickedRow');
					var existingRow = storedRowInfo[molgenis.hrefToId(feature.href)];
					var biobankDataSetIds = [];
					$.each(biobankDataSets, function(index, dataSet){
						biobankDataSetIds.push(molgenis.hrefToId(dataSet.href));
					});
					$.each(rematchedSelectedDataSetIds, function(index, dataSetId){
						var index = $.inArray(dataSetId, biobankDataSetIds);
						if(index !== -1){
							var spinner = $('<img src="/img/waiting-spinner.gif">').css('height', '15px');
							existingRow.find('td:eq(' + (index + 1) + ')').empty().append(spinner).css('text-align', 'center');
						}
					});
					var table = existingRow.parents('table:eq(0)');
					var rowIndex = $.inArray(existingRow[0], table.find('tr'));
					if(rowIndex > 0){
						var updatedRow = createTableRow(featureFromIndex.columnValueMap);
						table.find('tr:eq(' + --rowIndex + ')').after(updatedRow);
						existingRow.remove();
						$.each(rematchedSelectedDataSetIds, function(index, dataSetId){
							var index = $.inArray(dataSetId, biobankDataSetIds);
							if(index !== -1){
//								var columnToUpdate = updatedRow.find('td:eq(' + (index + 1) + ')');
//								columnToUpdate.find('div:eq(1)').empty();
							}
						});
					}
				});
			});
			selectAllButton.click();
		});
		
		function listOfOptions(dataSetsContainer, rematchedSelectedDataSetIds){
			dataSetsContainer.find('div:gt(0)').remove();
			$.each(rematchedSelectedDataSetIds, function(index, dataSetId){
				var dataSet = restApi.get('/api/v1/dataset/' + dataSetId);
				var newRow = $('<div />').addClass('row-fluid');
				$('<div />').addClass('offset2 span3').append(dataSet.Name).appendTo(newRow);
				var removeButton = $('<button type="btn" class="btn btn-link">Remove</button>');
				$('<div />').addClass('offset3 span2').append(removeButton).appendTo(newRow);
				dataSetsContainer.append(newRow);
				removeButton.click(function(){
					rematchedSelectedDataSetIds.splice($.inArray(dataSetId, rematchedSelectedDataSetIds), 1);
					newRow.remove();
				});
			});
		}
		
		function switchOptions(targetDataSetId, rematchedSelectedDataSetIds){
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
			modal.modal('show').css({
				'width' : '50%',
				'margin-left' : '-25%',
				'top' : '30%'
			});
		});
	};
	
	function setUserName(name){
		userName = name;
	}
	
	function getUserName(){
		return userName;
	}
	
	function createDataSetIdentifier(targetDataSet, sourceDataSet){
		return getUserName() + '-' + molgenis.hrefToId(targetDataSet.href) + '-' + molgenis.hrefToId(sourceDataSet.href);
	}
}($, window.top.molgenis = window.top.molgenis || {}, window.top));